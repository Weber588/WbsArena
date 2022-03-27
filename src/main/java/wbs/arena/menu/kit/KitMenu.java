package wbs.arena.menu.kit;

import org.bukkit.Material;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.menu.PlayerSpecificMenu;
import wbs.utils.util.menus.MenuSlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class KitMenu extends PlayerSpecificMenu {
    private static final int ROW_COUNT = 4;
    private static final int COLUMN_COUNT = 7;
    private static final int PAGE_SIZE = ROW_COUNT * COLUMN_COUNT;

    private static int getRowsForPage(int totalKits, int pageNumber) {
        int kitsOnPage = totalKits - (PAGE_SIZE * pageNumber);
        kitsOnPage = Math.max(0, kitsOnPage);
        kitsOnPage = Math.min(kitsOnPage, PAGE_SIZE);
        int kitRows = ((kitsOnPage) - 1) / COLUMN_COUNT + 1;
        // Add 2 for outlines on rows 0 and 5
        return 2 + kitRows;
    }

    public KitMenu(WbsArena plugin, ArenaPlayer player, String title, String id, int page) {
        super(plugin, player, title, getRowsForPage(KitManager.getAllKits().size(), page), id + ":" + page);

        MenuSlot outlineSlot = new MenuSlot(plugin, Material.BLUE_STAINED_GLASS_PANE, "&r");
        setOutline(outlineSlot);

        Map<String, Kit> kitMap = KitManager.getAllKits();
        List<Kit> kitsOnPage = new LinkedList<>();

        int index = 0;
        for (Kit kit : kitMap.values()) {
            if (index < PAGE_SIZE * (page + 1) && index >= PAGE_SIZE * page) {
                kitsOnPage.add(kit);
            }
            index++;
        }

        // TODO: Make rows protected in WbsUtils
        int rows = getRow(getMaxSlot());

        if (kitMap.size() >= PAGE_SIZE * (page + 1)) {
            MenuSlot nextPageSlot = new MenuSlot(plugin, Material.GREEN_STAINED_GLASS, "&r");
            nextPageSlot.setClickActionMenu((menu, click) -> {
                KitMenu nextPage = getPage(plugin, player, page + 1);
                nextPage.showTo(player.getPlayer());
            });
            setSlot(rows / 2, 8, nextPageSlot);
            if (rows % 2 == 1) {
                setSlot((rows + 1) / 2, 8, nextPageSlot);
            }
        }

        if (page > 0) {
            MenuSlot prevPageSlot = new MenuSlot(plugin, Material.RED_STAINED_GLASS, "&r");
            prevPageSlot.setClickActionMenu((menu, click) -> {
                KitMenu prevPage = getPage(plugin, player, page - 1);
                prevPage.showTo(player.getPlayer());
            });

            setSlot(rows / 2, 0, prevPageSlot);
            if (rows % 2 == 1) {
                setSlot((rows + 1) / 2, 0, prevPageSlot);
            }
        }

        for (Kit kit : kitsOnPage) {
            MenuSlot slot = getSlotFor(plugin, player, kit);

            setNextFreeSlot(1, 4, 1, 7, slot);
        }
    }

    protected abstract MenuSlot getSlotFor(WbsArena plugin, ArenaPlayer player, Kit kit);

    protected abstract KitMenu getPage(WbsArena plugin, ArenaPlayer player, int page);
}
