package wbs.arena;

import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.database.WbsDatabase;
import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public class StatsManager {
    public enum TrackedStat {
        KILLS, DEATHS, POINTS;

        Instant lastRecalculation;

        public WbsField getField() {
            return switch (this) {
                case KILLS -> ArenaDB.killsField;
                case DEATHS -> ArenaDB.deathsField;
                case POINTS -> ArenaDB.pointsField;
            };
        }

        public int of(ArenaPlayer player) {
            return switch (this) {
                case KILLS -> player.getKills();
                case DEATHS -> player.getDeaths();
                case POINTS -> player.getPoints();
            };
        }
    }

    public static final int topListSize = 25;

    private static final Map<TrackedStat, List<ArenaPlayer>> stats = new HashMap<>();

    public static void recalculateAll() {
        WbsArena.getInstance().runAsync(() -> {
            for (TrackedStat stat : TrackedStat.values()) {
                recalculate(stat);
            }
        });
    }

    public static List<ArenaPlayer> recalculate(TrackedStat stat) {
        List<ArenaPlayer> topList = new LinkedList<>();

        String query = "SELECT * FROM " + ArenaDB.playerTable.getName() + " " +
                "ORDER BY " + Objects.requireNonNull(stat.getField()).getFieldName() + " DESC " +
                "LIMIT " + topListSize;

        ArenaDB.getPlayerManager().saveCache();

        WbsDatabase db = ArenaDB.getDatabase();
        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            List<WbsRecord> selected = db.select(statement);

            for (WbsRecord record : selected) {
                topList.add(new ArenaPlayer(record));
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        stats.put(stat, topList);
        stat.lastRecalculation = Instant.now();
        return topList;
    }

    public static List<ArenaPlayer> getTop(TrackedStat stat) {
        List<ArenaPlayer> top = stats.get(stat);

        if (top != null) {
            return top;
        }

        return recalculate(stat);
    }

    public static int getTopAsync(TrackedStat stat, Consumer<List<ArenaPlayer>> callback) {
        List<ArenaPlayer> top = stats.get(stat);

        long ticksSinceLastRecalc;

        if (stat.lastRecalculation != null) {
            ticksSinceLastRecalc = Duration.between(stat.lastRecalculation, Instant.now()).toMillis() * 50;
        } else {
            ticksSinceLastRecalc = Long.MAX_VALUE;
        }

        if (top != null && (ticksSinceLastRecalc < WbsArena.getInstance().settings.leaderboardRefreshRate())) {
            callback.accept(top);
            return -1;
        }

        return WbsArena.getInstance().runAsync(
                () -> recalculate(stat),
                () -> callback.accept(stats.get(stat))
        );
    }

    public static List<ArenaPlayer> getTopCached(TrackedStat stat) {
        return stats.getOrDefault(stat, new LinkedList<>());
    }
}
