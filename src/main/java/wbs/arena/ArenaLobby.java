package wbs.arena;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.arena.Arena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.utils.util.entities.state.SavedPlayerState;
import wbs.utils.util.entities.state.tracker.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to track players in the lobby
 */
public final class ArenaLobby {
    private ArenaLobby() {}

    private static final Map<ArenaPlayer, SavedPlayerState<Player>> playersInLobby = new HashMap<>();
    private static final Map<ArenaPlayer, Arena> currentArenas = new HashMap<>();

    private static SavedPlayerState<Player> lobbyState;

    @NotNull
    public static SavedPlayerState<Player> getLobbyState() {
        if (lobbyState == null) {
            lobbyState = new SavedPlayerState<>();

            // TODO: Build lobby inventory with hotkey items
            lobbyState.track(new InventoryState(new ItemStack[0], 0));

            lobbyState.track(new LocationState(WbsArena.getInstance().settings.getLobbyLocation()));

            lobbyState.track(new HealthState())
                    .track(new HungerState())
                    .track(new SaturationState())
                    .track(new GameModeState(GameMode.ADVENTURE));
        }

        return lobbyState;
    }

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

        getLobbyState().restoreState(player.getPlayer());

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
        }

        getLobbyState().restoreState(player.getPlayer());

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

    public static boolean isInLobby(Player player) {
        return getPlayerFromLobby(player) != null;
    }

    public static ArenaPlayer getPlayerFromLobby(Player player) {
        for (ArenaPlayer arenaPlayer : playersInLobby.keySet()) {
            if (arenaPlayer.getUUID().equals(player.getUniqueId())) {
                return arenaPlayer;
            }
        }

        return null;
    }

    public static ArenaPlayer getPlayerFromArena(Player player) {
        for (ArenaPlayer arenaPlayer : currentArenas.keySet()) {
            if (arenaPlayer.getUUID().equals(player.getUniqueId())) {
                return arenaPlayer;
            }
        }

        return null;
    }

    public static void broadcastLobby(String message) {

    }

    public static boolean broadcastArena(String message, ArenaPlayer player) {
        Arena arena = getCurrentArena(player);
        if (arena == null) {
            return false;
        }
        broadcastArena(message, arena);
        return true;
    }

    public static void broadcastArena(String message, @NotNull Arena arena) {
        for (ArenaPlayer player : currentArenas.keySet()) {
            if (arena.equals(currentArenas.get(player))) {
                player.sendMessageNoPrefix(message);
            }
        }
    }

    public static void clear() {
        Map<ArenaPlayer, SavedPlayerState<Player>> playersInLobby = new HashMap<>(ArenaLobby.playersInLobby);
        Map<ArenaPlayer, Arena> currentArenas = new HashMap<>(ArenaLobby.currentArenas);

        for (ArenaPlayer player : currentArenas.keySet()) {
            leaveArena(player);
        }

        for (ArenaPlayer player : playersInLobby.keySet()) {
            leaveLobby(player);
        }
    }
}
