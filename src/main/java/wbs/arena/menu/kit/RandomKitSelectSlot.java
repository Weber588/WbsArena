package wbs.arena.menu.kit;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.kit.unlock.BuyResult;
import wbs.arena.kit.unlock.KitUnlockMethod;
import wbs.arena.kit.unlock.PurchaseUnlockMethod;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RandomKitSelectSlot extends MenuSlot {
    protected final ArenaPlayer player;
    protected final WbsArena plugin;

    public RandomKitSelectSlot(@NotNull WbsArena plugin, @NotNull ArenaPlayer player) {
        super(plugin,
                Material.CLOCK,
                "&dRandomize Kit",
                player.randomKitEnabled());

        this.plugin = plugin;
        this.player = player;

        setCloseOnClick(false);

        setClickActionMenu(this::onClick);
    }

    private void onClick(WbsMenu menu, InventoryClickEvent e) {
        player.setRandomKitEnabled(!player.randomKitEnabled());
        menu.update(KitSelectionMenu.RANDOM_KIT_SLOT_NUM);
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        if (player == null) return super.getFormattedItem(null);

        boolean randomEnabled = this.player.randomKitEnabled();

        ItemStack item = super.getFormattedItem(player);
        ItemMeta meta = item.getItemMeta();
        assert meta != null; // Validated this in kit constructor

        List<String> lore = new LinkedList<>(Arrays.asList(
                "&7When enabled, your kit",
                "&7will be randomized every",
                "&7time you join the arena!",
                "",
                randomEnabled ? "&bEnabled" : "&cDisabled"));

        meta.setLore(plugin.colouriseAll(lore));
        item.setItemMeta(meta);

        return item;
    }
}
