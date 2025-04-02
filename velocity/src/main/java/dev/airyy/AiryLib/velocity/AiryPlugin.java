package dev.airyy.AiryLib.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.airyy.AiryLib.BuildConstants;
import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.velocity.command.VelocityCommandManager;
import org.slf4j.Logger;

@Plugin(id = "airylib", name = "AiryLib", version = BuildConstants.VERSION, authors = {"AiryyCodes"})
public class AiryPlugin {

    private static AiryPlugin instance;

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;
    private CommandManager commandManager;

    @Subscribe
    private void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        commandManager = new VelocityCommandManager(server);
        // commandManager.registerCommand(new TestCommand());

        onEnable();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public static <T extends AiryPlugin> T getInstance() {
        return (T) instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
