package wbs.arena.data;

import wbs.arena.WbsArena;
import wbs.utils.util.database.WbsDatabase;
import wbs.utils.util.database.WbsField;
import wbs.utils.util.database.WbsFieldType;
import wbs.utils.util.database.WbsTable;

public class ArenaDB {
    private static WbsArena plugin;

    private static PlayerManager playerManager;
    public static PlayerManager getPlayerManager() {
        if (playerManager == null) {
            playerManager = new PlayerManager(plugin);
        }

        return playerManager;
    }

    private static WbsDatabase database;
    public static WbsDatabase getDatabase() {
        return database;
    }

    // Player table

    public static WbsTable playerTable;

    public static final WbsField uuidField = new WbsField("uuid", WbsFieldType.STRING);
    public static final WbsField nameField = new WbsField("name", WbsFieldType.STRING);

    public static final WbsField killsField = new WbsField("kills", WbsFieldType.INT, 0);
    public static final WbsField pointsField = new WbsField("points", WbsFieldType.INT, 0);
    public static final WbsField deathsField = new WbsField("deaths", WbsFieldType.INT, 0);
    public static final WbsField kitField = new WbsField("kit", WbsFieldType.STRING);
    public static final WbsField randomKitField = new WbsField("randomize_kits", WbsFieldType.BOOLEAN);
    public static final WbsField highestKillstreakField = new WbsField("highest_killstreak", WbsFieldType.INT, 0);

    public static void setupDatabase() {
        plugin = WbsArena.getInstance();
        database = new WbsDatabase(plugin, "arena");

        playerTable = new WbsTable(database, "players", uuidField);
    //    playerTable.setDebugMode(true);
        playerTable.addField(
                nameField,

                killsField,
                deathsField,
                pointsField,
                kitField,
                randomKitField
        );

        database.addTable(playerTable);

        if (!database.createDatabase()) {
            return;
        }

        if (database.createTables()) {
            addNewFields();
        }
    }


    /**
     * Add new fields added after the initial run.
     */
    private static void addNewFields() {
        playerTable.addFieldIfNotExists(highestKillstreakField);
    }
}
