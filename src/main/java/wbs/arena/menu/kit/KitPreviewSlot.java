package wbs.arena.menu.kit;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.kit.PreviewManager;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public class KitPreviewSlot extends MenuSlot {

    protected final ArenaPlayer player;
    protected final Kit kit;
    protected final WbsArena plugin;

    public KitPreviewSlot(@NotNull WbsArena plugin, @NotNull ArenaPlayer player, @NotNull Kit kit) {
        super(plugin,
                kit.getDisplayMaterial(),
                plugin.dynamicColourise("&r" + kit.getDisplayName()));

        this.plugin = plugin;
        this.player = player;
        this.kit = kit;

        setCloseOnClick(true);

        setClickActionMenu(this::onClick);
    }

    private void onClick(WbsMenu menu, InventoryClickEvent e) {
        PreviewManager.startTimedPreview(player.getPlayer(), kit, WbsArena.getInstance().settings.getMenuPreviewDuration());
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
            lore.add("&6Owned.");
        } else {
            lore.add(kit.getFormattedCostLine());
        }
        lore.add("&bClick to preview!");

        meta.setLore(plugin.colouriseAll(lore));
        item.setItemMeta(meta);

        return item;
    }
}
