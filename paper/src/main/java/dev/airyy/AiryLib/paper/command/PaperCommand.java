package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.CommandManager;
import dev.airyy.AiryLib.core.command.ICommandExecutor;
import dev.airyy.AiryLib.core.command.ICommandSender;
import dev.airyy.AiryLib.core.command.annotation.Permission;
import dev.airyy.AiryLib.core.command.argument.IArgument;
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
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String @NotNull [] args) {
        ICommandSender baseSender = new PaperCommandSender(sender);

        execute(baseSender, args, handlerInstance, defaultHandler, subCommands, commandManager, label);
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

    @Override
    public boolean testPermission(@NotNull CommandSender target) {
        if (!handlerInstance.getClass().isAnnotationPresent(Permission.class))
            return super.testPermission(target);

        Permission permission = handlerInstance.getClass().getAnnotation(Permission.class);
        return target.hasPermission(permission.value());
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        if (!handlerInstance.getClass().isAnnotationPresent(Permission.class))
            return super.testPermission(target);

        Permission permission = handlerInstance.getClass().getAnnotation(Permission.class);
        return target.hasPermission(permission.value());
    }
}
