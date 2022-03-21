package wbs.arena.arena;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.arena.kit.Kit;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.entities.state.SavedEntityState;
import wbs.utils.util.string.WbsStrings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ArenaManager {
    private ArenaManager() {}

    private static final Map<String, Arena> arenas = new HashMap<>();

    public static void registerArena(Arena arena) {
        arenas.put(formatId(arena.getName()), arena);
    }

    public static Arena getArena(String name) {
        return arenas.get(formatId(name));
    }

    private static String formatId(String id) {
        return id.toLowerCase().replace("_", "").replace("-", "");
    }

    public static Map<String, Arena> getAllArenas() {
        return arenas;
    }

    public static Arena createArena(String arenaName, YamlConfiguration specs, String name) {
        ArenaSettings settings = WbsArena.getInstance().settings;

        String displayName = specs.getString("display-name", arenaName);

        String blockStringMin = specs.getString("min-corner");
        String blockStringMax = specs.getString("max-corner");
        @SuppressWarnings("unchecked")
        List<Location> locationList = (List<Location>) specs.getList("spawnpoints", new LinkedList<>());

        Block blockMin = blockFromString(blockStringMin);
        Block blockMax = blockFromString(blockStringMax);

        Arena arena = new Arena(arenaName, blockMin, blockMax);

        arena.setDisplayName(displayName);
        for (Location loc : locationList) {
            arena.addSpawnpoint(loc);
        }

        return arena;
    }

    private static Block blockFromString(String blockString) {
        if (blockString == null) {
            throw new IllegalArgumentException("blockString may not be null.");
        }

        String[] args = blockString.split(", ");
        if (args.length != 4) {
            throw new IllegalArgumentException("Malformed block string (requires 4 args; x, y, z, world): " + blockString);
        }

        World world = Bukkit.getWorld(args[3]);
        if (world == null) {
            throw new IllegalArgumentException("Malformed block string (world not found; \"" + args[3] + "\"): " + blockString);
        }

        int x, y, z;
        try {
            x = Integer.parseInt(args[0]);
            y = Integer.parseInt(args[1]);
            z = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed block string (invalid co-ordinates; \""
                    + WbsStrings.combineFirst(args, 3, ", ") + "\"): " + blockString);
        }

        return world.getBlockAt(x, y, z);
    }

}
