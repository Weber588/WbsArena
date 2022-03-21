package wbs.arena.command.kit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.menu.kit.ConfirmationMenu;

import java.util.HashMap;
import java.util.Map;

public class KitDeleteCommand extends KitSubcommand {
    public KitDeleteCommand(@NotNull WbsArena plugin) {
        super(plugin, "delete");
    }

    private final Map<CommandSender, Kit> pendingDeletions = new HashMap<>();

    @Override
    protected boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit) {
        if (plugin.settings.disableDeleteCommand()) {
            sendMessage("&wThis command is disabled in the config.", sender);
            return true;
        }

        if (!plugin.settings.confirmDeleteCommands()) {
            deleteKit(kit, sender);
            return true;
        }


        if (plugin.settings.menuConfirmDeletion() && sender instanceof Player player) {
            ConfirmationMenu confirmMenu =
                    new ConfirmationMenu(plugin,
                            "&4Delete kit permanently?",
                            "kitdelete:" + player.getName(),
                            () -> deleteKit(kit, player),
                            () -> sendMessage("Deletion cancelled!", sender));

            confirmMenu.setInfo("&7This will &4permanently",
                    "&7delete kit \"" + kit.getName() + "&7\"!");

            confirmMenu.showTo(player);
        } else {
            if (pendingDeletions.containsKey(sender)) {
                Kit previousKit = pendingDeletions.get(sender);

                if (kit.equals(previousKit)) {
                    // Confirmed, delete the kit.
                    deleteKit(previousKit, sender);
                } else {
                    sendMessage("You chose a different kit. Repeat this command to confirm.", sender);
                    pendingDeletions.put(sender, kit);
                }
            } else {
                sendMessage("&wThis command will permanently delete the kit\"" + kit.getName() + "\"!", sender);
                sendMessage("If you're sure, repeat the command.", sender);
                pendingDeletions.put(sender, kit);
            }
        }

        return true;
    }

    private void deleteKit(Kit kit, CommandSender sender) {
        pendingDeletions.remove(sender);

        plugin.settings.deleteKitAsync(kit, deleted -> {
            if (deleted) {
                sendMessage("Kit deleted successfully!", sender);
                KitManager.removeKit(kit);
            } else {
                sendMessage("&wFailed to delete kit. See console for details.", sender);
            }
        });
    }
}
