package dev.airyy.AiryLib.paper.item;

import dev.airyy.AiryLib.paper.AiryPlugin;
import dev.airyy.AiryLib.paper.item.listener.ItemUseListener;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ItemManager {

    private final AiryPlugin plugin = AiryPlugin.getInstance();

    private final Map<String, Item> registeredItems = new HashMap<>();

    public void init() {
        Server server = plugin.getServer();
        PluginManager pluginManager = server.getPluginManager();

        pluginManager.registerEvents(new ItemUseListener(), plugin);
    }

    public void register(Item item) {
        registeredItems.put(item.getId(), item);
    }

    public @Nullable Item getItem(String id) {
        return registeredItems.get(id);
    }
}
