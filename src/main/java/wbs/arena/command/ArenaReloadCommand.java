package wbs.arena.command;

import wbs.arena.WbsArena;
import wbs.utils.util.commands.WbsReloadSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

public class ArenaReloadCommand extends WbsReloadSubcommand {
    public ArenaReloadCommand(WbsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected WbsSettings getSettings() {
        return WbsArena.getInstance().settings;
    }
}
