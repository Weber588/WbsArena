package wbs.arena;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.arena.arena.Arena;
import wbs.arena.arena.ArenaManager;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.kit.unlock.KitUnlockMethod;
import wbs.arena.kit.unlock.PointThresholdUnlockMethod;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;

public class ArenaSettings extends WbsSettings {
    /**
     * Duration in ticks
     */
    public static final int DEFAULT_PREVIEW_DURATION = 300;

    private final File kitsDir;
    private final File arenasDir;

    private final WbsArena plugin;
    public ArenaSettings(WbsArena plugin) {
        super(plugin);

        this.plugin = plugin;
        kitsDir = new File(plugin.getDataFolder() + File.separator + "kits");
        arenasDir = new File(plugin.getDataFolder() + File.separator + "arenas");
    }

    private YamlConfiguration config;
    private Location lobbyLocation;

    private boolean firstLoad = false;

    @Override
    public void reload() {
        String configName = "config.yml";
        File configFile = new File(this.plugin.getDataFolder(), configName);
        if (!configFile.exists()) {
            plugin.saveResource(configName, false);
            firstLoad = true;
        } else {
            firstLoad = false;
        }

        config = loadDefaultConfig(configName);

        ConfigurationSection options = config.getConfigurationSection("options");

        if (options == null) {
            logger.warning("No options config section found. Default settings will be used.");
            unlockMethod = KitUnlockMethod.getMethod(PointThresholdUnlockMethod.POINT_THRESHOLD_ID);
        } else {
            unlockMethod = KitUnlockMethod.getMethod(
                    options.getString("kit-unlock-method", PointThresholdUnlockMethod.POINT_THRESHOLD_ID));

            ConfigurationSection commandsSection = options.getConfigurationSection("commands");
            if (commandsSection == null) {
                logger.warning("No commands config section found. Default settings will be used.");
            } else {
                previewDuration = (int) (commandsSection.getDouble("preview-duration", previewDuration / 20.0) * 20);
                menuPreviewDuration = (int) (commandsSection.getDouble("menu-preview-duration", menuPreviewDuration / 20.0) * 20);

                confirmUpdateCommands = commandsSection.getBoolean("confirm-update-commands", confirmUpdateCommands);
                menuConfirmUpdates = commandsSection.getBoolean("menu-confirm-updates", menuConfirmUpdates);

                disableDeleteCommand = commandsSection.getBoolean("disable-delete-command", disableDeleteCommand);
                confirmDeleteCommands = commandsSection.getBoolean("confirm-delete-commands", confirmDeleteCommands);
                menuConfirmDeletion = commandsSection.getBoolean("menu-confirm-deletion", menuConfirmDeletion);
            }
        }

        loadKits();
        loadArenas();
        loadLobbyLocation();
    }

