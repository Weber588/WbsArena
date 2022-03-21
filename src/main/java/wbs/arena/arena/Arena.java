package wbs.arena.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.arena.ArenaLobby;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines a region in the world used for fighting
 */
public class Arena {
    private Block blockMin;
    private Block blockMax;

    private final String name;
    private String displayName;

    private final List<Location> spawnpoints = new LinkedList<>();

    public Arena(String name, Block blockMin, Block blockMax) {
        this.name = name;
        displayName = name;
        this.blockMin = blockMin;
        this.blockMax = blockMax;
    }

    public String getName() {
        return name;
    }

    public Block getBlockMin() {
        return blockMin;
    }

    public void setBlockMin(Block blockMin) {
        this.blockMin = blockMin;
    }

    public Block getBlockMax() {
        return blockMax;
    }

    public void setBlockMax(Block blockMax) {
        this.blockMax = blockMax;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void addSpawnpoint(Location loc) {
        spawnpoints.add(loc);
    }

    public void respawn(ArenaPlayer player) {
        Kit kit = player.getCurrentKit();
        kit.giveTo(player);

        if (!spawnpoints.isEmpty()) {
            // TODO: Implement distance from nearby players to select spawnpoints.
            Location spawnpoint = WbsCollectionUtil.getRandom(spawnpoints);

            player.getPlayer().teleport(spawnpoint);
        } else {
            player.sendMessage("No spawnpoints defined for arena " + getName() + "!");
        }
    }

    public YamlConfiguration toConfig(YamlConfiguration config) {
        config.set("min-corner", blockToString(blockMin));
        config.set("max-corner", blockToString(blockMax));
        config.set("display-name", displayName);
        config.set("spawnpoints", spawnpoints);

        return config;
    }

    private String blockToString(Block block) {
        return blockMin.getX() + ", " + blockMin.getY() + ", " + blockMin.getZ() + ", " + blockMin.getWorld().getName();
    }

    public List<Location> getSpawnpoints() {
        return new LinkedList<>(spawnpoints);
    }

    public void removeSpawnpoint(Location spawnpoint) {
        spawnpoints.remove(spawnpoint);
    }
}
