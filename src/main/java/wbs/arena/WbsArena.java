package wbs.arena;

import wbs.arena.command.ArenaCommand;
import wbs.arena.command.KitCommand;
import wbs.arena.data.ArenaDB;
import wbs.arena.kit.unlock.KitUnlockMethod;
import wbs.arena.listeners.CommandListener;
import wbs.arena.listeners.CombatListener;
import wbs.arena.listeners.MiscListener;
import wbs.utils.util.plugin.WbsPlugin;

public class WbsArena extends WbsPlugin {

    public static final String BASE_PERMISSION = "wbsarena";

    private static WbsArena instance;
    public static WbsArena getInstance() {
        return instance;
    }

    public ArenaSettings settings;

    @Override
    public void onEnable() {
        instance = this;
        settings = new ArenaSettings(this);

        ArenaDB.setupDatabase();
        KitUnlockMethod.registerKnownMethods();

        settings.reload();

        new ArenaCommand(this, getCommand("wbsarena"));
        new KitCommand(this, getCommand("wbskit"));

        registerListener(new CombatListener());
        registerListener(new MiscListener());
        registerListener(new CommandListener());

        StatsManager.recalculateAll();
        PlaceholderManager.registerPlaceholders();
    }

    @Override
    public void onDisable() {
        ArenaLobby.clear();
    }
}
