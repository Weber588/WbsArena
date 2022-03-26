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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KitManager {
    private KitManager() {}

    private static final Map<String, Kit> kits = new HashMap<>();

    public static void addKit(Kit kit) {
        kits.put(kit.getName().toLowerCase(), kit);
    }

    @Nullable
    public static Kit getKit(@NotNull String kitName) {
        return kits.get(kitName.toLowerCase());
    }

    public static Map<String, Kit> getAllKits() {
        return new HashMap<>(kits);
    }

    @NotNull
    public static Kit createKit(String kitName, YamlConfiguration specs, String fileName) {
        ArenaSettings settings = WbsArena.getInstance().settings;

        String displayName = specs.getString("display-name", kitName);
        boolean ownedByDefault = specs.getBoolean("owned-by-default", false);
        int cost = specs.getInt("cost", 10);
        List<String> description = specs.getStringList("description");
        Material displayItem = WbsEnums.materialFromString(specs.getString("display-item"), Material.BARRIER);

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

        return kit;
    }

    public static void openKitSelection(ArenaPlayer player) {
        KitSelectionMenu menu = new KitSelectionMenu(WbsArena.getInstance(), player);

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
}
