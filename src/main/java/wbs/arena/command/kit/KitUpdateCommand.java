package wbs.arena.command.kit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.kit.Kit;
import wbs.arena.menu.kit.ConfirmationMenu;

import java.util.HashMap;
import java.util.Map;

/**
 * Command to update the inventory of a kit. Optionally includes a confirmation menu
 * or repeat command requirement.
 */
public class KitUpdateCommand extends KitSubcommand {
    public KitUpdateCommand(@NotNull WbsArena plugin) {
        super(plugin, "update");
    }

    private final Map<Player, Kit> pendingUpdates = new HashMap<>();

    @Override
    protected boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (!plugin.settings.confirmUpdateCommands()) {
            updateKit(kit, player);
            return true;
        }

        if (plugin.settings.menuConfirmUpdates()) {
            ConfirmationMenu confirmMenu =
                    new ConfirmationMenu(plugin,
                            "&1Update kit to your inventory?",
                            "kitupdate:" + player.getName(),
                            () -> updateKit(kit, player),
                            () -> sendMessage("Update cancelled!", sender));

            confirmMenu.setInfo("&7This will override",
                    "&7kit \"" + kit.getName() + "&7\" with",
                    "&7your current inventory!");

            confirmMenu.showTo(player);
        } else {
            if (pendingUpdates.containsKey(player)) {
                Kit previousKit = pendingUpdates.get(player);

                if (kit.equals(previousKit)) {
                    // Confirmed, update the kit.
                    updateKit(previousKit, player);
                } else {
                    sendMessage("You chose a different kit. Repeat this command to confirm.", sender);
                    pendingUpdates.put(player, kit);
                }
            } else {
                sendMessage("This command will override the kit\"" + kit.getName() + "\" with your current inventory.", sender);
                sendMessage("If you're sure, repeat the command.", sender);
                pendingUpdates.put(player, kit);
            }
        }

        return true;
    }

    private void updateKit(Kit kit, Player player) {
        pendingUpdates.remove(player);
        kit.getPlayerState().captureState(player);
        plugin.settings.saveKitAsync(kit, success -> {
            if (success) {
                sendMessage("Kit updated!", player);
            } else {
                sendMessage("Something went wrong while saving. Please check console.", player);
            }
        });
    }
}
