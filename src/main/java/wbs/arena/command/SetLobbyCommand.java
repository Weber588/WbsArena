package wbs.arena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.utils.util.commands.WbsSubcommand;

public class SetLobbyCommand extends WbsSubcommand {

    private final WbsArena plugin;

    public SetLobbyCommand(@NotNull WbsArena plugin) {
        super(plugin, "setlobby");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        plugin.settings.setLobbyLocationAsync(player.getLocation(), saved -> {
            if (saved) {
                sendMessage("Lobby location set!", sender);
            } else {
                sendMessage("Failed to save location. See console for errors.", sender);
            }
        });

        return true;
    }
}
