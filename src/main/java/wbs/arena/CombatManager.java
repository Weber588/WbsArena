package wbs.arena;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.data.ArenaPlayer;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class CombatManager {
    private static final Map<ArenaPlayer, CombatTag> lastDamagerMap = new HashMap<>();
    private static final Multimap<ArenaPlayer, WeakReference<Projectile>> projectilesFired = LinkedHashMultimap.create();

    public static boolean isInCombat(@NotNull ArenaPlayer player) {
        CombatTag tag = lastDamagerMap.get(player);
        if (tag == null) return false;

        return tag.isValid();
    }

    public static void registerCombat(@NotNull ArenaPlayer attacker, @NotNull ArenaPlayer victim) {
        lastDamagerMap.put(victim, new CombatTag(attacker));
    }

    @Nullable
    public static CombatTag getCurrentTag(@NotNull ArenaPlayer player) {
        return lastDamagerMap.get(player);
    }

    /**
     * Register a projectile as having been shot by an ArenaPlayer, if shot by one.
     * @param projectile The projectile to register.
     * @return Whether or not the projectile was registered to an ArenaPlayer.
     */
    public static boolean registerProjectile(Projectile projectile) {
        ProjectileSource shooter = projectile.getShooter();

        if (shooter instanceof Player player) {
            ArenaPlayer arenaPlayer = ArenaLobby.getPlayerFromLobby(player);

            if (arenaPlayer != null) {
                projectilesFired.put(arenaPlayer, new WeakReference<>(projectile));
                return true;
            }
        }
        return false;
    }

    public static Collection<Projectile> getRegisteredProjectiles(ArenaPlayer player) {
        List<Projectile> projectiles = new LinkedList<>();
        List<WeakReference<Projectile>> toRemove = new LinkedList<>();
        for (WeakReference<Projectile> ref : projectilesFired.get(player)) {
            Projectile proj = ref.get();
            if (proj == null) {
                toRemove.add(ref);
            } else {
                if (!proj.isValid()) {
                    toRemove.add(ref);
                } else {
                    projectiles.add(proj);
                }
            }
        }

        for (WeakReference<Projectile> ref : toRemove) {
            projectilesFired.remove(player, ref);
        }

        return projectiles;
    }

    public static class CombatTag {

        private final ArenaPlayer attacker;
        private final Instant timestamp = Instant.now();

        public CombatTag(ArenaPlayer attacker) {
            this.attacker = attacker;
        }

        public boolean isValid() {
            return Duration.between(timestamp, Instant.now()).toMillis() / 50 < WbsArena.getInstance().settings.getCombatTagDuration();
        }

        public ArenaPlayer getAttacker() {
            return attacker;
        }
    }
}
