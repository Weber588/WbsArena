package wbs.arena.kit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.unlock.KitUnlockMethod;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.entities.state.SavedEntityState;
import wbs.utils.util.entities.state.tracker.InventoryState;
import wbs.utils.util.entities.state.tracker.XPState;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a saved inventory, and possibly abilities/passive effects.
 */
public class Kit {

    public static SavedEntityState<Player> getDefaultEntityState() {
        return new SavedEntityState<Player>()
                .track(new InventoryState())
                .track(new XPState());
    }

    @NotNull
    private String name;
    @NotNull
    private String displayName;
    @NotNull
    private List<String> description;

    private int order = -1;

    @NotNull
    private Material displayMaterial = Material.BARRIER;
    private boolean ownedByDefault;
    private int cost;

    @NotNull
    private SavedEntityState<Player> playerState;

    public Kit(@NotNull SavedEntityState<Player> playerState, @NotNull String name, @NotNull List<String> description) {
        this.playerState = playerState;
        this.name = name.toLowerCase();
        displayName = name;
        this.description = description;
    }

    public Kit(@NotNull Player player, @NotNull String name, @NotNull List<String> description) {
        this(name, description);
        playerState.captureState(player);
    }

    public Kit(@NotNull String name, @NotNull List<String> description) {
        this(getDefaultEntityState(),
                name,
                description);
    }

    @Nullable
    public ItemStack getPreviewItem(@Nullable List<String> extraLore) {
        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(displayMaterial);
        if (meta == null)
            return null;

        meta.setDisplayName(WbsArena.getInstance().dynamicColourise(name));

        List<String> lore = new LinkedList<>(description);
        if (extraLore != null) {
            lore.addAll(extraLore);
        }

        meta.setLore(WbsArena.getInstance().colouriseAll(lore));

        item.setItemMeta(meta);
        return item;
    }

    public void giveTo(ArenaPlayer player) {
        playerState.restoreState(player.getPlayer());
    }

    @NotNull
    public String getName() {
        return name;
    }
    public void setName(@NotNull String name) {
        this.name = name;
    }
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    public Material getDisplayMaterial() {
        return displayMaterial;
    }
    public void setDisplayMaterial(@NotNull Material displayMaterial) {
        this.displayMaterial = displayMaterial;
    }

    @NotNull
    public List<String> getDescription() {
        return new LinkedList<>(description);
    }
    public void setDescription(@NotNull List<String> description) {
        this.description = description;
    }

    public boolean isOwnedByDefault() {
        return ownedByDefault;
    }
    public void setOwnedByDefault(boolean ownedByDefault) {
        this.ownedByDefault = ownedByDefault;
    }

    public int getCost() {
        return cost;
    }
    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getFormattedCostLine(String displayColour, String valueColour) {
        KitUnlockMethod method = WbsArena.getInstance().settings.getUnlockMethod();
        return displayColour + WbsStrings.capitalizeAll(method.getCostName()) + ": " + valueColour + method.formatCost(this);
    }

    public String getFormattedCostLine() {
        return getFormattedCostLine("&6", "&b");
    }

    public List<String> getInfoDisplay(@Nullable ArenaPlayer player) {
        List<String> infoDisplay = new LinkedList<>();

        infoDisplay.add("&6Name: &r" + getDisplayName());
        infoDisplay.add("&6Description:");
        for (String line : getDescription()) {
            infoDisplay.add("&6- &7" + line);
        }
        infoDisplay.add("&6Default?: &r" + formatBoolean(isOwnedByDefault()));
        infoDisplay.add(getFormattedCostLine("&6", "&r"));
        infoDisplay.add("&6Display item: &r" +
                WbsEnums.toPrettyString(getDisplayMaterial()));
        infoDisplay.add("&6Order: &r" + getOrder());
        if (player != null) {
            infoDisplay.add("&6Owned? &r" + formatBoolean(KitManager.canUse(player, this)));
        }

        return infoDisplay;
    }

    private static String formatBoolean(boolean bool) {
        return bool ? "&atrue" : "&cfalse";
    }

    public void setPlayerState(@NotNull SavedEntityState<Player> playerState) {
        this.playerState = playerState;
    }
    @NotNull
    public SavedEntityState<Player> getPlayerState() {
        return playerState;
    }

    public String getPermission() {
        return "wbsarena.kit.owned." + getName().toLowerCase();
    }

    public YamlConfiguration toConfig(YamlConfiguration config) {
        config.set("display-name", displayName);
        config.set("owned-by-default", ownedByDefault);
        config.set("cost", cost);
        config.set("description", description);
        config.set("display-item", displayMaterial.toString());
        config.set("player-state", playerState);
        config.set("order", order);

        return config;
    }
}
