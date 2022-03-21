package wbs.arena.menu;

import wbs.arena.data.ArenaPlayer;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

public class PlayerSpecificMenu extends WbsMenu {
    protected final ArenaPlayer player;

    public PlayerSpecificMenu(WbsPlugin plugin, ArenaPlayer player, String title, int rows, String id) {
        super(plugin, title, rows, id + player.getName());
        setUnregisterOnClose(true);

        this.player = player;
    }
}
