package wbs.arena.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PointsCommand extends WbsSubcommand {
    public PointsCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "points");
    }

    private enum PointsArg {
        GIVE, TAKE, SET
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length < 2) {
            sendUsage("<" + WbsEnums.joiningPrettyStrings(PointsArg.class, "|") + "> <player> <points>", sender, label, args);
            return true;
        }

        PointsArg arg = WbsEnums.getEnumFromString(PointsArg.class, args[1]);
        if (arg == null) {
            sendMessage("Invalid arg: " + args[1] + "&r. Please choose from the following: "
                    + WbsEnums.joiningPrettyStrings(PointsArg.class, ", "), sender);
            return true;
        }

        if (args.length < 3) {
            sendUsage("<player> <points>", sender, label, args);
            return true;
        }

        String playerString = args[2];

        if (args.length < 4) {
            sendUsage("<points>", sender, label, args);
            return true;
        }

        String pointsArg = args[3];
        int points;
        try {
            points = Integer.parseInt(pointsArg);
        } catch (NumberFormatException e) {
            sendMessage("Invalid points: " + pointsArg + "&r. Please use an integer.", sender);
            return true;
        }

        UUID playerUUID = null;
        Player online = Bukkit.getPlayer(playerString);
        if (online != null) {
            playerUUID = online.getUniqueId();
        }

        if (playerUUID != null) {
            ArenaDB.getPlayerManager().getAsync(playerUUID, player -> apply(player, arg, points, sender));
        } else {
            ArenaDB.getPlayerManager().getUUIDsAsync(playerString, uuids -> findUUIDs(uuids, arg, points, playerString, sender));
        }

        return true;
    }

    private void findUUIDs(List<UUID> uuids, PointsArg arg, int points, String playerString, CommandSender sender) {
        if (uuids.isEmpty()) {
            sendMessage("Player not found: " + playerString, sender);
        } else if (uuids.size() == 1) {
            ArenaDB.getPlayerManager().getAsync(uuids.get(0), player -> apply(player, arg, points, sender));
        } else {
            sendMessage("Duplicate UUIDs for username &h" + playerString + "&r found. Please choose from the following: ", sender);
            String commandTemplate = "/wbsarena points " + arg.name().toLowerCase() + " ";
            int index = 1;
            for (UUID uuid : uuids) {
                String command = commandTemplate + uuid.toString() + " " + points;
                plugin.buildMessageNoPrefix(index + ") " + uuid)
                        .setFormatting("&h")
                        .addHoverText("&6Click to run &h" + command)
                        .addClickCommand(command)
                        .send(sender);
                index++;
            }
        }
    }

    private void apply(ArenaPlayer player, PointsArg arg, int points, CommandSender sender) {
        switch (arg) {
            case GIVE -> {
                player.addPoints(points);
                sendMessage("Gave " + player.getName() + " " + points + " points. New total: " + player.getPoints(), sender);
            }
            case TAKE -> {
                player.addPoints(-points);
                sendMessage("Took " + points + " points from " + player.getName() + ". New total: " + player.getPoints(), sender);
            }
            case SET -> {
                player.setPoints(points);
                sendMessage("Set " + player.getName() + "'s points to " + points + ".", sender);
            }
        }
        player.refreshScoreboard();
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        return switch (args.length) {
            case 2 -> WbsEnums.toStringList(PointsArg.class);
            case 3 -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            default -> null;
        };
    }
}
