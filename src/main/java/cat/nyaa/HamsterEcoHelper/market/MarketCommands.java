package cat.nyaa.HamsterEcoHelper.market;

import cat.nyaa.HamsterEcoHelper.HamsterEcoHelper;
import cat.nyaa.HamsterEcoHelper.database.MarketItem;
import cat.nyaa.nyaacore.LanguageRepository;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MarketCommands extends CommandReceiver {
    private HamsterEcoHelper plugin;

    public MarketCommands(Object plugin, LanguageRepository i18n) {
        super((HamsterEcoHelper) plugin, i18n);
        this.plugin = (HamsterEcoHelper) plugin;
    }

    @Override
    public String getHelpPrefix() {
        return "market";
    }

    @SubCommand(value = "offer", permission = "heh.offer")
    public void offer(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        double price = 0.0;
        price = args.nextDouble("#.##");
        if (!(price >= 0.01)) {
            msg(sender, "user.error.not_double");
            return;
        }
        ItemStack item = getItemInHand(sender);
        if (MarketManager.containsBook(item)) {
            msg(sender, "user.error.shulker_box_contains_book");
            return;
        }
        if (plugin.marketManager.offer(player, item, price)) {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
    }

    @SubCommand(value = "view", permission = "heh.view")
    public void view(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        if (args.length() == 3) {
            OfflinePlayer seller = args.nextOfflinePlayer();
            if (seller != null) {
                plugin.marketManager.openGUI(player, 1, seller.getUniqueId());
            }
        } else {
            plugin.marketManager.openGUI(player, 1, null);
        }
    }

    @SubCommand(value = "givemarketitem", permission = "heh.giveitem")
    public void give(CommandSender sender, Arguments args) {
        Player player = asPlayer(sender);
        MarketItem item = plugin.marketManager.getItem(args.nextInt());
        if (item != null) {
            int slot = player.getInventory().firstEmpty();
            if (slot >= 0 && player.getInventory().getItem(slot) == null) {
                msg(player, "user.market.offered", item.getPlayer().getName());
                msg(player, "user.market.unit_price", item.unitPrice);
                player.getInventory().setItem(slot, item.getItemStack(1));
            }
        }
    }
}
