package wbs.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.arena.arena.Arena;
import wbs.arena.arena.ArenaManager;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.kit.unlock.KitUnlockMethod;
import wbs.arena.kit.unlock.PointThresholdUnlockMethod;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class ArenaSettings extends WbsSettings {
    public static final int DEFAULT_PREVIEW_DURATION = 300;
    public static final int DEFAULT_COMBAT_TAG_DURATION = 60;

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

        loadOptions("config.yml");

        loadKits();
        loadArenas();
        loadLobbyLocation();
    }

    private void loadOptions(String directory) {
        ConfigurationSection options = config.getConfigurationSection("options");
        directory += "/options";

        if (options == null) {
            logger.warning("No options config section found. Default settings will be used.");
            unlockMethod = KitUnlockMethod.getMethod(PointThresholdUnlockMethod.POINT_THRESHOLD_ID);
        } else {
            String kitUnlockString = options.getString("kit-unlock-method");
            unlockMethod = KitUnlockMethod.getMethod(kitUnlockString);
            if (unlockMethod == null) {
                logError("Invalid kit unlock method: " + kitUnlockString, directory + "/kit-unlock-method");
                unlockMethod = KitUnlockMethod.getMethod(PointThresholdUnlockMethod.POINT_THRESHOLD_ID);
            }

            leaderboardRefreshRate = (int)
                    (options.getDouble("leaderboard-refresh-rate", leaderboardRefreshRate / (20.0 * 60.0)) * 20.0 * 60.0);

            String saveMethodString = options.getString("save-method", saveMethod.name());
            saveMethod = WbsEnums.getEnumFromString(SaveMethod.class, saveMethodString);
            if (saveMethod == null) {
                saveMethod = SaveMethod.INSTANT;
                logError("Invalid save method: " + saveMethodString, directory + "/save-method");
            }

            saveFrequency = (int)
                    (options.getDouble("save-frequency", saveFrequency / (20.0 * 60.0)) * 20.0 * 60.0);

            if (saveMethod == SaveMethod.PERIODIC) {
                ArenaDB.getPlayerManager().startSaveTimer();
            } else {
                // Cancel in case the save method was previously PERIODIC and this was just a reload
                ArenaDB.getPlayerManager().cancelSaveTimer();
            }

            minimumPoints = options.getInt("minimum-points", Integer.MIN_VALUE);

            loadCommandOptions(options, directory);
            loadArenaOptions(options, directory);
        }
    }

    private void loadArenaOptions(ConfigurationSection options, String directory) {
        ConfigurationSection arenaSection = options.getConfigurationSection("arena");
        directory += "/arena";
        if (arenaSection == null) {
            logger.warning("No arena config section found. Default settings will be used.");
        } else {
            loadCombatOptions(arenaSection, directory);

            despawnProjectiles = arenaSection.getBoolean("despawn-projectiles", despawnProjectiles);

            commandsDisabledInArena = arenaSection.getStringList("disabled-commands");
            commandsWhitelistedInArena = arenaSection.getStringList("allowed-commands");

            preventDropsInArena = arenaSection.getBoolean("prevent-drops", preventDropsInArena);

            killFormat = arenaSection.getString("kill-message-format", killFormat);
            deathFormat = arenaSection.getString("death-message-format", deathFormat);

            killPoints = arenaSection.getInt("points-per-kill", killPoints);
            deathPoints = arenaSection.getInt("points-per-death", deathPoints);

            loadKillRewards(arenaSection, directory);

            ConfigurationSection damageCauseSection = arenaSection.getConfigurationSection("prevent-damage");
            if (damageCauseSection != null) {
                for (String key : damageCauseSection.getKeys(false)) {
                    DamageCause cause = WbsEnums.getEnumFromString(DamageCause.class, key);
                    if (cause == null) {
                        logError("Invalid damage cause: " + key, directory + "/prevent-damage");
                        continue;
                    }

                    if (damageCauseSection.getBoolean(key, false)) {
                        damageIgnoredInArena.add(cause);
                    }
                }
            }

            preventItemMgmtInArena = arenaSection.getBoolean("prevent-inventory-management", preventItemMgmtInArena);
            preventItemDamageInArena = arenaSection.getBoolean("prevent-item-damage", preventItemDamageInArena);

            deleteProjectilesOnDeath = arenaSection.getBoolean("delete-projectiles-on-death", deleteProjectilesOnDeath);
            deleteTridentsOnDeath = arenaSection.getBoolean("delete-tridents-on-death", deleteTridentsOnDeath);

            leaveOnDeath = arenaSection.getBoolean("leave-on-death", leaveOnDeath);
        }
    }

    private void loadKillRewards(ConfigurationSection arenaSection, String directory) {
        ConfigurationSection rewardsSection = arenaSection.getConfigurationSection("kill-rewards");
        directory += "/kill-rewards";

        if (rewardsSection == null) {
            logger.warning("No kill rewards section found. Default settings will be used.");
        } else {
            ConfigurationSection itemsSection = rewardsSection.getConfigurationSection("items");
            String itemDirectory = directory + "/items";

            if (itemsSection != null) {
                for (String itemKey : itemsSection.getKeys(false)) {
                    if (itemsSection.isConfigurationSection(itemKey)) {
                        ItemStack item = itemsSection.getItemStack(itemKey);

                        if (item != null) {
                            killRewards.add(item);
                            continue;
                        }
                    }

                    Material material = WbsEnums.materialFromString(itemKey);
                    if (material == null) {
                        logError("Invalid item: " + itemKey, itemDirectory + "/");
                    }
                }
            }

            ConfigurationSection potionSection = rewardsSection.getConfigurationSection("potions");
            String potionDirectory = directory + "/potions";

            if (potionSection != null) {
                for (String potionKey : potionSection.getKeys(false)) {
                    ConfigurationSection potionInstance = potionSection.getConfigurationSection(potionKey);
                    if (potionInstance == null) {
                        logError("Potion rewards must be .", potionDirectory + "/" + potionKey);
                        continue;
                    }

                    PotionEffectType type = PotionEffectType.getByName(potionKey);

                    if (type == null) {
                        logError("Invalid potion type: " + potionKey, potionDirectory);
                        continue;
                    }

                    int duration = (int) (potionInstance.getDouble("duration", 3.0 / 20) * 20);
                    int level = potionInstance.getInt("level", 1) - 1;

                    PotionEffect effect = new PotionEffect(type, duration, level, true, false, true);

                    killPotionRewards.add(effect);
                }
            }
        }
    }

    private void loadCombatOptions(ConfigurationSection arenaSection, String directory) {
        ConfigurationSection combatTaggingSection = arenaSection.getConfigurationSection("combat-tagging");
        directory += "/combat-tagging";
        if (combatTaggingSection == null) {
            logger.warning("No combat tagging config section found. Default settings will be used.");
        } else {
            combatTagDuration = (int) (combatTaggingSection.getDouble("combat-tag-duration", combatTagDuration / 20.0) * 20);

            commandsDisabledInCombat = combatTaggingSection.getStringList("disabled-commands");
            commandsWhitelistedInCombat = combatTaggingSection.getStringList("allowed-commands");
        }
    }

    private void loadCommandOptions(@NotNull ConfigurationSection options, String directory) {
        ConfigurationSection commandsSection = options.getConfigurationSection("commands");
        directory += "/commands";

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

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    // region options
    private KitUnlockMethod unlockMethod;
    public KitUnlockMethod getUnlockMethod() {
        return unlockMethod;
    }

    private int combatTagDuration = DEFAULT_COMBAT_TAG_DURATION;
    public int getCombatTagDuration() {
        return combatTagDuration;
    }

    private List<String> commandsDisabledInArena;
    public List<String> getCommandsDisabledInArena() {
        return new LinkedList<>(commandsDisabledInArena);
    }

    private List<String> commandsDisabledInCombat;
    public List<String> getCommandsDisabledInCombat() {
        return new LinkedList<>(commandsDisabledInCombat);
    }

    private List<String> commandsWhitelistedInArena;
    public List<String> getCommandsWhitelistedInArena() {
        return new LinkedList<>(commandsWhitelistedInArena);
    }

    private List<String> commandsWhitelistedInCombat;
    public List<String> getCommandsWhitelistedInCombat() {
        return new LinkedList<>(commandsWhitelistedInCombat);
    }

    private boolean despawnProjectiles = true;
    public boolean despawnProjectiles() {
        return despawnProjectiles;
    }

    private boolean preventDropsInArena = false;
    public boolean preventDropsInArena() {
        return preventDropsInArena;
    }

    private boolean leaveOnDeath = true;
    public boolean leaveOnDeath() {
        return leaveOnDeath;
    }

    private final List<EntityDamageEvent.DamageCause> damageIgnoredInArena = new LinkedList<>();
    public boolean preventDamageInArena(EntityDamageEvent.DamageCause cause) {
        return damageIgnoredInArena.contains(cause);
    }

    private boolean preventItemMgmtInArena = false;
    public boolean preventItemMgmtInArena() {
        return preventItemMgmtInArena;
    }

    private boolean preventItemDamageInArena = true;
    public boolean preventItemDamageInArena() {
        return preventItemDamageInArena;
    }

    private boolean deleteProjectilesOnDeath = true;
    public boolean deleteProjectilesOnDeath() {
        return deleteProjectilesOnDeath;
    }

    private boolean deleteTridentsOnDeath = true;
    public boolean deleteTridentsOnDeath() {
        return deleteTridentsOnDeath;
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

    private String killFormat = "%victim% was slain by %attacker%!";
    public String getKillMessage(@NotNull ArenaPlayer attacker, @NotNull ArenaPlayer victim) {
        return killFormat.replace("%attacker", attacker.getName())
                .replace("%victim%", victim.getName());
    }

    private String deathFormat = "%victim% died!";
    public String getDeathMessage(@NotNull ArenaPlayer victim) {
        return deathFormat.replace("%victim%", victim.getName());
    }

    private int killPoints = 2;
    public int getKillPoints() {
        return killPoints;
    }

    private int leaderboardRefreshRate = 2 * 20 * 60;
    public int leaderboardRefreshRate() {
        return leaderboardRefreshRate;
    }

    private SaveMethod saveMethod = SaveMethod.INSTANT;
    public SaveMethod getSaveMethod() {
        return saveMethod;
    }

    private int saveFrequency = 5 * 20 * 60;
    public int saveFrequency() {
        return saveFrequency;
    }

    private int minimumPoints = 0;
    public int getMinimumPoints() {
        return minimumPoints;
    }

    private int deathPoints = 1;
    public int getDeathPoints() {
        return deathPoints;
    }

    private final List<ItemStack> killRewards = new LinkedList<>();
    public List<ItemStack> getKillRewards() {
        return new LinkedList<>(killRewards);
    }

    private final List<PotionEffect> killPotionRewards = new LinkedList<>();
    public List<PotionEffect> getKillPotionRewards() {
        return new LinkedList<>(killPotionRewards);
    }

    // endregion

    // region Kit, Arena, Lobby loading
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

    private void loadKits() {
        if (!kitsDir.exists()) {
            boolean kitsDirCreated = kitsDir.mkdirs();

            if (!kitsDirCreated) {
                logError("Failed to load kits. See console.", kitsDir.getName());
                return;
            }
        }

        KitManager.clear();

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
    // endregion

    // region Kit, Arena, Lobby saving
    public int setLobbyLocationAsync(Location location, Consumer<Boolean> resultConsumer) {
        return plugin.getAsync(() -> setLobbyLocation(location), resultConsumer);
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

    // endregion

    public enum SaveMethod {
        INSTANT,
        PERIODIC,
        DISCONNECT
    }
}
