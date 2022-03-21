package wbs.arena.command.arena;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.arena.Arena;
import wbs.arena.kit.KitManager;
import wbs.utils.util.WbsEnums;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaEditCommand extends ArenaSubcommand {
    private final WbsArena plugin;

    public ArenaEditCommand(@NotNull WbsArena plugin) {
        super(plugin, "edit");
        this.plugin = plugin;
    }

    private enum EditOption {
        NAME
    }

    @Override
    protected final boolean onArenaCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Arena arena) {
        if (args.length < start + 1) {
            String optionsString =
                    Arrays.stream(EditOption.values())
                            .map(Enum::toString)
                            .map(String::toLowerCase)
                            .collect(Collectors.joining("|"));
            sendUsage("<" + optionsString + "> <value>", sender, label, args);
            return true;
        }

        EditOption option = WbsEnums.getEnumFromString(EditOption.class, args[start]);

        if (option == null) {
            sendMessage("Invalid option: &h" + args[start] + "&r. Please choose from the following: &h"
                    + WbsEnums.joiningPrettyStrings(EditOption.class, ", "), sender);
            return true;
        }

        if (args.length < start + 2) {
            sendUsage("<" + option.toString().toLowerCase() + ">", sender, label, args);
            return true;
        }
        String value = args[start + 1];

        switch (option) {
            case NAME -> arena.setDisplayName(value);
        }

        plugin.settings.saveArenaAsync(arena, success -> {
            if (success) {
                sendMessage("Updated & saved!", sender);
            } else {
                sendMessage("Something went wrong while saving. Please check console.", sender);
            }
        });

        return true;
    }


    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<String> options = new LinkedList<>();

        switch (args.length - start + 1) {
            case 1 -> options.addAll(KitManager.getAllKits().keySet());
            case 2 -> options.addAll(
                    Arrays.stream(EditOption.values())
                            .map(option -> option.toString().toLowerCase())
                            .collect(Collectors.toList())
            );
        }

        return options;
    }
}
