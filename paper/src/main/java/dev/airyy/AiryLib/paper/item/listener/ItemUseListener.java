package dev.airyy.AiryLib.paper.item.listener;

import dev.airyy.AiryLib.paper.AiryPlugin;
import dev.airyy.AiryLib.paper.item.Item;
import dev.airyy.AiryLib.paper.item.ItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemUseListener implements Listener {

    private final AiryPlugin plugin = AiryPlugin.getInstance();
    private final ItemManager itemManager = plugin.getItemManager();

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null)
            return;
        if (!item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String id = pdc.get(Item.ITEM_ID_KEY, PersistentDataType.STRING);

        Item customItem = itemManager.getItem(id);
        if (customItem == null)
            return;

        customItem.onUse(player);
    }
}
