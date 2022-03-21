package wbs.arena.command.arena.spawnpoint;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.command.arena.ArenaSubcommand;

public class SpawnpointAddCommand extends ArenaSubcommand {
    public SpawnpointAddCommand(@NotNull WbsArena plugin) {
        super(plugin, "add");
    }

    @Override
    protected boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players", sender);
            return true;
        }

        Location location = player.getLocation();
        arena.addSpawnpoint(location);

        plugin.settings.saveArenaAsync(arena, success -> {
            if (success) {
                sendMessage("Spawnpoint added!", player);
            } else {
                sendMessage("&wFailed to save. Please report this error.", player);
                arena.removeSpawnpoint(location);
            }
        });

        return true;
    }
}
