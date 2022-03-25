package wbs.arena.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import wbs.arena.ArenaLobby;
import wbs.arena.ArenaSettings;
import wbs.arena.CombatManager;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;

import java.util.List;

@SuppressWarnings("unused")
public class CommandListener implements Listener {
    private static final ArenaSettings settings = WbsArena.getInstance().settings;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        ArenaPlayer arenaPlayer = ArenaLobby.getPlayerFromArena(player);

        if (arenaPlayer != null) {
            // Add a space to prevent blocking "/arena" when "/a" is blocked, since "/arena" doesn't start with "/a "
            String command = event.getMessage() + " ";

            if (CombatManager.isInCombat(arenaPlayer)) {
                List<String> whitelist = settings.getCommandsWhitelistedInCombat();

                if (!whitelist.isEmpty()) {
                    boolean outsideWhitelist = true;

                    for (String enabled : whitelist) {
                        outsideWhitelist &= !command.startsWith("/" + enabled);
                    }

                    if (outsideWhitelist) {
                        arenaPlayer.sendMessage("&wYou cannot run that command while in combat!");
                        event.setCancelled(true);
                        return;
                    }
                }

                for (String disabled : settings.getCommandsDisabledInCombat()) {
                    if (command.startsWith("/" + disabled)) {
                        arenaPlayer.sendMessage("&wYou cannot run that command while in combat!");
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            List<String> whitelist = settings.getCommandsWhitelistedInArena();
            if (!whitelist.isEmpty()) {
                boolean outsideWhitelist = true;
                for (String enabled : whitelist) {
                    outsideWhitelist &= !command.startsWith("/" + enabled);
                }

                if (outsideWhitelist) {
                    arenaPlayer.sendMessage("&wYou cannot run that command while in the arena!");
                    event.setCancelled(true);
                    return;
                }
            }

            for (String disabled : settings.getCommandsDisabledInArena()) {
                if (command.startsWith("/" + disabled)) {
                    arenaPlayer.sendMessage("&wYou cannot run that command while in the arena!");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