    /**
     * Lobby location is saved & loaded from misc.yml, a commentless yml file that can be written to without
     * destroying the formatting of config.yml.
     */
    private void loadLobbyLocation() {
        File miscFile = new File(this.plugin.getDataFolder(), "misc.yml");
        if (!miscFile.exists()) {
            return;
        }

        YamlConfiguration misc = loadConfigSafely(miscFile);
        lobbyLocation = misc.getLocation("lobby-location");
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }
    public boolean setLobbyLocation(Location lobbyLocation) {
        String fileName = "misc.yml";
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.logger.severe(fileName + " failed to create!");
                }
            } catch (IOException e) {
                plugin.logger.severe(fileName + " failed to create!");
                e.printStackTrace();
                return false;
            }

            plugin.logger.info(fileName + " created!");
        }

        YamlConfiguration miscConfig = loadConfigSafely(file);
        miscConfig.set("lobby-location", lobbyLocation);

        try {
            miscConfig.save(file);
        } catch (IOException e) {
            plugin.logger.severe(fileName + " failed to save!");
            e.printStackTrace();
            return false;
        }

        plugin.logger.info("Saved " + fileName + "!");
        this.lobbyLocation = lobbyLocation;
        return true;
    }

    private void loadKits() {
        if (!kitsDir.exists()) {
            boolean kitsDirCreated = kitsDir.mkdirs();

            if (!kitsDirCreated) {
                logError("Failed to load kits. See console.", kitsDir.getName());
                return;
            }
        }

        int errorsBefore = errors.size();

        if (firstLoad) {
            genConfig("kits" + File.separator + "fighter.yml");
            genConfig("kits" + File.separator + "ranger.yml");
        }

        int kitsLoaded = 0;

        for (File kitFile : Objects.requireNonNull(kitsDir.listFiles())) {
            String kitName = kitFile.getName().substring(0, kitFile.getName().lastIndexOf('.'));

            if (kitFile.getName().endsWith(".yml")) {
                YamlConfiguration specs = loadConfigSafely(kitFile);

                try {
                    Kit kit = KitManager.createKit(kitName, specs, kitFile.getName());
                    KitManager.addKit(kit);
                    kitsLoaded++;
                } catch (InternalError ignored) {
                }
            }
        }

        if (errorsBefore - errors.size() != 0) {
            logger.warning("Kits were loaded with " + errors.size() + " error(s). Do /wbsarena errors to view them.");
        }

        if (kitsLoaded >= 0) {
            logger.info(kitsLoaded + " kits loaded.");
        }
    }

    private void loadArenas() {
        if (!arenasDir.exists()) {
            boolean arenasDirCreated = arenasDir.mkdirs();

            if (!arenasDirCreated) {
                logError("Failed to load arenas. See console.", arenasDir.getName());
                return;
            }
        }

        int errorsBefore = errors.size();

        int arenasLoaded = 0;

        for (File arenaFile : Objects.requireNonNull(arenasDir.listFiles())) {
            String arenaName = arenaFile.getName().substring(0, arenaFile.getName().lastIndexOf('.'));

            if (arenaFile.getName().endsWith(".yml")) {
                YamlConfiguration specs = loadConfigSafely(arenaFile);

                try {
                    Arena arena = ArenaManager.createArena(arenaName, specs, arenaFile.getName());
                    ArenaManager.registerArena(arena);
                    arenasLoaded++;
                } catch (InternalError ignored) {
                }
            }
        }

        if (errorsBefore - errors.size() != 0) {
            logger.warning("Arenas were loaded with " + errors.size() + " error(s). Do /wbsarena errors to view them.");
        }

        if (arenasLoaded >= 0) {
            logger.info(arenasLoaded + " arenas loaded.");
        }
    }

    private KitUnlockMethod unlockMethod;
    public KitUnlockMethod getUnlockMethod() {
        return unlockMethod;
    }

    private int previewDuration = DEFAULT_PREVIEW_DURATION;
    public int getPreviewDuration() {
        return previewDuration;
    }

    private int menuPreviewDuration = DEFAULT_PREVIEW_DURATION;
    public int getMenuPreviewDuration() {
        return menuPreviewDuration;
    }

    private boolean confirmUpdateCommands = true;
    public boolean confirmUpdateCommands() {
        return confirmUpdateCommands;
    }

    private boolean menuConfirmUpdates = true;
    public boolean menuConfirmUpdates() {
        return menuConfirmUpdates;
    }

    private boolean disableDeleteCommand = false;
    public boolean disableDeleteCommand() {
        return disableDeleteCommand;
    }

    private boolean confirmDeleteCommands = true;
    public boolean confirmDeleteCommands() {
        return confirmDeleteCommands;
    }

    private boolean menuConfirmDeletion = true;
    public boolean menuConfirmDeletion() {
        return menuConfirmDeletion;
    }

    /**
     * Asynchronously save a kit, returning the save result in the given consumer.
     * @param kit The kit to save
     * @param resultConsumer The consumer that accepts the result of the save
     * @return The id of the created async task.
     */
    public int saveKitAsync(Kit kit, Consumer<Boolean> resultConsumer) {
        return plugin.getAsync(() -> saveKit(kit), resultConsumer);
    }

    /**
     * Saves a kit to the existing file, or creates a new file if it doesn't exist.
     * @param kit The kit to save to the file of the same name in the kits file.
     * @return Whether or not the kit successfully saved.
     */
    public boolean saveKit(Kit kit) {
        String fileName = kit.getName() + ".yml";
        File file = new File(kitsDir, fileName);

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.logger.severe(fileName + " failed to create!");
                }
            } catch (IOException e) {
                plugin.logger.severe(fileName + " failed to create!");
                e.printStackTrace();
                return false;
            }

            plugin.logger.info(fileName + " created!");
        }

        YamlConfiguration kitConfig = loadConfigSafely(file);
        kit.toConfig(kitConfig);

        try {
            kitConfig.save(file);
        } catch (IOException e) {
            plugin.logger.severe(fileName + " failed to save!");
            e.printStackTrace();
            return false;
        }

        plugin.logger.info("Saved " + fileName + "!");
        return true;
    }

    /**
     * Asynchronously delete a kit, returning the result in the given consumer.
     * @param kit The kit to delete
     * @param resultConsumer The consumer that accepts the result of the deletion
     * @return The id of the created async task.
     */
    public int deleteKitAsync(Kit kit, Consumer<Boolean> resultConsumer) {
        return plugin.getAsync(() -> deleteKit(kit), resultConsumer);
    }

    public boolean deleteKit(Kit kit) {
        String fileName = kit.getName() + ".yml";
        File file = new File(kitsDir, fileName);

        plugin.logger.info("Attempting to delete " + fileName + "...");

        if (!file.exists()) {
            plugin.logger.info("File not found.");
            return false;
        }

        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            plugin.logger.info("Failed to delete " + fileName + ".");
            e.printStackTrace();
            return false;
        }

        plugin.logger.info("Successfully deleted " + fileName + ".");
        return true;
    }
    /**
     * Asynchronously save an arena, returning the save result in the given consumer.
     * @param arena The arena to save
     * @param resultConsumer The consumer that accepts the result of the save
     * @return The id of the created async task.
     */
    public int saveArenaAsync(Arena arena, Consumer<Boolean> resultConsumer) {
        return plugin.getAsync(() -> saveArena(arena), resultConsumer);
    }

    /**
     * Saves an arena to the existing file, or creates a new file if it doesn't exist.
     * @param arena The arena to save to the file of the same name in the arena file.
     * @return Whether or not the arena successfully saved.
     */
    public boolean saveArena(Arena arena) {
        String fileName = arena.getName() + ".yml";
        File file = new File(arenasDir, fileName);

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    plugin.logger.severe(fileName + " failed to create!");
                }
            } catch (IOException e) {
                plugin.logger.severe(fileName + " failed to create!");
                e.printStackTrace();
                return false;
            }

            plugin.logger.info(fileName + " created!");
        }

        YamlConfiguration arenaConfig = loadConfigSafely(file);
        arena.toConfig(arenaConfig);

        try {
            arenaConfig.save(file);
        } catch (IOException e) {
            plugin.logger.severe(fileName + " failed to save!");
            e.printStackTrace();
            return false;
        }

        plugin.logger.info("Saved " + fileName + "!");
        return true;
    }
}
