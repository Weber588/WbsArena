package wbs.arena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class StatsCommand extends WbsSubcommand {
    public StatsCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "stats");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players!", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), this::showStats);

        return true;
    }

    private void showStats(ArenaPlayer arenaPlayer) {
        for (String line : arenaPlayer.getStatsStrings()) {
            arenaPlayer.sendMessage(line);
        }
    }
}
