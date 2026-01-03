package dev.airyy.AiryLib.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.airyy.AiryLib.BuildConstants;
import dev.airyy.AiryLib.core.IAiryPlugin;
import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.argument.impl.BooleanArgument;
import dev.airyy.AiryLib.core.command.argument.impl.IntegerArgument;
import dev.airyy.AiryLib.core.command.argument.impl.StringArgument;
import dev.airyy.AiryLib.core.config.ConfigManager;
import dev.airyy.AiryLib.core.config.parser.StringParser;
import dev.airyy.AiryLib.core.config.parser.UUIDParser;
import dev.airyy.AiryLib.velocity.command.VelocityCommandManager;
import dev.airyy.AiryLib.velocity.command.argument.PlayerArgument;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Base velocity implementation of the {@link IAiryPlugin} interface
 *
 * <p>This class provides multiple components needed for making plugins at ease, including {@link CommandManager} and others.</p>
 *
 * <p>Intended to be extended by plugin developers who want a simplified
 * starting point for their own plugins.
 *
 * @see IAiryPlugin
 */
@Plugin(id = "airylib", name = "AiryLib", version = BuildConstants.VERSION, authors = {"AiryyCodes"})
public class AiryPlugin implements IAiryPlugin {

    private static AiryPlugin instance;

    private final Logger logger;
    private final ProxyServer server;
    private final Path dataDirectory;

    private VelocityCommandManager commandManager;

    @Inject
    public AiryPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

    }

    @Subscribe
    private void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        commandManager = new VelocityCommandManager(this);

        commandManager.registerArgumentParser(int.class, new IntegerArgument());
        commandManager.registerArgumentParser(Integer.class, new IntegerArgument());

        commandManager.registerArgumentParser(boolean.class, new BooleanArgument());
        commandManager.registerArgumentParser(Boolean.class, new BooleanArgument());

        commandManager.registerArgumentParser(String.class, new StringArgument());

        commandManager.registerArgumentParser(Player.class, new PlayerArgument());

        ConfigManager.registerParser(String.class, new StringParser());
        ConfigManager.registerParser(UUID.class, new UUIDParser());

        // commandManager.register(new VelocityTestCommand());

        onInit();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        onDestroy();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This base implementation does nothing. Override in subclasses to register listeners,
     * load configurations, or perform setup tasks.
     */
    @Override
    public void onInit() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>This base implementation does nothing. Override in subclasses to clean up resources,
     * save state, or unregister listeners.
     */
    @Override
    public void onDestroy() {
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

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public VelocityCommandManager getCommandManager() {
        return commandManager;
    }
}
