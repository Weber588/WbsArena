package wbs.arena.command.kit;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public class KitCreateCommand extends WbsSubcommand {

    private final WbsArena plugin;

    public KitCreateCommand(@NotNull WbsArena plugin) {
        super(plugin, "create");
        this.plugin = plugin;
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return true;
        }

        if (args.length <= start) {
            sendUsage("<name> <description>", sender, label, args, args.length);
            return true;
        }

        String name = args[start];

        if (KitManager.getAllKits().containsKey(name.toLowerCase())) {
            sendMessage("There is already a kit with that name.", sender);
            return true;
        }

        if (args.length <= start + 1) {
            sendUsage("<description>", sender, label, args, args.length);
            return true;
        }

        List<String> description = new LinkedList<>();
        description.add(WbsStrings.combineLast(args, start + 1, " "));

        Kit kit = new Kit(player, name, description);

        Material displayMaterial = player.getInventory().getItemInMainHand().getType();
        if (displayMaterial == Material.AIR) {
            displayMaterial = Material.BARRIER;
        }

        kit.setDisplayMaterial(displayMaterial);

        KitManager.addKit(kit);

        plugin.settings.saveKitAsync(kit, success -> {
            sendMessage("Kit created with your current inventory and XP level! " +
                    "To edit it further, do &h/" + label + " kit edit " + name, player);
        });
        return true;
    }
}
