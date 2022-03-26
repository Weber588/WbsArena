package wbs.arena.data;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.utils.util.database.AbstractDataManager;
import wbs.utils.util.database.WbsRecord;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerManager extends AbstractDataManager<ArenaPlayer, UUID> {
    private final WbsArena plugin;
    public PlayerManager(WbsArena plugin) {
        super(plugin, ArenaDB.playerTable);
        this.plugin = plugin;
    }

    @Override
    protected @NotNull ArenaPlayer fromRecord(@NotNull WbsRecord wbsRecord) {
        return new ArenaPlayer(wbsRecord);
    }

    @Override
    protected @NotNull ArenaPlayer produceDefault(UUID uuid) {
        return new ArenaPlayer(uuid);
    }

    private final Set<ArenaPlayer> pendingSaves = new HashSet<>();

    private int saveTimerId = -1;

    public void cancelSaveTimer() {
        if (saveTimerId != -1) {
            Bukkit.getScheduler().cancelTask(saveTimerId);
            saveTimerId = -1;
        }
    }

    public int startSaveTimer() {
        cancelSaveTimer();

        int saveFrequency = plugin.settings.saveFrequency();

        saveTimerId = new BukkitRunnable() {
            @Override
            public void run() {
                saveAsync(pendingSaves, () -> {
                    plugin.logger.info("Saved " + pendingSaves.size() + " players!");
                    for (ArenaPlayer saved : pendingSaves) {
                        saved.setPendingSave(false);
                    }
                    pendingSaves.clear();
                });
            }
        }.runTaskTimer(plugin, saveFrequency, saveFrequency).getTaskId();

        return saveTimerId;
    }

    public void addToPendingSaves(ArenaPlayer arenaPlayer) {
        if (plugin.settings.getSaveMethod() == ArenaSettings.SaveMethod.PERIODIC) {
            pendingSaves.add(arenaPlayer);
        }
    }
}
