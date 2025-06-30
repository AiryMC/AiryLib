package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.ICommandExecutor;
import dev.airyy.AiryLib.core.command.ICommandSender;
import dev.airyy.AiryLib.core.command.argument.IArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class PaperCommand extends Command implements ICommandExecutor {
    private final Object handlerInstance;
    private final Method defaultHandler;
    private final Map<String, Method> subCommands;
    private final CommandManager commandManager;

    public PaperCommand(String name, Object instance, Method defaultHandler, Map<String, Method> subcommands, CommandManager commandManager) {
        super(name);
        this.handlerInstance = instance;
        this.defaultHandler = defaultHandler;
        this.subCommands = subcommands;
        this.commandManager = commandManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        ICommandSender baseSender = new PaperCommandSender(sender);

        if (args.length == 0) {
            callDefaultHandler(baseSender, defaultHandler, handlerInstance);
            return true;
        }

        String sub = args[0].toLowerCase();
        Method method = subCommands.get(sub);
        if (method == null) {
            sender.sendMessage(Component.text("§cUnknown subcommand."));
            return true;
        }

        try {
            callHandler(baseSender, method, handlerInstance, args, commandManager, label, sub);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(Component.text("Error: " + e.getMessage()));
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String @NotNull [] args) throws IllegalArgumentException {
        if (args.length == 0) return List.of();

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

        if (paramIndex == 0) return List.of();

        Parameter param = parameters[paramIndex];
        IArgument<?> parser = commandManager.getArgumentParser(param.getType());
        if (parser == null) return List.of();

        return parser.suggest(args[args.length - 1]);
    }
}
