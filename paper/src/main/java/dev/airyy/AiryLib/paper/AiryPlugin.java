package dev.airyy.AiryLib.paper;

import dev.airyy.AiryLib.core.IAiryPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class AiryPlugin extends JavaPlugin implements IAiryPlugin {

    private static AiryPlugin instance;

    @Override
    public void onEnable() {
        onInit();
    }

    @Override
    public void onDisable() {
        onDestroy();
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onDestroy() {

    }

    public static <T extends AiryPlugin> T getInstance() {
        return (T) instance;
    }
}
