package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.annotation.Command;
import dev.airyy.AiryLib.core.command.annotation.Default;
import dev.airyy.AiryLib.core.command.annotation.Handler;
import dev.airyy.AiryLib.core.command.argument.IArgument;
import dev.airyy.AiryLib.paper.AiryPlugin;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class PaperCommandManager extends CommandManager {

    private final AiryPlugin plugin;
    private final CommandMap commandMap;

    private final Map<Class<?>, IArgument<?>> argumentParsers;

    public PaperCommandManager(AiryPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = getCommandMap();
        argumentParsers = new HashMap<>();
    }

    @Override
    public <T> void register(T commandHandler) {
        Class<?> clazz = commandHandler.getClass();
        Command command = clazz.getAnnotation(Command.class);
        if (command == null) return;

        String commandName = command.value();
        Map<String, Method> subcommands = new HashMap<>();

        Method defaultHandler = null;

        for (Method method : clazz.getDeclaredMethods()) {
            Default newDefaultHandler = method.getAnnotation(Default.class);
            if (newDefaultHandler != null) {
                defaultHandler = method;
                continue;
            }

            Handler handler = method.getAnnotation(Handler.class);
            if (handler != null) {
                subcommands.put(handler.value().toLowerCase(), method);
            }
        }

        PaperCommand cmd = new PaperCommand(commandName, commandHandler, defaultHandler, subcommands, this);

        commandMap.register(plugin.getName(), cmd);
    }

    @Override
    public <T> void registerArgumentParser(Class<T> clazz, IArgument<T> parser) {
        if (argumentParsers.containsKey(clazz)) {
            throw new IllegalArgumentException("Cannot register more than 1 identical parser");
        }

        argumentParsers.put(clazz, parser);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> IArgument<T> getArgumentParser(Class<T> type) {
        if (!argumentParsers.containsKey(type)) {
            throw new IllegalArgumentException("Argument map does not contain the specified type: " + type.getName());
        }

        return (IArgument<T>) argumentParsers.get(type);
    }

    private CommandMap getCommandMap() {
        try {
            Field field = plugin.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(plugin.getServer());
        } catch (Exception e) {
            throw new RuntimeException("Could not get CommandMap", e);
        }
    }
}
