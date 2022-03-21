package wbs.arena.kit.unlock;

import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;

public interface PurchaseUnlockMethod {
    BuyResult buy(ArenaPlayer player, Kit kit, boolean sendMessages);
}
