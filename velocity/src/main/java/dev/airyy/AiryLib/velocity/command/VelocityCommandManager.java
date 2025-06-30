package dev.airyy.AiryLib.velocity.command;

import com.velocitypowered.api.command.CommandMeta;
import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.annotation.Command;
import dev.airyy.AiryLib.core.command.annotation.Default;
import dev.airyy.AiryLib.core.command.annotation.Handler;
import dev.airyy.AiryLib.core.command.argument.IArgument;
import dev.airyy.AiryLib.velocity.AiryPlugin;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class VelocityCommandManager extends CommandManager {

    private final AiryPlugin plugin;
    private final com.velocitypowered.api.command.CommandManager commandManager;

    private final Map<Class<?>, IArgument<?>> argumentParsers;

    public VelocityCommandManager(AiryPlugin plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getServer().getCommandManager();

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

        CommandMeta commandMeta = commandManager.metaBuilder(commandName)
                .plugin(plugin)
                .build();

        VelocityCommand cmd = new VelocityCommand(commandName, commandHandler, defaultHandler, subcommands, this);

        commandManager.register(commandMeta, cmd);
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
}
