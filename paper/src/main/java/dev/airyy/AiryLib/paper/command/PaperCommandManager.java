package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.command.CommandData;
import dev.airyy.AiryLib.command.CommandManager;
import dev.airyy.AiryLib.command.annotations.Command;
import dev.airyy.AiryLib.command.annotations.Default;
import dev.airyy.AiryLib.command.arguments.ArgumentConverter;
import dev.airyy.AiryLib.command.arguments.IntegerArgument;
import dev.airyy.AiryLib.paper.command.arguments.PlayerArgument;
import dev.airyy.AiryLib.utils.Annotations;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

public class PaperCommandManager extends CommandManager {

    private final JavaPlugin plugin;

    private final CommandMap commandMap;
    private final Map<String, ArgumentConverter<?>> converters;

    public PaperCommandManager(JavaPlugin plugin) {
        super();

        this.plugin = plugin;
        this.commandMap = plugin.getServer().getCommandMap();
        this.converters = new HashMap<>();

        registerArgument(int.class, new IntegerArgument());
        registerArgument(Player.class, new PlayerArgument());
    }

    @Override
    public <T> void registerCommand(T command) {
        if (!Annotations.hasAnnotation(command.getClass(), Command.class)) {
            plugin.getLogger().warning("Cannot register a non command class");
            return;
        }

        Command rootCommand = command.getClass().getAnnotation(Command.class);
        if (commandMap.getCommand(rootCommand.value()) != null) {
            plugin.getLogger().warning("There can only be one class instance per command");
            return;
        }

        if (rootCommand.value().contains(" ")) {
            plugin.getLogger().warning("There cannot be any whitespace in the command name");
            return;
        }

        List<Method> defaultHandlers = getDefaultHandlers(command);

        PaperCommandHandler<T> commandHandler = new PaperCommandHandler<>(plugin, rootCommand.value(), Arrays.stream(rootCommand.aliases()).toList(), defaultHandlers, command, converters);
        commandMap.register(rootCommand.value(), commandHandler);
    }

    private static <T> @NotNull List<Method> getDefaultHandlers(T command) {
        List<Method> defaultHandlers = new ArrayList<>();
        for (Method method : command.getClass().getDeclaredMethods()) {
            if (Annotations.hasAnnotation(method, Default.class)) {
                defaultHandlers.add(method);
                continue;
            }
        }
        return defaultHandlers;
    }

    @Override
    public void registerArgument(Class<?> type, ArgumentConverter<?> argument) {
        converters.put(type.getTypeName(), argument);
    }
}
