package wbs.arena.kit.unlock;

import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;

public class PointThresholdUnlockMethod extends KitUnlockMethod {
    public static final String POINT_THRESHOLD_ID = "POINT_THRESHOLD";

    public PointThresholdUnlockMethod() {
        super(POINT_THRESHOLD_ID);
    }

    @Override
    public boolean canUse(ArenaPlayer player, Kit kit) {
        return player.getPoints() >= kit.getCost();
    }

    @Override
    public String getUnlockDescription(Kit kit) {
        return "Unlock this kit by reaching " + formatCost(kit) + " points!";
    }

    @Override
    public String formatCost(Kit kit) {
        return kit.getCost() + "";
    }

    @Override
    public String getCostName() {
        return "points required";
    }
}
