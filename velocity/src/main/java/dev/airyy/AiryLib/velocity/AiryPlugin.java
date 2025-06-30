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
import dev.airyy.AiryLib.core.command.argument.impl.BooleanArgument;
import dev.airyy.AiryLib.core.command.argument.impl.IntegerArgument;
import dev.airyy.AiryLib.velocity.command.VelocityCommandManager;
import dev.airyy.AiryLib.velocity.command.argument.PlayerArgument;
import dev.airyy.AiryLib.velocity.command.impl.VelocityTestCommand;
import org.slf4j.Logger;

import java.nio.file.Path;

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
        commandManager = new VelocityCommandManager(this);

        commandManager.registerArgumentParser(int.class, new IntegerArgument());
        commandManager.registerArgumentParser(Integer.class, new IntegerArgument());

        commandManager.registerArgumentParser(boolean.class, new BooleanArgument());
        commandManager.registerArgumentParser(Boolean.class, new BooleanArgument());

        commandManager.registerArgumentParser(Player.class, new PlayerArgument());

        // commandManager.register(new VelocityTestCommand());

        onInit();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
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

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
