package wbs.arena.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import wbs.arena.data.ArenaPlayer;

public abstract class ArenaPlayerEvent extends PlayerEvent {

    private final ArenaPlayer arenaPlayer;

    public ArenaPlayerEvent(@NotNull ArenaPlayer arenaPlayer) {
        super(arenaPlayer.getPlayer());
        this.arenaPlayer = arenaPlayer;
    }

    @NotNull
    public ArenaPlayer getArenaPlayer() {
        return arenaPlayer;
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
}
