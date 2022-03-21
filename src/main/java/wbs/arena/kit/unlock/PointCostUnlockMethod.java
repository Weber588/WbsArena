package wbs.arena.kit.unlock;

import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.utils.util.pluginhooks.VaultWrapper;

public class PointCostUnlockMethod extends PermissionUnlockMethod implements PurchaseUnlockMethod {
    public PointCostUnlockMethod() {
        super("POINT_COST");
    }

    @Override
    public BuyResult buy(ArenaPlayer player, Kit kit, boolean sendMessages) {
        if (player.getPoints() >= kit.getCost()) {
            boolean given = VaultWrapper.givePermission(player.getPlayer(), kit.getPermission());
            if (given) {
                player.setPoints(player.getPoints() - kit.getCost());
            }
            if (sendMessages) {
                player.sendMessage("Kit unlocked!");
            }
            return BuyResult.SUCCESS;
        } else {
            if (sendMessages) {
                player.sendMessage("Not enough points!");
            }
            return BuyResult.NO_POINTS;
        }
    }

    @Override
    public String getUnlockDescription(Kit kit) {
        return "You can buy this kit for " + formatCost(kit) + "!";
    }

    @Override
    public String formatCost(Kit kit) {
        return kit.getCost() + " points";
    }

    @Override
    public String getCostName() {
        return "cost";
    }
}
