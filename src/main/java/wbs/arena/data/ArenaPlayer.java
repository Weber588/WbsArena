package wbs.arena.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.ArenaLobby;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.utils.util.WbsMath;
import wbs.utils.util.WbsScoreboard;
import wbs.utils.util.database.RecordProducer;
import wbs.utils.util.database.WbsRecord;

import java.util.*;

public class ArenaPlayer implements RecordProducer {

    private final UUID uuid;
    private String name = "Unknown";

    private Player player;

    private int kills = 0;
    private int deaths = 0;
    private int points = 0;

    private int currentKillstreak = 0;
    private int highestKillstreak = 0;

    private boolean pendingSave = false;

    private Kit kit;
    private boolean randomKitEnabled;

    private WbsScoreboard statsScoreboard;

    public ArenaPlayer(UUID uuid) {
        this.uuid = uuid;

        tryGetPlayer();

        if (player != null) {
            chooseRandomKit();
        }
    }

    public ArenaPlayer(WbsRecord record) {
        uuid = UUID.fromString(record.getValue(ArenaDB.uuidField, String.class));
        name = record.getValue(ArenaDB.nameField, String.class);

        tryGetPlayer();

        kills = record.getOrDefault(ArenaDB.killsField, Integer.class);
        deaths = record.getOrDefault(ArenaDB.deathsField, Integer.class);
        points = record.getOrDefault(ArenaDB.pointsField, Integer.class);
        highestKillstreak = record.getOrDefault(ArenaDB.highestKillstreakField, Integer.class);
        randomKitEnabled = record.getOrDefault(ArenaDB.randomKitField, Boolean.class);

        String kitName = record.getOrDefault(ArenaDB.kitField, String.class);
        if (kitName != null) {
            kit = KitManager.getKit(kitName);
        }
        if (kit == null) {
            chooseRandomKit();
        }
    }

    public void tryGetPlayer() {
        player = Bukkit.getPlayer(uuid);
        if (player != null) {
            name = player.getName();
        }
    }

