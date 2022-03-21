package wbs.arena.data;

import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.utils.util.database.AbstractDataManager;
import wbs.utils.util.database.WbsRecord;

import java.util.UUID;

public final class PlayerManager extends AbstractDataManager<ArenaPlayer, UUID> {
    public PlayerManager(WbsArena plugin) {
        super(plugin, ArenaDB.playerTable);
    }

    @Override
    protected @NotNull ArenaPlayer fromRecord(@NotNull WbsRecord wbsRecord) {
        return new ArenaPlayer(wbsRecord);
    }

    @Override
    protected @NotNull ArenaPlayer produceDefault(UUID uuid) {
        return new ArenaPlayer(uuid);
    }
}
