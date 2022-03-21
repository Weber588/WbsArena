package wbs.arena.command;

import wbs.arena.WbsArena;
import wbs.utils.util.commands.WbsErrorsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

public class ArenaErrorCommand extends WbsErrorsSubcommand {
    public ArenaErrorCommand(WbsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected WbsSettings getSettings() {
        return WbsArena.getInstance().settings;
    }
}
