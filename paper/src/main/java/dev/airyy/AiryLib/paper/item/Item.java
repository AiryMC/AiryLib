package dev.airyy.AiryLib.paper.item;

import dev.airyy.AiryLib.paper.AiryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public abstract class Item {

    public static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(AiryPlugin.getInstance(), "ITEM_ID");

    private final String id;
    private final Material material;
    private String name;
    private String displayName;

    public Item(String id, Material material) {
        this.id = id;
        this.material = material;
    }

    public Item(String id, Material material, String name) {
        this.id = id;
        this.material = material;
        this.name = name;
    }

    public Item(String id, Material material, String name, String displayName) {
        this.id = id;
        this.material = material;
        this.name = name;
        this.displayName = displayName;
    }

    public void onUse(Player player) {
    }

    public void onClick(InventoryClickEvent event) {
    }

    public @NotNull ItemStack getItemStack() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();

            pdc.set(ITEM_ID_KEY, PersistentDataType.STRING, id);

            if (!displayName.isEmpty()) {
                try {
                    Component component = MiniMessage.miniMessage().deserialize(displayName);
                    meta.displayName(component);
                } catch (Exception e) {
                    // Fallback to legacy
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    public void give(Player player) {
        player.getInventory().addItem(getItemStack());
    }

    public void give(Player player, int slot) {
        player.getInventory().setItem(slot, getItemStack());
    }

    public boolean isSame(ItemStack item) {
        if (item == null)
            return false;
        if (!item.hasItemMeta())
            return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String pdcId = pdc.get(ITEM_ID_KEY, PersistentDataType.STRING);
        return id.equals(pdcId);
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
