package wbs.arena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaDB;
import wbs.arena.kit.KitManager;
import wbs.utils.util.commands.WbsCommandNode;
import wbs.utils.util.commands.WbsSubcommand;

/**
 * Kit management subcommands
 */
public class KitCommandNode extends WbsCommandNode {

    public KitCommandNode(@NotNull WbsArena plugin) {
        super(plugin, "kit");
        addAlias("kits");

        for (WbsSubcommand kitSubcommand : KitCommand.getKitSubcommands(plugin)) {
            addChild(kitSubcommand);
        }
    }

    @Override
    protected boolean onCommandNoArgs(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player bukkitPlayer)) {
            return super.onCommandNoArgs(sender, label, args, start);
        }

        ArenaDB.getPlayerManager().getAsync(bukkitPlayer.getUniqueId(), KitManager::openKitSelection);
        return true;
    }
}
