package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.annotations.Command;
import dev.airyy.AiryLib.core.command.annotations.Default;
import dev.airyy.AiryLib.core.command.annotations.SubCommand;
import dev.airyy.AiryLib.core.command.arguments.ArgumentConverter;
import dev.airyy.AiryLib.core.command.arguments.IntegerArgument;
import dev.airyy.AiryLib.core.command.arguments.StringArgument;
import dev.airyy.AiryLib.core.utils.Annotations;
import dev.airyy.AiryLib.paper.command.arguments.PlayerArgument;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

public class PaperCommandManager extends CommandManager {

    private final JavaPlugin plugin;
    private final CommandMap commandMap;

    public PaperCommandManager(JavaPlugin plugin) {
        super();

        this.plugin = plugin;
        this.commandMap = plugin.getServer().getCommandMap();

        registerArgument(int.class, new IntegerArgument());
        registerArgument(String.class, new StringArgument());
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
        Map<String, List<Method>> subCommands = getSubCommands(command);

        PaperCommandHandler<T> commandHandler = new PaperCommandHandler<>(plugin, rootCommand.value(), Arrays.stream(rootCommand.aliases()).toList(), defaultHandlers, subCommands, command, getConverters());
        commandMap.register(rootCommand.value(), commandHandler);
    }

    private <T> @NotNull List<Method> getDefaultHandlers(T command) {
        List<Method> defaultHandlers = new ArrayList<>();
        for (Method method : command.getClass().getDeclaredMethods()) {
            if (Annotations.hasAnnotation(method, Default.class)) {
                defaultHandlers.add(method);
            }
        }
        return defaultHandlers;
    }

    private <T> @NotNull Map<String, List<Method>> getSubCommands(T command) {
        Map<String, List<Method>> subCommands = new HashMap<>();
        for (Method method : command.getClass().getDeclaredMethods()) {
            if (Annotations.hasAnnotation(method, SubCommand.class)) {
                SubCommand subCommand = method.getAnnotation(SubCommand.class);
                if (subCommands.containsKey(subCommand.value())) {
                    subCommands.get(subCommand.value()).add(method);
                } else {
                    subCommands.put(subCommand.value(), new ArrayList<>(List.of(method)));
                }
            }
        }
        return subCommands;
    }

    @Override
    public void registerArgument(Class<?> type, ArgumentConverter<?> argument) {
        getConverters().put(type.getTypeName(), argument);
    }
}
