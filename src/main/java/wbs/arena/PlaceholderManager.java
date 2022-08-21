package wbs.arena;

import org.bukkit.OfflinePlayer;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.pluginhooks.PlaceholderAPIWrapper;

import java.util.List;

public final class PlaceholderManager {
    private PlaceholderManager() {}

    private static final String TOP_KEY = "top";

    private static final String NOT_FOUND = "N/A";

    public static void registerPlaceholders() {
        if (PlaceholderAPIWrapper.isActive()) {
            PlaceholderAPIWrapper.registerSimplePlaceholder(WbsArena.getInstance(), "Weber588", PlaceholderManager::parseParams);
        } else {
            WbsArena.getInstance().logger.info("PlaceholderAPI not found. Placeholders will not be used.");
        }
    }

    private static String parseParams(OfflinePlayer player, String params) {
        String[] args = params.split("_");

        String result = parseTop(player, args);

        if (result != null) return result;

        result = parseMisc(player, args);

        if (result != null) return result;

        return null;
    }

    private static String parseMisc(OfflinePlayer player, String[] args) {
        ArenaPlayer arenaPlayer = null;

        for (ArenaPlayer check : ArenaDB.getPlayerManager().getCache().values()) {
            if (check.getName().equalsIgnoreCase(player.getName())) {
                arenaPlayer = check;
                break;
            }
        }

        if (arenaPlayer == null) return null;

        String prop = String.join("", args).toLowerCase();

        PlayerProperty property = WbsEnums.getEnumFromString(PlayerProperty.class, prop);

        if (property == null) return null;

        return property.getProperty(arenaPlayer);
    }

    private static String parseTop(OfflinePlayer player, String[] args) {
        if (!args[0].equalsIgnoreCase(TOP_KEY)) {
            return null;
        }

        if (args.length < 3) {
            return null;
        }

        String statName = args[1];

        StatsManager.TrackedStat stat = WbsEnums.getEnumFromString(StatsManager.TrackedStat.class, statName);

        if (stat == null) {
            return "[Invalid stat: " + args[1] + "]";
        }

        int place;
        try {
            place = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return "[Invalid place number: " + args[2] + "]";
        }

        if (place <= 0) {
            return "[Invalid place number: " + args[2] + "]";
        }

        PlayerProperty property;
        if (args.length >= 4) {
            property = WbsEnums.getEnumFromString(PlayerProperty.class, args[3]);
            if (property == null) {
                return "[Invalid property: " + args[3] + "]";
            }
        } else {
            property = PlayerProperty.NAME;
        }

        List<ArenaPlayer> top = StatsManager.getTopCached(stat);
        if (top.size() < place) {
            return NOT_FOUND;
        }

        return property.getProperty(top.get(place - 1));
    }

    private enum PlayerProperty {
        NAME,
        KILLS,
        DEATHS,
        KD,
        POINTS,
        KILLSTREAK,
        MAXKILLSTREAK,
        KIT,
        ;

        public String getProperty(ArenaPlayer player) {
            return switch (this) {
                case NAME -> player.getName();
                case KILLS -> player.getKills() + "";
                case DEATHS -> player.getDeaths() + "";
                case KD -> player.getKills() / (double) player.getDeaths() + "";
                case POINTS -> player.getPoints() + "";
                case KILLSTREAK -> player.getCurrentKillstreak() + "";
                case MAXKILLSTREAK -> player.getHighestKillstreak() + "";
                case KIT -> player.getCurrentKit().getName();
            };
        }
    }
}

