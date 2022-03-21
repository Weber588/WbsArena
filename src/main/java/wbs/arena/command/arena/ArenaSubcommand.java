package wbs.arena.command.arena;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.arena.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;

import java.util.LinkedList;
import java.util.List;

public abstract class ArenaSubcommand extends WbsSubcommand {
    protected final WbsArena plugin;

    public ArenaSubcommand(@NotNull WbsArena plugin, @NotNull String label) {
        super(plugin, label);
        this.plugin = plugin;
    }

    @Override
    protected final boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length <= start) {
            return onArenaCommandNoArgs(sender, label, args, start);
        }

        String name = args[start].toLowerCase();
        Arena arena = ArenaManager.getArena(name);

        if (arena == null) {
            sendMessage("Arena not found: &h" + name, sender);
            return true;
        }

        return onArenaCommand(sender, label, args, start + 1, arena);
    }

    /**
     * Called when there was no argument provided for an arena. By default, this sends the usage,
     * requiring the field to be entered as an arena, but extending classes can override this if
     * there should be other behaviour (such as opening a menu).
     */
    protected boolean onArenaCommandNoArgs(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        sendUsage("<name>", sender, label, args, args.length);
        return true;
    }

    protected abstract boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena);

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> results = new LinkedList<>();

        //noinspection SwitchStatementWithTooFewBranches
        switch (args.length - start + 1) {
            case 1:
                for (Arena arena : ArenaManager.getAllArenas().values()) {
                    results.add(arena.getName());
                }
                break;
        }

        return results;
    }
}
