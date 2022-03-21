package wbs.arena.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.utils.util.database.RecordProducer;
import wbs.utils.util.database.WbsRecord;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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

    public void setPoints(int points) {
        this.points = points;
    }

    public void sendMessage(String message) {
        WbsArena.getInstance().sendMessage(message, getPlayer());
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
}
