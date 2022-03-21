package wbs.arena.command.arena.spawnpoint;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.command.arena.ArenaSubcommand;

public class SpawnpointListCommand extends ArenaSubcommand {
    public SpawnpointListCommand(@NotNull WbsArena plugin) {
        super(plugin, "list");
    }

    @Override
    protected boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        sendMessage("Spawnpoints for &h" + arena.getName() + "&r: ", sender);

        for (Location spawnpoint : arena.getSpawnpoints()) {
            String locString = Math.round(spawnpoint.getX()) + ", " +
                    Math.round(spawnpoint.getY()) + ", " +
                    Math.round(spawnpoint.getZ());

            plugin.buildMessage("&b" + locString, sender)
                    .append(" &6&l[TP]")
                        .addClickCommand("/wbsarena spawnpoint tp " + arena.getName() + " -h " + spawnpoint.hashCode())
                        .addHoverText("&7Click to tp!")
                    .append(" &4&l[DELETE]")
                        .addClickCommand("/wbsarena spawnpoint remove " + arena.getName() + " -h " + spawnpoint.hashCode())
                        .addHoverText("&cClick to delete!")
                    .send();
        }

        return true;
    }
}
