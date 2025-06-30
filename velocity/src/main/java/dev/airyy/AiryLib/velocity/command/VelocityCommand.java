package dev.airyy.AiryLib.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.ICommandExecutor;
import dev.airyy.AiryLib.core.command.ICommandSender;
import dev.airyy.AiryLib.core.command.argument.IArgument;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class VelocityCommand implements SimpleCommand, ICommandExecutor {
    private final Object handlerInstance;
    private final Method defaultHandler;
    private final Map<String, Method> subCommands;
    private final CommandManager commandManager;

    public VelocityCommand(String name, Object instance, Method defaultHandler, Map<String, Method> subcommands, CommandManager commandManager) {
        this.handlerInstance = instance;
        this.defaultHandler = defaultHandler;
        this.subCommands = subcommands;
        this.commandManager = commandManager;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        CommandSource sender = invocation.source();
        ICommandSender baseSender = new VelocityCommandSender(sender);

        if (args.length == 0) {
            callDefaultHandler(baseSender, defaultHandler, handlerInstance);
            return;
        }

        String sub = args[0].toLowerCase();
        Method method = subCommands.get(sub);
        if (method == null) {
            sender.sendMessage(Component.text("§cUnknown subcommand."));
            return;
        }

        try {
            callHandler(baseSender, method, handlerInstance, args, commandManager, invocation.alias(), sub);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Component.text("Error: " + e.getMessage()));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) return List.copyOf(subCommands.keySet());

        if (args.length == 1) {
            // Suggest subcommands
            return subCommands.keySet().stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        String sub = args[0].toLowerCase();
        Method method = subCommands.get(sub);
        if (method == null) return List.of();

        Parameter[] parameters = method.getParameters();

        if (args.length - 1 >= parameters.length) return List.of();

        int paramIndex = args.length - 1;

        Parameter param = parameters[paramIndex];
        IArgument<?> parser = commandManager.getArgumentParser(param.getType());
        if (parser == null) return List.of();

        List<String> suggestions = parser.suggest(args[args.length - 1]);

        return suggestions.stream().filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(suggest(invocation));
    }
}
