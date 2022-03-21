package wbs.arena.kit.unlock;

import wbs.arena.WbsArena;
import wbs.arena.data.ArenaPlayer;
import wbs.arena.kit.Kit;
import wbs.utils.util.pluginhooks.VaultWrapper;

public class MoneyUnlockMethod extends PermissionUnlockMethod implements PurchaseUnlockMethod {
    public MoneyUnlockMethod() {
        super("MONEY_COST");
    }

    @Override
    public BuyResult buy(ArenaPlayer player, Kit kit, boolean sendMessages) {
        if (!VaultWrapper.isEcoSetup()) {
            WbsArena.getInstance().logger.warning("Vault economy not set up while trying to purchase kit (" + player.getName() + ")");
            if (sendMessages) {
                player.sendMessage("&wFailed to withdraw money. Please report this error.");
            }
            return BuyResult.ERROR_MONEY;
        }
        if (VaultWrapper.getMoney(player.getPlayer()) >= kit.getCost()) {
            boolean taken = VaultWrapper.takeMoney(player.getPlayer(), kit.getCost());
            if (taken) {
                boolean given = VaultWrapper.givePermission(player.getPlayer(), kit.getPermission());

                if (given) {
                    if (sendMessages) {
                        player.sendMessage("Kit unlocked!");
                    }
                    return BuyResult.SUCCESS;
                } else {
                    // Give money back since permission setting failed.
                    VaultWrapper.giveMoney(player.getPlayer(), kit.getCost());
                    WbsArena.getInstance().logger.warning("Unable to set kit permission for " + player.getName());
                    if (sendMessages) {
                        player.sendMessage("&wFailed to update access. Please report this error.");
                    }
                    return BuyResult.ERROR_PERMISSION;
                }
            } else {
                WbsArena.getInstance().logger.warning("Unable to take money from player " + player.getName());
                if (sendMessages) {
                    player.sendMessage("&wFailed to withdraw money. Please report this error.");
                }
                return BuyResult.ERROR_MONEY;
            }
        } else {
            if (sendMessages) {
                player.sendMessage("Not enough money!");
            }
            return BuyResult.NO_MONEY;
        }
    }

    @Override
    public String getUnlockDescription(Kit kit) {
        return "You can buy this kit for " + formatCost(kit) + "!";
    }

    @Override
    public String formatCost(Kit kit) {
        return VaultWrapper.formatMoney(kit.getCost());
    }

    @Override
    public String getCostName() {
        return "cost";
    }
}
