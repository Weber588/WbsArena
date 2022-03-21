package wbs.arena.command.kit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaDB;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;

public class KitBuyCommand extends KitSubcommand {
    public KitBuyCommand(@NotNull WbsArena plugin) {
        super(plugin, "buy");
    }

    @Override
    protected boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), arenaPlayer -> KitManager.buy(arenaPlayer, kit, true));
        return true;
    }
}
