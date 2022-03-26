package wbs.arena.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.arena.StatsManager;
import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TopCommand extends WbsSubcommand {
    public TopCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "top");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        String statString = StatsManager.TrackedStat.KILLS.name();

        if (args.length > 1) {
            statString = args[1];
        }

        StatsManager.TrackedStat stat = WbsEnums.getEnumFromString(StatsManager.TrackedStat.class, statString);

        if (stat == null) {
            sendMessage("Invalid stat: " + args[1] + ". Please choose from the following: &h" +
                    WbsEnums.joiningPrettyStrings(StatsManager.TrackedStat.class, ", "), sender);
            return true;
        }

        StatsManager.getTopAsync(stat, (top) -> showTop(top, stat, sender));
        return true;
    }


    private void showTop(List<ArenaPlayer> top, StatsManager.TrackedStat stat, CommandSender sender) {
        sendMessage("Top " + top.size() + " players (" + WbsEnums.toPrettyString(stat) + "):", sender);

        int i = 1;
        for (ArenaPlayer player : top) {
            sendMessage("&6" + (i++) + ") &h" + player.getName() + "&r> &h" + stat.of(player), sender);
        }
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> choices = new LinkedList<>();

        if (args.length == 2) {
            Arrays.stream(StatsManager.TrackedStat.values())
                    .map(WbsEnums::toPrettyString)
                    .map(String::toLowerCase)
                    .forEach(choices::add);
        }

        return choices;
    }
}
