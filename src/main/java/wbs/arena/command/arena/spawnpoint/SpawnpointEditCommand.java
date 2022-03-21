package wbs.arena.command.arena.spawnpoint;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.command.arena.ArenaSubcommand;
import wbs.arena.data.ArenaDB;
import wbs.arena.menu.arena.ArenaSpawnpointEditMenu;

public class SpawnpointEditCommand extends ArenaSubcommand {
    public SpawnpointEditCommand(@NotNull WbsArena plugin) {
        super(plugin, "edit");
    }

    @Override
    protected boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players (menu command).", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), arenaPlayer -> {
            ArenaSpawnpointEditMenu editMenu = new ArenaSpawnpointEditMenu(plugin, arenaPlayer, arena);

            editMenu.showTo(arenaPlayer.getPlayer());
        });

        return true;
    }
}
