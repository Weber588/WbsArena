package wbs.arena.menu.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.menus.MenuSlot;
import wbs.utils.util.menus.WbsMenu;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.function.Consumer;

public class ConfirmationMenu extends WbsMenu {

    private String[] info;
    private boolean receivedResponse = false;
    private final Consumer<Boolean> confirmationConsumer;

    public ConfirmationMenu(@NotNull WbsPlugin plugin,
                            @NotNull String title,
                            @NotNull String id,
                            @NotNull Runnable onConfirm,
                            @Nullable Runnable onCancel) {
        this(plugin, title, id, confirmed -> {
            if (confirmed) {
                onConfirm.run();
            } else {
                if (onCancel != null)
                    onCancel.run();
            }
        });
    }

    public ConfirmationMenu(@NotNull WbsPlugin plugin,
                            @NotNull String title,
                            @NotNull String id,
                            @NotNull Consumer<Boolean> confirmationConsumer) {
        super(plugin, title, 3, id);
        this.confirmationConsumer = confirmationConsumer;

        setUnregisterOnClose(true);

        MenuSlot outline = new MenuSlot(plugin, Material.CYAN_STAINED_GLASS_PANE, "&r");
        setOutline(outline);

        MenuSlot cancelSlot = new MenuSlot(plugin, Material.BARRIER, "&4Cancel");
        cancelSlot.setClickAction(clickEvent -> respond(false));
        cancelSlot.setCloseOnClick(true);

        MenuSlot confirmSlot = new MenuSlot(plugin, Material.LIME_DYE, "&aConfirm");
        confirmSlot.setClickAction(clickEvent -> respond(true));
        confirmSlot.setCloseOnClick(true);

        setSlot(1, 2, cancelSlot);
        setSlot(1, 6, confirmSlot);
    }

    private void respond(boolean response) {
        receivedResponse = true;
        confirmationConsumer.accept(response);
    }

    @Override
    public boolean unregister() {
        // If the player closes the inventory/it gets unregistered before they respond, consider it
        // a cancel.
        if (!receivedResponse) {
            respond(false);
        }
        return super.unregister();
    }

    @Override
    public void showTo(Player player) {
        MenuSlot infoSlot = new MenuSlot(plugin, Material.OAK_SIGN, "&c&lAre you sure?", info);

        setSlot(1, 4, infoSlot);
        super.showTo(player);
    }

    public void setInfo(String ... info) {
        this.info = info;
    }
}
