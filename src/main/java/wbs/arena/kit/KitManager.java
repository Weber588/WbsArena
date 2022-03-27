package wbs.arena.kit;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.unlock.BuyResult;
import wbs.arena.kit.unlock.KitUnlockMethod;
import wbs.arena.kit.unlock.PurchaseUnlockMethod;
import wbs.arena.menu.kit.KitSelectionMenu;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.entities.state.SavedEntityState;

import java.util.*;

public final class KitManager {
    private KitManager() {}

    private static final Map<String, Kit> kits = new HashMap<>();

    public static void addKit(Kit kit) {
        int order = kit.getOrder();

        // Find first order that isn't filled by another kit
        int newOrder = 0;
        while (order == -1) {
            boolean orderExists = false;
            for (Kit existingKit : kits.values()) {
                if (existingKit.getOrder() == newOrder) {
                    orderExists = true;
                    break;
                }
            }
            if (!orderExists) {
                order = newOrder;
            } else {
                newOrder++;
            }
        }

        kit.setOrder(order);

        kits.put(kit.getName().toLowerCase(), kit);
    }

    @Nullable
    public static Kit getKit(@NotNull String kitName) {
        return kits.get(kitName.toLowerCase());
    }

    public static Map<String, Kit> getAllKits() {
        LinkedHashMap<String, Kit> toReturn = new LinkedHashMap<>();

        List<Kit> sorted = new LinkedList<>(kits.values());

        sorted.sort(Comparator.comparing(Kit::getOrder));

        for (Kit entry : sorted) {
            toReturn.put(entry.getName(), entry);
        }

        return toReturn;
    }

    @NotNull
    public static Kit createKit(String kitName, YamlConfiguration specs, String fileName) {
        ArenaSettings settings = WbsArena.getInstance().settings;

        String displayName = specs.getString("display-name", kitName);
        boolean ownedByDefault = specs.getBoolean("owned-by-default", false);
        int cost = specs.getInt("cost", 10);
        List<String> description = specs.getStringList("description");
        Material displayItem = WbsEnums.materialFromString(specs.getString("display-item"), Material.BARRIER);
        int order = specs.getInt("order", -1);

        SavedEntityState<Player> playerState;
        try {
            //noinspection unchecked
            playerState = (SavedEntityState<Player>) specs.getSerializable("player-state", SavedEntityState.class);
        } catch (ClassCastException e) {
            String error = "Failed to deserialize player state (i.e. inventory): mismatched generic type. Please report this to the developer.";
            settings.logError(error, fileName + "/player-state");
            throw new InternalError(error);
        }

        if (playerState == null) {
            String error = "Failed to deserialize player state (i.e. inventory). Please report this to the developer.";
            settings.logError(error, fileName + "/player-state");
            throw new InternalError(error);
        }

        Kit kit = new Kit(playerState, kitName, description);

        kit.setDisplayName(displayName);
        kit.setOwnedByDefault(ownedByDefault);
        kit.setCost(cost);
        kit.setDisplayMaterial(displayItem);
        kit.setOrder(order);

        return kit;
    }

    public static void openKitSelection(ArenaPlayer player) {
        KitSelectionMenu menu = new KitSelectionMenu(WbsArena.getInstance(), player, 0);

        menu.showTo(player.getPlayer());
    }

    public static BuyResult buy(ArenaPlayer player, Kit kit) {
        return buy(player, kit, false);
    }

    public static BuyResult buy(ArenaPlayer player, Kit kit, boolean sendMessages) {
        if (player.canUse(kit)) {
            if (sendMessages) {
                player.sendMessage("You already have access to that kit!");
            }
            return BuyResult.ALREADY_OWNED;
        }

        KitUnlockMethod method = WbsArena.getInstance().settings.getUnlockMethod();
        if (method instanceof PurchaseUnlockMethod) {
            return ((PurchaseUnlockMethod) method).buy(player, kit, sendMessages);
        } else {
            if (sendMessages) {
                player.sendMessage("Buying kits is disabled.");
            }
            return BuyResult.BUYING_DISABLED;
        }
    }

    public static boolean canUse(ArenaPlayer player, Kit kit) {
        return WbsArena.getInstance().settings.getUnlockMethod().canUse(player, kit) || kit.isOwnedByDefault();
    }

    public static String getCostFormat() {
        return WbsArena.getInstance().settings.getUnlockMethod().getCostName();
    }

    public static void removeKit(Kit kit) {
        kits.remove(kit.getName());
    }

    public static void clear() {
        kits.clear();
    }
}
