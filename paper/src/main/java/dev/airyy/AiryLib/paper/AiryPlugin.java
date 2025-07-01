package dev.airyy.AiryLib.paper;

import dev.airyy.AiryLib.core.IAiryPlugin;
import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.argument.impl.BooleanArgument;
import dev.airyy.AiryLib.core.command.argument.impl.IntegerArgument;
import dev.airyy.AiryLib.core.command.argument.impl.StringArgument;
import dev.airyy.AiryLib.core.config.ConfigManager;
import dev.airyy.AiryLib.paper.command.PaperCommandManager;
import dev.airyy.AiryLib.paper.command.argument.PlayerArgument;
import dev.airyy.AiryLib.paper.config.parser.ComponentParser;
import dev.airyy.AiryLib.paper.config.parser.LocationParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Base paper implementation of the {@link IAiryPlugin} interface
 *
 * <p>This class provides multiple components needed for making plugins at ease, including {@link CommandManager} and others.</p>
 *
 * <p>Intended to be extended by plugin developers who want a simplified
 * starting point for their own plugins.
 *
 * @see IAiryPlugin
 */
public class AiryPlugin extends JavaPlugin implements IAiryPlugin {

    private static AiryPlugin instance;

    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        instance = this;

        commandManager = new PaperCommandManager(this);

        commandManager.registerArgumentParser(int.class, new IntegerArgument());
        commandManager.registerArgumentParser(Integer.class, new IntegerArgument());

        commandManager.registerArgumentParser(boolean.class, new BooleanArgument());
        commandManager.registerArgumentParser(Boolean.class, new BooleanArgument());

        commandManager.registerArgumentParser(String.class, new StringArgument());

        commandManager.registerArgumentParser(Player.class, new PlayerArgument());

        ConfigManager.registerParser(Component.class, new ComponentParser());
        ConfigManager.registerParser(Location.class, new LocationParser());

        // commandManager.register(new TestCommand());

        onInit();
    }

    @Override
    public void onDisable() {
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

    /**
     * <p>Gets the instance of the current plugin</p>
     * @return the current instance of the plugin
     */
    @SuppressWarnings("unchecked")
    public static <T extends AiryPlugin> T getInstance() {
        return (T) instance;
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }
}
