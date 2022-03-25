package wbs.arena.command.arena;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.ArenaLobby;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.arena.ArenaManager;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.WbsCollectionUtil;

import java.util.Map;

public class ArenaJoinCommand extends ArenaSubcommand {
    public ArenaJoinCommand(@NotNull WbsArena plugin) {
        super(plugin, "join");
    }

    @Override
    protected boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), arenaPlayer -> tryJoin(arenaPlayer, label, args, start, arena));
        return true;
    }

    @Override
    protected boolean onArenaCommandNoArgs(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), arenaPlayer -> tryJoin(arenaPlayer, label, args, start, null));
        return true;
    }

    private void tryJoin(ArenaPlayer player, String label, String[] args, int start, @Nullable Arena arena) {
        if (ArenaLobby.isInLobby(player)) {
            if (ArenaLobby.isInArena(player)) {
                sendMessage("You're already in an arena! Do &h/" + label + " leave&r to leave.", player.getPlayer());
                return;
            }
            if (arena == null) {
                Map<String, Arena> arenaMap = ArenaManager.getAllArenas();
                // If there's only one arena, don't force the player to use its name
                if (arenaMap.size() == 1) {
                    String key = arenaMap.keySet().iterator().next();
                    arena = arenaMap.get(key);
                } else {
                    super.onArenaCommandNoArgs(player.getPlayer(), label, args, start);
                    return;
                }
            }
        } else {
            if (ArenaLobby.joinLobby(player)) {
                if (arena == null) {
                    sendMessage("Joined the lobby! Do &h/" + label + " join <arena>&r to start playing!", player.getPlayer());
                    return;
                }
            } else {
                sendMessage("Joined the lobby! Do &h/" + label + " join <arena>&r to start playing!", player.getPlayer());
                return;
            }
        }

        boolean joinedArena = ArenaLobby.joinArena(player, arena);

        if (joinedArena) {
            sendMessage("Joined the arena!", player.getPlayer());
        } else {
            sendMessage("Failed to join arena. Please report this error.", player.getPlayer());
        }
    }
}
