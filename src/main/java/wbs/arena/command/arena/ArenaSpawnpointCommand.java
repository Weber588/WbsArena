package wbs.arena.command.arena;

import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.command.arena.spawnpoint.*;
import wbs.utils.util.commands.WbsCommandNode;

public class ArenaSpawnpointCommand extends WbsCommandNode {
    public ArenaSpawnpointCommand(@NotNull WbsArena plugin) {
        super(plugin, "spawnpoint");

        addChild(new SpawnpointAddCommand(plugin), getPermission() + ".add");
        addChild(new SpawnpointListCommand(plugin), getPermission() + ".list");
        addChild(new SpawnpointRemoveCommand(plugin), getPermission() + ".remove");
        addChild(new SpawnpointEditCommand(plugin), getPermission() + ".edit");
        addChild(new SpawnpointTpCommand(plugin), getPermission() + ".tp");
    }
}
