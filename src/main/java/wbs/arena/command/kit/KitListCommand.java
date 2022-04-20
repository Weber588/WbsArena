package wbs.arena.command.kit;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.arena.WbsArena;
import wbs.arena.data.ArenaDB;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.utils.util.commands.WbsSubcommand;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class KitListCommand extends WbsSubcommand {
    public KitListCommand(@NotNull WbsArena plugin) {
        super(plugin, "list");
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (sender instanceof Player player) {
            ArenaDB.getPlayerManager().getAsync(player.getUniqueId(), arenaPlayer -> showList(arenaPlayer, sender));
        } else {
            showList(null, sender);
        }
        return true;
    }

    private void showList(@Nullable ArenaPlayer player, CommandSender sender) {
        for (Kit kit : KitManager.getAllKits().values()) {
            String display = kit.getDisplayName() + "&h (" + kit.getName() + "&h)";

            List<String> infoLines = kit.getInfoDisplay(player);
            String hoverText = String.join("\n", infoLines);

            plugin.buildMessage(display)
                    .addHoverText(hoverText)
                    .addClickCommandSuggestion("/wbsarena kit info " + kit.getName())
                    .send(sender);
        }
    }
}
