package wbs.arena.kit.unlock;

import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;

public class PermissionUnlockMethod extends KitUnlockMethod {
    protected PermissionUnlockMethod(String id) {
        super(id);
    }
    public PermissionUnlockMethod() {
        super("PERMISSION_ONLY");
    }

    @Override
    public boolean canUse(ArenaPlayer player, Kit kit) {
        return player.getPlayer().hasPermission(kit.getPermission());
    }

    @Override
    public String getUnlockDescription(Kit kit) {
        return "You need the permission \"" + kit.getPermission() + "\" to use this kit.";
    }

    @Override
    public String formatCost(Kit kit) {
        return kit.getPermission();
    }

    @Override
    public String getCostName() {
        return "permission";
    }
}
