package wbs.arena.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.ArenaLobby;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
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

    private Kit kit;

    public ArenaPlayer(UUID uuid) {
        this.uuid = uuid;

        tryGetPlayer();

        if (player != null) {
            chooseRandomKit();
        }
    }

    public ArenaPlayer(WbsRecord record) {
        uuid = UUID.fromString(record.getValue(ArenaDB.uuidField, String.class));

        tryGetPlayer();

        if (name == null) {
            name = record.getValue(ArenaDB.nameField, String.class);
        }

        kills = record.getOrDefault(ArenaDB.killsField, Integer.class);
        deaths = record.getOrDefault(ArenaDB.deathsField, Integer.class);
        points = record.getOrDefault(ArenaDB.pointsField, Integer.class);

        String kitName = record.getOrDefault(ArenaDB.kitField, String.class);
        kit = KitManager.getKit(kitName);
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

    public int getDeaths() {
        return deaths;
    }

    public void setPoints(int points) {
        this.points = points;
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

    @Override
    public WbsRecord toRecord() {
        WbsRecord record = new WbsRecord(ArenaDB.getDatabase());

        record.setField(ArenaDB.uuidField, uuid.toString());
        record.setField(ArenaDB.nameField, name);

        record.setField(ArenaDB.killsField, kills);
        record.setField(ArenaDB.deathsField, deaths);
        record.setField(ArenaDB.pointsField, points);

        record.setField(ArenaDB.kitField, kit.getName());

        return record;
    }

    public void onDeath(@Nullable ArenaPlayer killer) {
        ArenaSettings settings = WbsArena.getInstance().settings;
        points -= settings.getDeathPoints();

        deaths++;

        if (killer == null) {
            ArenaLobby.broadcastArena(settings.getDeathMessage(this), this);
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
    }

    public void onKill(@NotNull ArenaPlayer victim) {
        WbsArena plugin = WbsArena.getInstance();
        ArenaSettings settings = plugin.settings;

        int killPoints = settings.getKillPoints();
        points += killPoints;

        kills++;

        String pointsMessage = null;
        if (killPoints > 1) {
            pointsMessage = "+" + killPoints + " points!";
        } else if (killPoints == 1) {
            pointsMessage = "+" + killPoints + " point!";
        }

        if (pointsMessage != null) {
            plugin.sendActionBar(pointsMessage, getPlayer());
        }

        ArenaLobby.broadcastArena(settings.getKillMessage(this, victim), this);

        giveItemSafely(getPlayer(), settings.getKillRewards().toArray(new ItemStack[0]));
        getPlayer().addPotionEffects(settings.getKillPotionRewards());
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
}
