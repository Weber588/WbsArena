package wbs.arena.command;

import org.bukkit.command.PluginCommand;
import wbs.arena.WbsArena;
import wbs.arena.command.arena.*;
import wbs.utils.util.commands.WbsCommand;

public class ArenaCommand extends WbsCommand {

    public static final String ARENA_PERMISSION = WbsArena.BASE_PERMISSION + ".arena";

    public ArenaCommand(WbsArena plugin, PluginCommand command) {
        super(plugin, command);

        addSubcommand(new KitCommandNode(plugin), KitCommand.KIT_PERMISSION);

        addSubcommand(new ArenaJoinCommand(plugin), ARENA_PERMISSION + ".join");
        addSubcommand(new ArenaLeaveCommand(plugin), ARENA_PERMISSION + ".leave");

        String adminPermission = WbsArena.BASE_PERMISSION + ".admin";
        addSubcommand(new ArenaReloadCommand(plugin), adminPermission + ".reload");
        addSubcommand(new ArenaErrorCommand(plugin), adminPermission + ".reload");

        String arenaAdminPerm = ARENA_PERMISSION + ".admin";
        addSubcommand(new ArenaCreateCommand(plugin), arenaAdminPerm + ".create");
        addSubcommand(new ArenaEditCommand(plugin), arenaAdminPerm + ".edit");
        addSubcommand(new ArenaSpawnpointCommand(plugin), arenaAdminPerm + ".spawnpoint");
    }
}
