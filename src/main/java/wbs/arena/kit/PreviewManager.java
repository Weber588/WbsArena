package wbs.arena.kit;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.utils.util.entities.state.SavedEntityState;

import java.util.HashMap;
import java.util.Map;

public final class PreviewManager {
    private PreviewManager() {}

    private static final Map<Player, PreviewInstance> previewingPlayers = new HashMap<>();

    public static boolean isPreviewing(Player player) {
        return previewingPlayers.containsKey(player);
    }

    public static void startPreview(Player player, Kit kit) {
        if (isPreviewing(player)) {
            endPreview(player);
        }
        SavedEntityState<Player> state = Kit.getDefaultEntityState();

        state.captureState(player);
        PreviewInstance preview = new PreviewInstance(player, -1, state, kit);
        preview.start();
        previewingPlayers.put(player, preview);
    }

    public static void startTimedPreview(Player player, Kit kit, int durationInTicks) {
        WbsArena plugin = WbsArena.getInstance();

        if (isPreviewing(player)) {
            endPreview(player);
        }

        SavedEntityState<Player> state = Kit.getDefaultEntityState();

        state.captureState(player);

        int taskId;
        if (durationInTicks <= 0) {
            durationInTicks = ArenaSettings.DEFAULT_PREVIEW_DURATION;
        }
        final int previewDuration = durationInTicks;

        taskId = new BukkitRunnable() {
            int secondsRemaining = (int) Math.ceil(previewDuration / 20.0);
            @Override
            public void run() {
                secondsRemaining--;

                if (secondsRemaining <= 0) {
                    endPreview(player);
                    cancel();
                } else {
                    plugin.sendActionBar("&bPreview: " + kit.getDisplayName() + "&b (" + secondsRemaining + ")", player);
                }
            }
        }.runTaskTimer(plugin, 20, 20).getTaskId();

        PreviewInstance preview = new PreviewInstance(player, taskId, state, kit);
        preview.start();
        previewingPlayers.put(player, preview);
    }

    public static Kit getPreviewedKit(Player player) {
        PreviewInstance preview = previewingPlayers.get(player);

        if (preview == null) return null;

        return preview.getKit();
    }

    public static void endPreview(Player player) {
        PreviewInstance preview = previewingPlayers.get(player);

        if (preview == null) return;

        preview.end();
        previewingPlayers.remove(player);
    }
}