    public void chooseRandomKit() {
        List<Kit> allKits = new LinkedList<>(KitManager.getAllKits().values());
        Collections.shuffle(allKits);
        for (Kit kit : allKits) {
            if (canUse(kit)) {
                this.kit = kit;
                break;
            }
        }

        if (kit == null) {
            throw new IllegalStateException("No valid kits for player " + player.getName());
        }
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public Kit getCurrentKit() {
        if (kit == null) {
            chooseRandomKit();
        }
        return kit;
    }

    public void setKit(@NotNull Kit kit) {
        this.kit = kit;
        markAsPendingSave();
    }

    public void setRandomKitEnabled(boolean randomKitEnabled) {
        this.randomKitEnabled = randomKitEnabled;
    }

    /**
     * Forces the next {@link #getPlayer()} to retrieve a new player entity
     * with {@link Bukkit#getPlayer(UUID)}
     */
    public void resetPlayer() {
        player = null;
    }

    public Player getPlayer() {
        // If player is null, or if the player isn't online, get player (they might be online,
        // but if they relog the player entity stored is out of date and needs updating).
        if (player == null || !player.isOnline()) {
            tryGetPlayer();
        }
        return player;
    }

    public boolean canUse(Kit kit) {
        return KitManager.canUse(this, kit);
    }

    public int getPoints() {
        return points;
    }

    public int getKills() {
        return kills;
    }

    private int addKill() {
        markAsPendingSave();
        return ++kills;
    }

    public int getDeaths() {
        return deaths;
    }

    private int addDeath() {
        markAsPendingSave();
        return ++deaths;
    }

    public void setPoints(int points) {
        markAsPendingSave();
        this.points = points;
    }

    public void addPoints(int points) {
        markAsPendingSave();
        this.points += points;
    }

    public void removePoints(int points) {
        markAsPendingSave();
        this.points -= points;
        int minPoints = WbsArena.getInstance().settings.getMinimumPoints();
        if (this.points < minPoints) {
            this.points = minPoints;
        }
    }

    public int getCurrentKillstreak() {
        return currentKillstreak;
    }

    public int getHighestKillstreak() {
        return highestKillstreak;
    }

    public void sendMessage(String message) {
        if (player.isOnline()) {
            WbsArena.getInstance().sendMessage(message, getPlayer());
        }
    }

    public void sendMessageNoPrefix(String message) {
        if (player.isOnline()) {
            WbsArena.getInstance().sendMessageNoPrefix(message, getPlayer());
        }
    }

    public List<String> getStatsStrings() {
        List<String> strings = new LinkedList<>();

        strings.add("&rPoints: &h" + getPoints());
        strings.add("&rKills: &h" + getKills());
        strings.add("&rDeaths: &h" + getDeaths());
        strings.add("&rK/D: &h" + WbsMath.roundTo(getKills() / (double) getDeaths(), 2));
        strings.add("&rCurrent Killstreak: &h" + getCurrentKillstreak());
        strings.add("&rHighest Killstreak: &h" + getHighestKillstreak());

        return strings;
    }

    @Override
    public WbsRecord toRecord() {
        WbsRecord record = new WbsRecord(ArenaDB.getDatabase());

        record.setField(ArenaDB.uuidField, uuid.toString());
        record.setField(ArenaDB.nameField, name);

        record.setField(ArenaDB.killsField, kills);
        record.setField(ArenaDB.deathsField, deaths);
        record.setField(ArenaDB.pointsField, points);

        record.setField(ArenaDB.highestKillstreakField, highestKillstreak);

        record.setField(ArenaDB.kitField, kit.getName());
        record.setField(ArenaDB.randomKitField, randomKitEnabled);

        return record;
    }

    public void onDeath(@Nullable ArenaPlayer killer) {
        ArenaSettings settings = WbsArena.getInstance().settings;

        resetKillstreak();
        removePoints(settings.getDeathPoints());
        addDeath();

        getPlayer().setArrowsInBody(0);

        if (killer == null) {
            ArenaLobby.broadcastArena(settings.getDeathMessage(this), this);
        } else {
            ArenaLobby.broadcastArena(settings.getKillMessage(killer, this), this);
        }

        if (settings.leaveOnDeath()) {
            ArenaLobby.leaveArena(this);
        } else {
            Arena arena = ArenaLobby.getCurrentArena(this);

            if (arena != null) {
                arena.respawn(this);
            } else {
                WbsArena.getInstance().logger.warning("onDeath called while player was outside arena. Please report this issue.");
            }
        }

        refreshScoreboard();
    }

    public void onKill(@NotNull ArenaPlayer victim) {
        WbsArena plugin = WbsArena.getInstance();
        ArenaSettings settings = plugin.settings;

        currentKillstreak++;
        highestKillstreak = Math.max(currentKillstreak, highestKillstreak);
        int killPoints = settings.getKillPoints();
        addPoints(killPoints);
        addKill();

        String pointsMessage = null;
        if (killPoints > 1) {
            pointsMessage = "+" + killPoints + " points!";
        } else if (killPoints == 1) {
            pointsMessage = "+" + killPoints + " point!";
        }

        if (pointsMessage != null) {
            plugin.sendActionBar(pointsMessage, getPlayer());
        }

        giveItemSafely(getPlayer(), settings.getKillRewards().toArray(new ItemStack[0]));
        getPlayer().addPotionEffects(settings.getKillPotionRewards());

        refreshScoreboard();
    }

    private static void giveItemSafely(Player player, ItemStack ... itemStacks) {
        Inventory inv = player.getInventory();

        Map<Integer, ItemStack> failed = inv.addItem(itemStacks);

        Location dropLocation = player.getEyeLocation();
        for (ItemStack item : failed.values()) {
            Objects.requireNonNull(dropLocation.getWorld()).dropItemNaturally(dropLocation, item);
        }

        failed.size();
    }

    public void markAsPendingSave() {
        pendingSave = true;

        switch (WbsArena.getInstance().settings.getSaveMethod()) {
            case INSTANT -> pendingSave = true;
            case PERIODIC -> ArenaDB.getPlayerManager().addToPendingSaves(this);
            case DISCONNECT -> {
            }
        }
    }

    public boolean isPendingSave() {
        return pendingSave;
    }

    public void setPendingSave(boolean pendingSave) {
        this.pendingSave = pendingSave;
    }

    public boolean randomKitEnabled() {
        return randomKitEnabled;
    }

    @Override
    public String toString() {
        return name;
    }

    public void resetKillstreak() {
        currentKillstreak = 0;
    }

    private static final String BORDER = "&h==================";
    private void loadScoreboard() {
        String namespace = getPlayer().getUniqueId().toString();
        namespace = namespace.substring(namespace.length() - 16);

        statsScoreboard = new WbsScoreboard(WbsArena.getInstance(), namespace, WbsArena.getInstance().prefix);
    }

    public void refreshScoreboard() {
        if (statsScoreboard == null) {
            loadScoreboard();
        }
        statsScoreboard.clear();

        statsScoreboard.addLine(BORDER);
        statsScoreboard.addLine("");
        statsScoreboard.addLine("&rKit: &h" + getCurrentKit().getDisplayName());
        statsScoreboard.addLine("&r");

        for (String line : getStatsStrings()) {
            statsScoreboard.addLine(line);
        }

        statsScoreboard.addLine("&r&r");
        statsScoreboard.addLine(BORDER + "&r");

        statsScoreboard.showToPlayer(getPlayer());
    }

    public void showScoreboard() {
        refreshScoreboard();
    }

    public void hideScoreboard() {
        if (statsScoreboard.isBeingViewedBy(getPlayer())) {
            getPlayer().setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard());
        }
    }
}
