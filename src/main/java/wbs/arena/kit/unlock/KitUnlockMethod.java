package wbs.arena.kit.unlock;

import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;

import java.util.HashMap;
import java.util.Map;

public abstract class KitUnlockMethod {
    private static final Map<String, KitUnlockMethod> registered = new HashMap<>();

    public static void registerKnownMethods() {
        register(new PointThresholdUnlockMethod());
        register(new MoneyUnlockMethod());
        register(new PointCostUnlockMethod());
        register(new PermissionUnlockMethod());
    }

    public static void register(KitUnlockMethod unlockMethod) {
        registered.put(formatId(unlockMethod.id), unlockMethod);
    }

    public static KitUnlockMethod getMethod(String id) {
        return registered.get(formatId(id));
    }

    private static String formatId(String id) {
        return id.toLowerCase().replace("_", "").replace("-", "");
    }

    private final String id;

    public KitUnlockMethod(String id) {
        this.id = formatId(id);
    }

    public abstract boolean canUse(ArenaPlayer player, Kit kit);
    public abstract String getUnlockDescription(Kit kit);

    public abstract String formatCost(Kit kit);

    public abstract String getCostName();
}
