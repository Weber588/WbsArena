package wbs.arena.command.kit;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.arena.WbsArena;
import wbs.arena.kit.Kit;
import wbs.arena.kit.KitManager;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.string.WbsStrings;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class KitEditCommand extends KitSubcommand {

    private final WbsArena plugin;

    public KitEditCommand(@NotNull WbsArena plugin) {
        super(plugin, "edit");
        this.plugin = plugin;
    }

    private enum EditOption {
        NAME, DESCRIPTION, COST, ITEM, DEFAULT, ORDER
    }

    @Override
    protected final boolean onKitCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, @NotNull Kit kit) {
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
            sendUsage("<new " + option.toString().toLowerCase() + ">", sender, label, args);
            return true;
        }

        String value = WbsStrings.combineLast(args, start + 1, " ");

        switch (option) {
            case NAME -> kit.setDisplayName(value);
            case DESCRIPTION -> kit.setDescription(Collections.singletonList(value));
            case COST -> {
                int newCost;
                try {
                    newCost = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    sendMessage("Invalid number: " + value, sender);
                    return true;
                }
                kit.setCost(newCost);
            }
            case ITEM -> {
                Material newMaterial = WbsEnums.materialFromString(value);
                if (newMaterial != null) {
                    kit.setDisplayMaterial(newMaterial);
                } else {
                    sendMessage("Invalid material: " + value, sender);
                    return true;
                }
            }
            case DEFAULT -> {
                boolean isDefault = Boolean.getBoolean(value);
                kit.setOwnedByDefault(isDefault);
            }
            case ORDER -> {
                int newOrder;
                try {
                    newOrder = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    sendMessage("Invalid number: " + value, sender);
                    return true;
                }
                kit.setOrder(newOrder);
            }
        }

        plugin.settings.saveKitAsync(kit, success -> {
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
        List<String> choices = new LinkedList<>();

        switch (args.length - start + 1) {
            case 1 -> choices.addAll(KitManager.getAllKits().keySet());
            case 2 -> choices.addAll(
                    Arrays.stream(EditOption.values())
                            .map(option -> option.toString().toLowerCase())
                            .collect(Collectors.toList())
                );
            case 3 -> {
                EditOption option = WbsEnums.getEnumFromString(EditOption.class, args[1]);
                if (option != null) {
                    switch (option) {
                        case ITEM -> Arrays.stream(Material.values())
                                .map(Enum::toString)
                                .map(String::toLowerCase)
                                .forEach(choices::add);
                        case DEFAULT -> {
                            choices.add("true");
                            choices.add("false");
                        }
                        case ORDER -> {
                            int kitCount = KitManager.getAllKits().size();
                            for (int i = 0; i < kitCount; i++) {
                                choices.add(i + "");
                            }
                        }
                    }
                }
            }
        }

        return choices;
    }
}
