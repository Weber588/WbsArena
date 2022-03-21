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

public class SpawnpointRemoveCommand extends ArenaSubcommand {
    public SpawnpointRemoveCommand(@NotNull WbsArena plugin) {
        super(plugin, "remove");
    }

    @Override
    protected boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (args.length <= start) {
            if (sender instanceof Player) {
                sendUsage("<index>&r. Alternatively, use &h/" + label +
                        " spawnpoint list&r to manage spawnpoints automatically.", sender, label, args, args.length);
            } else {
                sendUsage("<index>", sender, label, args, args.length);
            }
            return true;
        }

        String key = args[start];

        List<Location> spawnpoints = arena.getSpawnpoints();

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
                    remove(sender, arena, loc);
                    return true;
                }
            }

            sendMessage("Invalid reference. Has the spawnpoint already been removed?", sender);
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

        Location toRemove = spawnpoints.get(index - 1);
        remove(sender, arena, toRemove);

        return true;
    }

    private void remove(CommandSender sender, Arena arena, Location toRemove) {
        arena.removeSpawnpoint(toRemove);

        plugin.settings.saveArenaAsync(arena, success -> {
            if (success) {
                sendMessage("Spawnpoint removed!", sender);
            } else {
                sendMessage("Location removed: &h" + toRemove.getBlockX() +
                        ", " + toRemove.getBlockY() + ", "+ toRemove.getBlockZ(), sender);
                arena.removeSpawnpoint(toRemove);
            }
        });
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
