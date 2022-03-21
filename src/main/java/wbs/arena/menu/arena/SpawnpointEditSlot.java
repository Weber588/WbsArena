package wbs.arena.menu.arena;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.arena.Arena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.menu.kit.ConfirmationMenu;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

public class SpawnpointEditSlot extends MenuSlot {
    private final Location spawnpoint;
    private final String locationString;
    private final Arena arena;
    private final ArenaPlayer player;

    public SpawnpointEditSlot(@NotNull WbsPlugin plugin,
                              @NotNull Material material,
                              @NotNull String displayName,
                              @NotNull Location spawnpoint,
                              @NotNull Arena arena,
                              @NotNull ArenaPlayer player) {
        super(plugin, material, displayName);
        this.spawnpoint = spawnpoint;
        this.arena = arena;
        this.player = player;

        this.locationString = spawnpoint.getBlockX() + ", " + spawnpoint.getBlockY() + ", " + spawnpoint.getBlockZ();

        setClickActionMenu(this::onClick);
    }

    private void onClick(WbsMenu menu, InventoryClickEvent clickEvent) {
        ConfirmationMenu confirmationMenu = new ConfirmationMenu(plugin, "&9&lRemove " + locationString,
                "confirmDelete" + spawnpoint.hashCode(), () -> {
            arena.removeSpawnpoint(spawnpoint);
        }, () -> {

        });

        confirmationMenu.showTo(player.getPlayer());
    }

    @Override
    public ItemStack getFormattedItem(@Nullable Player player) {
        ItemStack item = super.getFormattedItem(player);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        List<String> lore = new LinkedList<>();

        lore.add("&b" + locationString);
        lore.add("");
        lore.add("&4Click to delete");

        meta.setLore(plugin.colouriseAll(lore));
        item.setItemMeta(meta);

        return item;
    }
}
