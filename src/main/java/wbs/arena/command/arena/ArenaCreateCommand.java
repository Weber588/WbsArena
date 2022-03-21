package wbs.arena.command.arena;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.world.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.arena.ArenaManager;
import wbs.utils.util.commands.WbsSubcommand;

public class ArenaCreateCommand extends WbsSubcommand {
    private WbsArena plugin;

    public ArenaCreateCommand(@NotNull WbsArena plugin) {
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
            sendUsage("<name>", sender, label, args, args.length);
            return true;
        }

        String name = args[start];

        if (ArenaManager.getArena(name) != null) {
            sendMessage("An arena with that name already exists!", sender);
            return true;
        }

        SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
        LocalSession session = sessionManager.findByName(player.getName());
        if (session == null) {
            sendMessage("Make a world edit selection for the arena.", sender);
            return true;
        }

        World world = session.getSelectionWorld();

        if (!world.getName().equals(player.getWorld().getName())) {
            sendMessage("Make a world edit selection in your current world for the arena.", sender);
            return true;
        }

        Region region;
        try {
            region = session.getSelection(world);
        } catch (IncompleteRegionException e) {
            sendMessage("Make a world edit selection for the arena.", sender);
            return true;
        }

        BlockVector3 block1 = region.getMinimumPoint();
        BlockVector3 block2 = region.getMaximumPoint();

        Block blockMin = player.getWorld().getBlockAt(block1.getBlockX(), block1.getBlockY(), block1.getBlockZ());
        Block blockMax = player.getWorld().getBlockAt(block2.getBlockX(), block2.getBlockY(), block2.getBlockZ());

        Arena arena = new Arena(name, blockMin, blockMax);

        ArenaManager.registerArena(arena);
        plugin.settings.saveArenaAsync(arena, success -> {
            sendMessage("Arena registered! You can edit it with &h/" + label + " edit " + arena.getName(), sender);
        });

        return true;
    }
}
