package wbs.arena.command.kit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.utils.util.string.WbsStrings;

public class KitInfoCommand extends KitSubcommand {
    public KitInfoCommand(@NotNull WbsArena plugin) {
        super(plugin, "info");
    }

    @Override
    protected boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit) {
        String commandBeforeInfo = WbsStrings.combineFirst(args, start - 2, " ");
        if (start > 2) {
            commandBeforeInfo += " ";
        }
        String editCommand = "/" + label + " " + commandBeforeInfo + "edit " + kit.getName();

        if (sender instanceof Player player) {
            ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), arenaPlayer -> showInfo(arenaPlayer, sender, kit, editCommand));
        } else {
            showInfo(null, sender, kit, editCommand);
        }

        return true;
    }

    private void showInfo(@Nullable ArenaPlayer player, CommandSender sender, Kit kit, String editCommand) {
        sendMessage("&6Kit: &c" + kit.getName(), sender);

        for (String line : kit.getInfoDisplay(player)) {
            sendMessageNoPrefix("    " + line, sender);
        }

        plugin.buildMessage("To edit this kit, do &h" + editCommand)
                .addHoverText("&7Click to edit!")
                .addClickCommandSuggestion(editCommand + " ")
                .send(sender);

    }
}
