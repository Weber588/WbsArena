package wbs.arena.command.arena.spawnpoint;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.arena.ArenaManager;
import wbs.arena.command.arena.ArenaSubcommand;

import java.util.List;

public class SpawnpointTpCommand extends ArenaSubcommand {
    public SpawnpointTpCommand(@NotNull WbsArena plugin) {
        super(plugin, "tp");
    }

    @Override
    protected boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length <= start) {
            sendUsage("<index>&r. Alternatively, use &h/" + label +
                    " spawnpoint list&r to manage spawnpoints automatically.", sender, label, args, args.length);
            return true;
        }

        List<Location> spawnpoints = arena.getSpawnpoints();

        String key = args[start];

        if (key.equalsIgnoreCase("-h")) {
            if (args.length <= start + 1) {
                sendUsage("-h <hashCode>", sender, label, args, args.length);
                return true;
            }

            String hashCodeString = args[start + 1];
            int hashCode;
            try {
                hashCode = Integer.parseInt(hashCodeString);
            } catch (NumberFormatException e) {
                sendMessage("Invalid hash code: " + hashCodeString, sender);
                return true;
            }

            for (Location loc : spawnpoints) {
                if (loc.hashCode() == hashCode) {
                    teleport(player, loc);
                    return true;
                }
            }

            sendMessage("Invalid reference. Has the spawnpoint been removed?", sender);
            return true;
        }

        int maxIndex = spawnpoints.size();

        int index;
        try {
            index = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            sendMessage("Invalid index: " + key + ". Use an integer between 1 and " + maxIndex, sender);
            return true;
        }

        if (index > maxIndex || index < 1) {
            sendMessage("Invalid index: " + key + ". Use an integer between 1 and " + maxIndex, sender);
            return true;
        }

        Location spawnpoint = spawnpoints.get(index - 1);
        teleport(player, spawnpoint);

        return true;
    }

    private void teleport(Player player, Location spawnpoint) {
        player.teleport(spawnpoint);
        sendMessage("Teleported!", player);
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> choices = super.getTabCompletions(sender, label, args, start);

        switch (args.length - start + 1) {
            case 2:
                Arena arena = ArenaManager.getArena(args[start - 1]);
                if (arena != null) {
                    for (int i = 0; i < arena.getSpawnpoints().size(); i++) {
                        choices.add((i + 1) + "");
                    }
                }
                break;
        }

        return choices;
    }
}
