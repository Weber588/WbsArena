package wbs.arena.kit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import wbs.arena.CombatManager;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.data.PlayerManager;
import wbs.utils.util.entities.state.SavedEntityState;

public class PreviewInstance {
    private final Player player;
    private final int taskId;
    private final SavedEntityState<Player> state;
    private final Kit kit;

    public PreviewInstance(Player player, int taskId, SavedEntityState<Player> state, Kit kit) {
        this.player = player;
        this.taskId = taskId;
        this.state = state;
        this.kit = kit;
    }

    public Kit getKit() {
        return kit;
    }

    public void start() {
        kit.getPlayerState().restoreState(player);
    }

    public void end() {
        state.restoreState(player);

        ArenaPlayer arenaPlayer = ArenaDB.getPlayerManager().getCached(player.getUniqueId());
        CombatManager.getRegisteredProjectiles(arenaPlayer).forEach(Entity::remove);

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}
