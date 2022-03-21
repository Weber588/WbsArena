package wbs.arena.command;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.command.kit.*;
import wbs.arena.data.ArenaDB;
import wbs.arena.kit.KitManager;
import wbs.utils.util.commands.WbsCommand;
import wbs.utils.util.commands.WbsSubcommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class KitCommand extends WbsCommand {

    public static final String KIT_PERMISSION = WbsArena.BASE_PERMISSION + ".kit";

    private static final List<WbsSubcommand> kitSubcommands = new LinkedList<>();

    /**
     * Get a set of global instances used for kit subcommands. Permissions are preset.
     * @param plugin The plugin instance to build with.
     * @return The new or existing list of kit subcommands
     */
    public static List<WbsSubcommand> getKitSubcommands(WbsArena plugin) {
        if (kitSubcommands.isEmpty()) {
            kitSubcommands.addAll(
                    Arrays.asList(
                            new KitCreateCommand(plugin),
                            new KitPreviewCommand(plugin),
                            new KitEditCommand(plugin),
                            new KitGetCommand(plugin),
                            new KitUpdateCommand(plugin),
                            new KitBuyCommand(plugin),
                            new KitDeleteCommand(plugin),
                            new KitListCommand(plugin),
                            new KitInfoCommand(plugin)
                    ));

            for (WbsSubcommand subcommand : kitSubcommands) {
                subcommand.setPermission(KIT_PERMISSION + "." + subcommand.getLabel());
            }
        }

        return Collections.unmodifiableList(kitSubcommands);
    }

    public KitCommand(WbsArena plugin, PluginCommand command) {
        super(plugin, command);

        for (WbsSubcommand kitSubcommand : getKitSubcommands(plugin)) {
            addSubcommand(kitSubcommand);
        }
    }

    @Override
    public boolean onCommandNoArgs(@NotNull CommandSender sender, String label) {
        if (!(sender instanceof Player bukkitPlayer)) {
            return super.onCommandNoArgs(sender, label);
        }

        ArenaDB.getPlayerManager().getAsync(bukkitPlayer.getUniqueId(), KitManager::openKitSelection);
        return true;
    }
}
