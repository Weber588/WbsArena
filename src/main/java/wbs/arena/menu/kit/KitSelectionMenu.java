package wbs.arena.menu.kit;

import org.bukkit.Material;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.menu.PlayerSpecificMenu;
import wbs.utils.util.menus.MenuSlot;

public class KitSelectionMenu extends KitMenu {

    public static final int RANDOM_KIT_SLOT_NUM = 8;

    public KitSelectionMenu(WbsArena plugin, ArenaPlayer player, int page) {
        super(plugin, player, "&9&lKits", "kitselection", page);

        if (plugin.settings.allowRandomKit()) {
            RandomKitSelectSlot randomKitSlot = new RandomKitSelectSlot(plugin, player);

            setSlot(RANDOM_KIT_SLOT_NUM, randomKitSlot);
        }
    }

    @Override
    protected MenuSlot getSlotFor(WbsArena plugin, ArenaPlayer player, Kit kit) {
        return new KitSelectSlot(plugin, player, kit);
    }

    @Override
    protected KitMenu getPage(WbsArena plugin, ArenaPlayer player, int page) {
        return new KitSelectionMenu(plugin, player, page);
    }
}
