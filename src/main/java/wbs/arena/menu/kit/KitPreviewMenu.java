package wbs.arena.menu.kit;

import org.bukkit.Material;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.menu.PlayerSpecificMenu;
import wbs.utils.util.menus.MenuSlot;

import java.util.Map;

public class KitPreviewMenu extends KitMenu {
    public KitPreviewMenu(WbsArena plugin, ArenaPlayer player, int page) {
        super(plugin, player, "&9&lPreview Kits", "kitpreview", page);
    }

    @Override
    protected MenuSlot getSlotFor(WbsArena plugin, ArenaPlayer player, Kit kit) {
        return new KitPreviewSlot(plugin, player, kit);
    }

    @Override
    protected KitMenu getPage(WbsArena plugin, ArenaPlayer player, int page) {
        return new KitPreviewMenu(plugin, player, page);
    }
}
