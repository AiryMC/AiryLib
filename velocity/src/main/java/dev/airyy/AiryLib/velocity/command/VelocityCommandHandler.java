package dev.airyy.AiryLib.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.ICommandHandler;
import dev.airyy.AiryLib.core.command.ICommandSender;
import dev.airyy.AiryLib.core.command.arguments.IArgumentConverter;
import dev.airyy.AiryLib.velocity.AiryPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class VelocityCommandHandler<T> implements SimpleCommand, ICommandHandler {

    private final ProxyServer server;
    private final AiryPlugin plugin = AiryPlugin.getInstance();

    private final CommandManager manager;
    private final String name;
    private final List<String> aliases;
    private final List<Method> defaultHandlers;
    private final Map<String, List<Method>> subCommands;
    private final T commandClass;
    private final Map<String, IArgumentConverter<?>> converters;

    public VelocityCommandHandler(ProxyServer server, CommandManager commandManager, String name, List<String> aliases, List<Method> defaultHandlers, Map<String, List<Method>> subCommands, T commandClass, Map<String, IArgumentConverter<?>> converters) {
        this.server = server;
        this.manager = commandManager;
        this.name = name;
        this.aliases = aliases;
        this.defaultHandlers = defaultHandlers;
        this.subCommands = subCommands;
        this.commandClass = commandClass;
        this.converters = converters;
    }

    @Override
    public void execute(Invocation invocation) {
        String label = invocation.alias();
        String[] args = invocation.arguments();
        CommandSource sender = invocation.source();

        if (!label.equalsIgnoreCase(getName()))
            return;

        execute(manager.getCommandSender(sender), args);
    }

    @Override
    public boolean handleCommand(Method command, ICommandSender sender, List<Object> objects, Parameter[] parameters) {
        if (sender.getSender() instanceof Player player && isParamPlayer(parameters[0])) {
            return handlePlayer(command, player, objects);
        } else if (sender.getSender() instanceof ConsoleCommandSource consoleSender && isParamConsole(parameters[0])) {
            return handleConsole(command, consoleSender, objects);
        } else if (isParamSource(parameters[0])) {
            return handleGeneric(command, sender.getSender(), objects);
        }

        ((CommandSource) sender.getSender()).sendPlainMessage("You cannot use this command.");

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

    private boolean handleConsole(Method method, ConsoleCommandSource console, List<Object> objects) {
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

    private boolean handleGeneric(Method method, CommandSource source, List<Object> objects) {
        objects.addFirst(source);

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
        return parameter.getType() == ConsoleCommandSource.class;
    }

    private boolean isParamSource(Parameter parameter) {
        return parameter.getType() == CommandSource.class;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean isSubCommand(String subCommand) {
        return subCommands.containsKey(subCommand);
    }

    @Override
    public List<Method> getSubCommands(String subCommandName) {
        return subCommands.get(subCommandName);
    }

    public List<Method> getDefaultHandlers() {
        return defaultHandlers;
    }

    @Override
    public @Nullable IArgumentConverter<?> getConverter(String typeName) {
        return converters.getOrDefault(typeName, null);
    }

    public Map<String, List<Method>> getSubCommands() {
        return subCommands;
    }
}