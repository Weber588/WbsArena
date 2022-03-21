package wbs.arena.menu.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import wbs.arena.arena.Arena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.menu.PlayerSpecificMenu;
import wbs.utils.util.plugin.WbsPlugin;

public class ArenaSpawnpointEditMenu extends PlayerSpecificMenu {
    public ArenaSpawnpointEditMenu(WbsPlugin plugin, ArenaPlayer player, Arena arena) {
        super(plugin, player, "&c&lEdit " + arena.getDisplayName() + " &c&lSpawnpoints", 6, "spawnpointedit");
        setUnregisterOnClose(true);

        int index = 1;
        for (Location spawnpoint : arena.getSpawnpoints()) {
            SpawnpointEditSlot slot =
                    new SpawnpointEditSlot(plugin,
                            Material.ENDER_PEARL,
                            "&b&l" + index,
                            spawnpoint,
                            arena,
                            player);
            setNextFreeSlot(1, 4, 1, 7, slot);
            index++;
        }
    }
}
