package dev.airyy.AiryLib.velocity.command;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.airyy.AiryLib.command.CommandManager;
import dev.airyy.AiryLib.command.annotations.Command;
import dev.airyy.AiryLib.command.annotations.Default;
import dev.airyy.AiryLib.command.annotations.SubCommand;
import dev.airyy.AiryLib.command.arguments.ArgumentConverter;
import dev.airyy.AiryLib.command.arguments.IntegerArgument;
import dev.airyy.AiryLib.command.arguments.StringArgument;
import dev.airyy.AiryLib.utils.Annotations;
import dev.airyy.AiryLib.velocity.AiryPlugin;
import dev.airyy.AiryLib.velocity.command.arguments.PlayerArgument;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

public class VelocityCommandManager extends CommandManager {

    private final ProxyServer server;
    private final AiryPlugin plugin = AiryPlugin.getInstance();

    private final Map<String, ArgumentConverter<?>> converters;

    public VelocityCommandManager(ProxyServer server) {
        super();

        this.server = server;
        this.converters = new HashMap<>();

        registerArgument(int.class, new IntegerArgument());
        registerArgument(String.class, new StringArgument());
        registerArgument(Player.class, new PlayerArgument());
    }

    @Override
    public <T> void registerCommand(T command) {
        if (!Annotations.hasAnnotation(command.getClass(), Command.class)) {
            plugin.getLogger().warn("Cannot register a non command class");
            return;
        }

        Command rootCommand = command.getClass().getAnnotation(Command.class);
        if (server.getCommandManager().hasCommand(rootCommand.value())) {
            plugin.getLogger().warn("There can only be one class instance per command");
            return;
        }

        if (rootCommand.value().contains(" ")) {
            plugin.getLogger().warn("There cannot be any whitespace in the command name");
            return;
        }

        List<Method> defaultHandlers = getDefaultHandlers(command);
        Map<String, List<Method>> subCommands = getSubCommands(command);

        VelocityCommandHandler<T> commandHandler = new VelocityCommandHandler<>(server, rootCommand.value(), Arrays.stream(rootCommand.aliases()).toList(), defaultHandlers, subCommands, command, converters);
        CommandMeta meta = server.getCommandManager().metaBuilder(rootCommand.value())
                .plugin(plugin)
                .build();

        server.getCommandManager().register(meta, commandHandler);
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
        converters.put(type.getTypeName(), argument);
    }
}
