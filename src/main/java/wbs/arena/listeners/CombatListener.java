package wbs.arena.listeners;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.ArenaLobby;
import wbs.arena.ArenaSettings;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.event.ArenaDeathEvent;

import wbs.arena.CombatManager;
import wbs.arena.CombatManager.CombatTag;

@SuppressWarnings("unused")
public class CombatListener implements Listener {

    private static final ArenaSettings settings = WbsArena.getInstance().settings;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            handleDamage(event, player, null);
        }
    }

    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victim) {
            if (event.getDamager() instanceof Player attacker) {
                handleDamage(event, victim, attacker);
            } else if (event.getDamager() instanceof Projectile projectile) {
                if (projectile.getShooter() instanceof Player attacker) {
                    handleDamage(event, victim, attacker);
                }
            }
        }
    }

    private void handleDamage(EntityDamageEvent event, @NotNull Player victim, @Nullable Player attacker) {
        if (settings.preventDamageInArena(event.getCause())) {
            event.setCancelled(true);
            return;
        }

        ArenaPlayer victimInArena = ArenaLobby.getPlayerFromArena(victim);

        if (victimInArena != null) {
            ArenaPlayer attackerInArena = null;
            if (attacker != null) {
                attackerInArena = ArenaLobby.getPlayerFromArena(attacker);
            }

            handleArenaDamage(event, victimInArena, attackerInArena);
            return;
        }

        ArenaPlayer victimInLobby = ArenaLobby.getPlayerFromLobby(victim);

        if (victimInLobby != null) {
            handleLobbyDamage(event, victimInLobby);
        }
    }

    private void handleLobbyDamage(EntityDamageEvent event, ArenaPlayer victim) {
        event.setCancelled(true);
    }

    private void handleArenaDamage(EntityDamageEvent event, @NotNull ArenaPlayer victim, @Nullable ArenaPlayer attacker) {
        if (attacker != null) {
            CombatManager.registerCombat(attacker, victim);
        }

        double endDamage = event.getFinalDamage();

        if (victim.getPlayer().getHealth() - endDamage <= 0) {
            event.setCancelled(true);

            ArenaDeathEvent deathEvent;

            CombatTag tag = CombatManager.getCurrentTag(victim);
            if (tag != null && tag.isValid()) {
                deathEvent = new ArenaDeathEvent(victim, tag.getAttacker());
            } else {
                deathEvent = new ArenaDeathEvent(victim);
            }

            WbsArena.getInstance().pluginManager.callEvent(deathEvent);

            if (!deathEvent.isCancelled()) {
                ArenaPlayer attackerInArena = deathEvent.getKillingPlayer();
                victim.onDeath(attackerInArena);
                if (attackerInArena != null) {
                    attackerInArena.onKill(victim);
                }
            }
        }
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        CombatManager.registerProjectile(projectile);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!settings.despawnProjectiles()) {
            return;
        }
        Projectile projectile = event.getEntity();

        ProjectileSource shooter = projectile.getShooter();

        if (shooter instanceof Player player) {
            ArenaPlayer arenaPlayer = ArenaLobby.getPlayerFromArena(player);

            if (arenaPlayer != null) {
                if (projectile instanceof AbstractArrow arrow) {
                    if (arrow.getPickupStatus() != AbstractArrow.PickupStatus.ALLOWED) {
                        arrow.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

    }
}
