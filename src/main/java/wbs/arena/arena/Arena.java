package wbs.arena.arena;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import wbs.arena.ArenaLobby;
import wbs.arena.CombatManager;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEntities;
import wbs.utils.util.entities.WbsPlayerUtil;
import wbs.utils.util.entities.state.SavedPlayerState;
import wbs.utils.util.entities.state.tracker.*;
import wbs.utils.util.string.WbsStrings;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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

    private static SavedPlayerState respawnState;
    private static SavedPlayerState getRespawnState() {
        if (respawnState == null) {
            respawnState = new SavedPlayerState();
            respawnState.track(new HungerState())
                    .track(new SaturationState())
                    .track(new HealthState())
                    .track(new PotionEffectsState())
                    .track(new GameModeState(GameMode.ADVENTURE))
                    .track(new FireTicksState());
        }
        respawnState.trackAll();

        return respawnState;
    }

    public void respawn(ArenaPlayer player) {
        Kit kit = player.getCurrentKit();
        kit.giveTo(player);

        // TODO: Move this elsewhere for when more than just respawning triggers it (such as leaving the arena)
        if (WbsArena.getInstance().settings.deleteProjectilesOnDeath()) {
            CombatManager.getRegisteredProjectiles(player).forEach(Entity::remove);
        } else if (WbsArena.getInstance().settings.deleteTridentsOnDeath()) {
            CombatManager.getRegisteredProjectiles(player)
                    .stream()
                    .filter(proj -> proj instanceof Trident)
                    .forEach(Entity::remove);
        }

        if (!spawnpoints.isEmpty()) {
            // TODO: Implement distance from nearby players to select spawnpoints.
            Location spawnpoint = WbsCollectionUtil.getRandom(spawnpoints);

            player.getPlayer().teleport(spawnpoint);
        } else {
            player.sendMessage("No spawnpoints defined for arena " + getName() + "!");
        }

        getRespawnState().restoreState(player.getPlayer());
    }

    public YamlConfiguration toConfig(YamlConfiguration config) {
        config.set("min-corner", blockToString(blockMin));
        config.set("max-corner", blockToString(blockMax));
        config.set("display-name", displayName);
        config.set("spawnpoints", spawnpoints);

        return config;
    }

    private String blockToString(Block block) {
        return block.getX() + ", " + block.getY() + ", " + block.getZ() + ", " + block.getWorld().getName();
    }

    public List<Location> getSpawnpoints() {
        return new LinkedList<>(spawnpoints);
    }

    public void removeSpawnpoint(Location spawnpoint) {
        spawnpoints.remove(spawnpoint);
    }
}
