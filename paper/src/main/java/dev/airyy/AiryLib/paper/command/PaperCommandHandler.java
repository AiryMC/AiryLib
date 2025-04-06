package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.ICommandHandler;
import dev.airyy.AiryLib.core.command.ICommandSender;
import dev.airyy.AiryLib.core.command.arguments.IArgumentConverter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class PaperCommandHandler<T> extends Command implements ICommandHandler {

    private final JavaPlugin plugin;
    private final CommandManager manager;
    private final String name;
    private final List<String> aliases;
    private final List<Method> defaultHandlers;
    private final Map<String, List<Method>> subCommands;
    private final T commandClass;
    private final Map<String, IArgumentConverter<?>> converters;

    public PaperCommandHandler(JavaPlugin plugin, CommandManager commandManager, String name, List<String> aliases, List<Method> defaultHandlers, Map<String, List<Method>> subCommands, T commandClass, Map<String, IArgumentConverter<?>> converters) {
        super(name, "todo", "todo", aliases);

        this.plugin = plugin;
        this.manager = commandManager;
        this.name = name;
        this.aliases = aliases;
        this.defaultHandlers = defaultHandlers;
        this.subCommands = subCommands;
        this.commandClass = commandClass;
        this.converters = converters;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!label.equalsIgnoreCase(getName()))
            return true;

        execute(manager.getCommandSender(sender), args);

        return true;
    }

    @Override
    public boolean handleCommand(Method command, ICommandSender sender, List<Object> objects, Parameter[] parameters) {
        if (sender.getSender() instanceof Player player && isParamPlayer(parameters[0])) {
            return handlePlayer(command, player, objects);
        } else if (sender.getSender() instanceof ConsoleCommandSender consoleSender && isParamConsole(parameters[0])) {
            return handleConsole(command, consoleSender, objects);
        } else if (isParamSender(parameters[0])) {
            return handleGeneric(command, sender.getSender(), objects);
        }

        ((CommandSender) sender.getSender()).sendMessage("You cannot use this command");

        return true;
    }

    private boolean handlePlayer(Method method, Player player, List<Object> objects) {
        objects.addFirst(player);

        try {
            if (method.getParameters().length > 0 && !objects.isEmpty()) {
                method.invoke(commandClass, objects.toArray());
            } else {
                method.invoke(commandClass);
            }
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean handleConsole(Method method, ConsoleCommandSender console, List<Object> objects) {
        objects.addFirst(console);

        try {
            if (method.getParameters().length > 0 && !objects.isEmpty()) {
                method.invoke(commandClass, objects.toArray());
            } else {
                method.invoke(commandClass);
            }
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean handleGeneric(Method method, CommandSender sender, List<Object> objects) {
        objects.addFirst(sender);

        try {
            if (method.getParameters().length > 0 && !objects.isEmpty()) {
                method.invoke(commandClass, objects.toArray());
            } else {
                method.invoke(commandClass);
            }
            return true;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isParamPlayer(Parameter parameter) {
        return parameter.getType() == Player.class;
    }

    private boolean isParamConsole(Parameter parameter) {
        return parameter.getType() == ConsoleCommandSender.class;
    }

    private boolean isParamSender(Parameter parameter) {
        return parameter.getType() == CommandSender.class;
    }


    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<String> getAliases() {
        return aliases;
    }

    public List<Method> getDefaultHandlers() {
        return defaultHandlers;
    }

    public Map<String, List<Method>> getSubCommands() {
        return subCommands;
    }

    @Override
    public boolean isSubCommand(String subCommand) {
        return subCommands.containsKey(subCommand);
    }

    @Override
    public List<Method> getSubCommands(String subCommandName) {
        return subCommands.get(subCommandName);
    }

    @Override
    public @Nullable IArgumentConverter<?> getConverter(String typeName) {
        return converters.getOrDefault(typeName, null);
    }
}
