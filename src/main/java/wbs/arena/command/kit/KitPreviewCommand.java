package wbs.arena.command.kit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.kit.PreviewManager;
import wbs.arena.menu.kit.KitPreviewMenu;

import java.util.LinkedList;
import java.util.List;

public class KitPreviewCommand extends KitSubcommand {

    private final WbsArena plugin;

    public KitPreviewCommand(@NotNull WbsArena plugin) {
        super(plugin, "preview");
        this.plugin = plugin;
    }

    @Override
    protected boolean onKitCommandNoKit(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), this::openPreviewMenu);
        return true;
    }

    @Override
    protected boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (PreviewManager.isPreviewing(player)) {
            Kit previewKit = PreviewManager.getPreviewedKit(player);
            if (kit.equals(previewKit)) {
                PreviewManager.endPreview(player);
                sendMessage("Preview ended.", player);
                return true;
            } else {
                PreviewManager.endPreview(player);
            }
        }

        int previewDuration = plugin.settings.getPreviewDuration();

        if (previewDuration <= 0) {
            sendMessage("Preview started! Repeat the command to stop previewing.", sender);
            PreviewManager.startPreview(player, kit);
        } else {
            sendMessage("Preview started!", sender);
            PreviewManager.startTimedPreview(player, kit, previewDuration);
        }
        return true;
    }

    private void openPreviewMenu(ArenaPlayer player) {
        KitPreviewMenu previewMenu = new KitPreviewMenu(plugin, player, 0);

        previewMenu.showTo(player.getPlayer());
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> results = new LinkedList<>();

        //noinspection SwitchStatementWithTooFewBranches
        switch (args.length - start + 1) {
            case 1:
                for (Kit kit : KitManager.getAllKits().values()) {
                    results.add(kit.getName());
                }
                break;
        }

        return results;
    }
}
