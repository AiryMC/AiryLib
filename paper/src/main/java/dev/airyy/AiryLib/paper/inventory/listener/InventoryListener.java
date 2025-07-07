package dev.airyy.AiryLib.paper.inventory.listener;

import dev.airyy.AiryLib.paper.AiryPlugin;
import dev.airyy.AiryLib.paper.inventory.BaseInventory;
import dev.airyy.AiryLib.paper.inventory.InventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        BaseInventory ui = InventoryManager.getOpenInventories().get(player);
        if (ui != null && event.getInventory().equals(ui.getInventory())) {
            event.setCancelled(true);
            ui.handleClick(event);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryManager.getOpenInventories().remove((Player) event.getPlayer());
    }
}