package wbs.arena.command.kit;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.utils.util.commands.WbsSubcommand;

import java.util.LinkedList;
import java.util.List;

public abstract class KitSubcommand extends WbsSubcommand {

    protected final WbsArena plugin;

    public KitSubcommand(@NotNull WbsArena plugin, @NotNull String label) {
        super(plugin, label);
        this.plugin = plugin;
    }

    @Override
    protected final boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length <= start) {
            return onKitCommandNoKit(sender, label, args, start);
        }

        String name = args[start].toLowerCase();
        Kit kit = KitManager.getKit(name);

        if (kit == null) {
            sendMessage("Kit not found: &h" + name, sender);
            return true;
        }

        return onKitCommand(sender, label, args, start + 1, kit);
    }

    /**
     * Called when there was no argument provided for a kit. By default, this sends the usage,
     * requiring the field to be entered as a kit, but extending classes can override this if
     * there should be other behaviour (such as opening a menu).
     */
    protected boolean onKitCommandNoKit(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        sendUsage("<name>", sender, label, args, args.length);
        return true;
    }

    protected abstract boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit);

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> results = new LinkedList<>();

        //noinspection SwitchStatementWithTooFewBranches
        switch (args.length - start + 1) {
            case 1:
                for (Kit kit : KitManager.getAllKits().values()) {
                    results.add(kit.getName());
                }
                break;
        }

        return results;
    }
}
