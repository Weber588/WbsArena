package wbs.arena.command.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.ArenaLobby;
import wbs.arena.arena.Arena;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaLeaveCommand extends WbsSubcommand {
    public ArenaLeaveCommand(@NotNull WbsPlugin plugin) {
        super(plugin, "leave");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), this::tryLeave);
        return true;
    }

    private void tryLeave(ArenaPlayer player) {
        if (ArenaLobby.isInLobby(player)) {
            Arena arena = ArenaLobby.getCurrentArena(player);
            if (arena == null) {
                ArenaLobby.leaveLobby(player);
            } else {
                ArenaLobby.leaveArena(player);
            }
        } else {
            sendMessage("You're not in the game!", player.getPlayer());
        }
    }
}
