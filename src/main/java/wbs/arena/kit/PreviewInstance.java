package wbs.arena.kit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}
