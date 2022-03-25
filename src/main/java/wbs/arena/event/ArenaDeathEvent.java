package wbs.arena.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.data.ArenaPlayer;

public class ArenaDeathEvent extends ArenaPlayerEvent implements Cancellable {
    private boolean isCancelled = false;

    @Nullable
    private final ArenaPlayer killingPlayer;

    public ArenaDeathEvent(@NotNull ArenaPlayer arenaPlayer) {
        super(arenaPlayer);
        killingPlayer = null;
    }

    public ArenaDeathEvent(@NotNull ArenaPlayer arenaPlayer, @Nullable ArenaPlayer killingPlayer) {
        super(arenaPlayer);
        this.killingPlayer = killingPlayer;
    }

    @Nullable
    public ArenaPlayer getKillingPlayer() {
        return killingPlayer;
    }

    private static final HandlerList HANDLERS = new HandlerList();
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
