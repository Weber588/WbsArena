package wbs.arena.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import wbs.arena.ArenaLobby;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.PreviewManager;

import java.util.Collections;

@SuppressWarnings("unused")
public class MiscListener implements Listener {

    private static final ArenaSettings settings = WbsArena.getInstance().settings;

    @EventHandler
    public void onDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PreviewManager.endPreview(player);

        ArenaPlayer foundArenaPlayer = null;

        ArenaPlayer playerInLobby = ArenaLobby.getPlayerFromLobby(player);
        if (playerInLobby != null) {
            ArenaLobby.leaveArena(playerInLobby);
            ArenaLobby.leaveLobby(playerInLobby);
            foundArenaPlayer = playerInLobby;
        } else {
            ArenaPlayer cachedPlayer = ArenaDB.getPlayerManager().getCached(player.getUniqueId());

            if (cachedPlayer != null) {
                foundArenaPlayer = cachedPlayer;
            }
        }

        if (foundArenaPlayer != null) {
            foundArenaPlayer.resetPlayer();
            if (settings.getSaveMethod() == ArenaSettings.SaveMethod.DISCONNECT) {
                ArenaDB.getPlayerManager().saveAsync(Collections.singleton(foundArenaPlayer));
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!settings.preventDropsInArena()) {
            return;
        }

        Player player = event.getPlayer();

        ArenaPlayer arenaPlayer = ArenaLobby.getPlayerFromArena(player);

        if (arenaPlayer != null) {
            event.setCancelled(true);
            return;
        }

        arenaPlayer = ArenaLobby.getPlayerFromLobby(player);

        if (arenaPlayer != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!settings.preventItemMgmtInArena()) {
            return;
        }

        HumanEntity human = event.getWhoClicked();
        if (human instanceof Player player) {
            ArenaPlayer arenaPlayer = ArenaLobby.getPlayerFromArena(player);

            if (arenaPlayer != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!settings.preventItemDamageInArena()) {
            return;
        }

        ArenaPlayer player = ArenaLobby.getPlayerFromArena(event.getPlayer());

        if (player != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        PreviewManager.endPreview(event.getPlayer());
    }
}
