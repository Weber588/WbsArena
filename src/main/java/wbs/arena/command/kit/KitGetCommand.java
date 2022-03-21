package wbs.arena.command.kit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;

public class KitGetCommand extends KitSubcommand {
    public KitGetCommand(@NotNull WbsArena plugin) {
        super(plugin, "get");
    }

    @Override
    protected boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), arenaPlayer -> tryGive(arenaPlayer, kit));
        return true;
    }

    private void tryGive(ArenaPlayer player, Kit kit) {
        if (!KitManager.canUse(player, kit)) {
            player.sendMessage("You don't have access to that kit!");
            return;
        }

        kit.getPlayerState().restoreState(player.getPlayer());
    }
}
