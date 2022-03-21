package wbs.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import wbs.arena.arena.Arena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.utils.util.entities.state.SavedPlayerState;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to track players in the lobby
 */
public final class ArenaLobby {
    private ArenaLobby() {}

    private static final Map<ArenaPlayer, SavedPlayerState<Player>> playersInLobby = new HashMap<>();
    private static final Map<ArenaPlayer, Arena> currentArenas = new HashMap<>();

    /**
     * Make the given player join the lobby, saving their player state
     * before joining.
     * @param player The player to make join.
     * @return Whether or not the player joined successfully.
     */
    public static boolean joinLobby(ArenaPlayer player) {
        if (playersInLobby.containsKey(player)) {
            return false;
        }

        SavedPlayerState<Player> playerState = new SavedPlayerState<>();
        playerState.trackAll();

        playerState.captureState(player.getPlayer());

        playersInLobby.put(player, playerState);
        return true;
    }

    /**
     * Make the player leave the lobby,
     * @param player The player to leave.
     * @return Whether or not the player joined successfully.
     */
    public static boolean leaveLobby(ArenaPlayer player) {
        if (!playersInLobby.containsKey(player)) {
            return false;
        }

        SavedPlayerState<Player> playerState = playersInLobby.get(player);
        playersInLobby.remove(player);

        playerState.restoreState(player.getPlayer());

        return true;
    }

    public static boolean joinArena(ArenaPlayer player, Arena arena) {
        if (!isInLobby(player)) {
            boolean joinedLobby = joinLobby(player);
            if (!joinedLobby) {
                return false;
            }
        }

        // TODO: restore lobby state

        currentArenas.put(player, arena);
        arena.respawn(player);
        return true;
    }

    public static boolean leaveArena(ArenaPlayer player) {
        Arena arena = currentArenas.get(player);

        if (arena == null) {
            return false;
        }

        Location lobbyLocation = WbsArena.getInstance().settings.getLobbyLocation();

        if (lobbyLocation == null) {
            player.sendMessage("Lobby location not set.");
        } else {
            player.getPlayer().teleport(lobbyLocation);
        }

        currentArenas.remove(player);
        return true;
    }

    @Nullable
    public static Arena getCurrentArena(ArenaPlayer player) {
        return currentArenas.get(player);
    }

    public static boolean isInLobby(ArenaPlayer player) {
        return playersInLobby.containsKey(player);
    }
}
