package wbs.arena.menu.kit;

import org.bukkit.Material;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.arena.menu.PlayerSpecificMenu;
import wbs.utils.util.menus.MenuSlot;

public class KitPreviewMenu extends PlayerSpecificMenu {
    public KitPreviewMenu(WbsArena plugin, ArenaPlayer player) {
        super(plugin, player, "&9&lPreview Kits", 6, "kitpreview");

        MenuSlot outlineSlot = new MenuSlot(plugin, Material.BLUE_STAINED_GLASS_PANE, "&r");
        setOutline(outlineSlot);

        for (Kit kit : KitManager.getAllKits().values()) {
            KitPreviewSlot slot = new KitPreviewSlot(plugin, player, kit);

            setNextFreeSlot(1, 4, 1, 7, slot);
        }
    }
}
