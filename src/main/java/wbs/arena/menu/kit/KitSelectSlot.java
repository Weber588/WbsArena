package wbs.arena.menu.kit;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.kit.unlock.BuyResult;
import wbs.arena.kit.unlock.KitUnlockMethod;
import wbs.arena.kit.unlock.PurchaseUnlockMethod;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;

import java.util.LinkedList;
import java.util.List;

public class KitSelectSlot extends MenuSlot {

    protected final ArenaPlayer player;
    protected final Kit kit;
    protected final WbsArena plugin;

    public KitSelectSlot(@NotNull WbsArena plugin, @NotNull ArenaPlayer player, @NotNull Kit kit) {
        super(plugin,
                kit.getDisplayMaterial(),
                plugin.dynamicColourise("&r" + kit.getDisplayName()));

        this.plugin = plugin;
        this.player = player;
        this.kit = kit;

        setCloseOnClick(false);

        setClickActionMenu(this::onClick);
    }

    private void onClick(WbsMenu menu, InventoryClickEvent e) {
        if (player.canUse(kit)) {
            player.setKit(kit);
            if (player.randomKitEnabled()) {
                menu.update(KitSelectionMenu.RANDOM_KIT_SLOT_NUM);
                player.setRandomKitEnabled(false);
            }
            player.sendMessage("&hKit updated!");
            menu.update();
        } else {
            KitUnlockMethod method = plugin.settings.getUnlockMethod();

            if (method instanceof PurchaseUnlockMethod) {
                BuyResult buyResult = KitManager.buy(player, kit, true);
                switch (buyResult) {
                    case SUCCESS -> menu.update();
                    case ERROR_PERMISSION, ERROR_MONEY -> player.getPlayer().closeInventory();
                }
            } else {
                player.sendMessage(method.getUnlockDescription(kit));
            }
        }
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        if (player == null) return super.getFormattedItem(null);
        List<String> lore = new LinkedList<>();

        for (String descriptionLine : kit.getDescription()) {
            lore.add("&7" + descriptionLine);
        }

        lore.add("&r");

        ItemStack item = super.getFormattedItem(player);
        ItemMeta meta = item.getItemMeta();
        assert meta != null; // Validated this in kit constructor

        if (this.player.canUse(kit)) {
            if (kit.equals(this.player.getCurrentKit())) {
                lore.add("&6Equipped!");
                meta.addEnchant(Enchantment.LOYALTY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                lore.add("&7Click to equip!");
            }
        } else {
            KitUnlockMethod method = plugin.settings.getUnlockMethod();
            lore.add(kit.getFormattedCostLine());
            if (method instanceof PurchaseUnlockMethod) {
                lore.add("&6Click to buy!");
            }
        }

        meta.setLore(plugin.colouriseAll(lore));
        item.setItemMeta(meta);

        return item;
    }
}
