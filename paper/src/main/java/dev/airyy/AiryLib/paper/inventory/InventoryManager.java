package dev.airyy.AiryLib.paper.inventory;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.WeakHashMap;

public class InventoryManager {

    private static final Map<Player, BaseInventory> openInventories = new WeakHashMap<>();

    public static void track(Player player, BaseInventory inventory) {
        openInventories.put(player, inventory);
    }

    public static Map<Player, BaseInventory> getOpenInventories() {
        return openInventories;
    }
}
