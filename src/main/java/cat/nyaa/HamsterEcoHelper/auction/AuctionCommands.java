package cat.nyaa.HamsterEcoHelper.auction;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.market.MarketManager;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommands extends CommandReceiver {
    private HamsterEcoHelper plugin;

    public AuctionCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "auction";
    }

    @SubCommand(value = "addauc", permission = "heh.addauction")
    public void addAuc(CommandSender sender, Arguments args) {
        if (args.length() != 7) {
            msg(sender, "manual.auction.addauc.usage");
            return;
        }
        AuctionItemTemplate item = new AuctionItemTemplate();
        item.templateItemStack = getItemInHand(sender).clone();
        item.baseAuctionPrice = args.nextInt();
        item.bidStepPrice = args.nextInt();
        item.randomWeight = args.nextDouble();
        item.hideName = args.nextBoolean();
        item.waitTimeTicks = args.nextInt();
        plugin.config.auctionConfig.itemsForAuction.add(item);
        plugin.config.save();
    }

    @SubCommand(value = "runauc", permission = "heh.runauc")
    public void runAuction(CommandSender sender, Arguments args) {
        boolean success;
        if (args.length() == 3) {
            int id = args.nextInt();
            if (id < 0 || id >= plugin.config.auctionConfig.itemsForAuction.size()) {
                msg(sender, "admin.error.auc_id_oor", 0, plugin.config.auctionConfig.itemsForAuction.size() - 1);
                return;
            } else {
                success = plugin.auctionManager.newAuction(plugin.config.auctionConfig.itemsForAuction.get(id));
            }
        } else {
            success = plugin.auctionManager.newAuction();
        }
        if (!success) {
            msg(sender, "admin.error.run_auc_fail");
        }
    }

    @SubCommand(value = "haltauc", permission = "heh.runauc")
    public void haltAuction(CommandSender sender, Arguments args) {
        AuctionInstance auc = plugin.auctionManager.getCurrentAuction();
        if (auc == null) {
            msg(sender, "user.info.no_current_auction");
            return;
        }
        plugin.auctionManager.halt();
    }

    @SubCommand(value = "bid", permission = "heh.bid")
    public void userBid(CommandSender sender, Arguments args) {
        userBid(sender, args, false);
    }

    public void userBid(CommandSender sender, Arguments args, boolean min) {
        Player p = asPlayer(sender);
        AuctionInstance auc = plugin.auctionManager.getCurrentAuction();
        if (auc == null) {
            msg(p, "user.info.no_current_auction");
            return;
        }
        if (args.remains() == 0 && !min) {
            msg(sender, "manual.auction.bid.usage");
            return;
        }
        if (auc.owner != null && auc.owner.getUniqueId().equals(p.getUniqueId())) {
            return;
        }
        double minPrice = auc.currentHighPrice == -1 ? auc.startPr : auc.currentHighPrice + auc.stepPr;
        String tmp = args.top();
        double bid;
        if ((args.remains() == 0 && min) || "min".equals(tmp)) {
            bid = minPrice;
        } else {
            bid = args.nextDouble("#.##");
        }

        if (!plugin.eco.enoughMoney(p, bid)) {
            msg(p, "user.warn.no_enough_money");
            return;
        }
        if (bid < minPrice) {
            msg(p, "user.warn.not_high_enough", minPrice);
            return;
        }
        auc.onBid(p, bid);
    }

    @SubCommand(value = "auc", permission = "heh.userauc")
    public void Auc(CommandSender sender, Arguments args) {
        if (args.remains() < 2) {
            msg(sender, "manual.auction.auc.usage");
            return;
        }
        Player player = asPlayer(sender);
        ItemStack item = getItemInHand(sender).clone();
        double basePrice = args.nextDouble("#.##");
        double stepPrice = args.nextDouble("#.##");
        if (stepPrice <= 0) {
            msg(sender, "user.auc.step_price_error");
            return;
        }
        if (MarketManager.containsBook(item)) {
            msg(sender, "user.error.shulker_box_contains_book");
            return;
        }
        int reservePrice = 0;
        if (args.remains() == 1) {
            reservePrice = args.nextInt();
            if (reservePrice <= basePrice) {
                msg(sender, "user.auc.reserve_price_error");
                return;
            }
        }
        boolean success = plugin.auctionManager.newPlayerAuction(player, item, basePrice, stepPrice, reservePrice);
        if (success) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }
}
