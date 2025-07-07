package dev.airyy.AiryLib.paper.inventory;

import dev.airyy.AiryLib.paper.AiryPlugin;
import dev.airyy.AiryLib.paper.inventory.listener.InventoryListener;
import dev.airyy.AiryLib.paper.item.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseInventory {

    private final Inventory inventory;
    private final Map<Integer, Item> items = new HashMap<>();

    public BaseInventory(Component title, int size) {
        AiryPlugin plugin = AiryPlugin.getInstance();

        this.inventory = plugin.getServer().createInventory(null, size, title);
        build();
    }

    /**
     * Implement this to populate the inventory with items.
     */
    protected abstract void build();

    public void onClick(InventoryClickEvent event) {}

    protected void setItem(int slot, Item item) {
        items.put(slot, item);
        inventory.setItem(slot, item.getItemStack());
    }

    public void open(Player player) {
        player.openInventory(inventory);
        InventoryManager.track(player, this);
    }

    public void handleClick(InventoryClickEvent event) {
        onClick(event);

        Item item = items.get(event.getSlot());
        if (item != null) {
            item.onClick(event);
        }
    }

    public Inventory getInventory() {
        return inventory;
    }
}
