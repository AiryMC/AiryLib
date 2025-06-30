package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.argument.IArgument;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ICommandExecutor {

    default void callDefaultHandler(ICommandSender sender, Method handler, Object handlerInstance) {
        Object[] parsedArgs = new Object[handler.getParameterCount()];

        parsedArgs[0] = sender.getSender();

        try {
            handler.invoke(handlerInstance, parsedArgs);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("§cAn error occurred while executing this command.");
        }
    }

    default void callHandler(
            ICommandSender sender,
            Method handler,
            Object handlerInstance,
            String[] args,
            CommandManager commandManager,
            String commandName,
            String subcommandName
    ) {
        Object[] parsedArgs = new Object[handler.getParameterCount()];
        Parameter[] params = handler.getParameters();

        if (params.length == 0 || !params[0].getType().isInstance(sender.getSender())) {
            sender.sendMessage("§cInternal error: Command method is not valid.");
            return;
        }

        parsedArgs[0] = sender.getSender(); // First argument is always sender

        int totalParams = params.length - 1; // excluding sender
        int providedArgs = args.length - 1;

        boolean isVarargs = handler.isVarArgs();
        int requiredArgs = 0;

        // Count only required parameters
        for (int i = 1; i < params.length; i++) {
            requiredArgs++;
        }

        // Validate argument count
        if (providedArgs < requiredArgs) {
            String usage = UsageGenerator.generateUsage("test", subcommandName, handler);
            sender.sendMessage("§cUsage: " + usage);
            return;
        }

        if (!isVarargs && providedArgs > totalParams) {
            String usage = UsageGenerator.generateUsage("test", subcommandName, handler);
            sender.sendMessage("§cUsage: " + usage);
            return;
        }

        for (int i = 1; i < params.length; i++) {
            Class<?> type = params[i].getType();
            IArgument<?> parser = commandManager.getArgumentParser(type);
            if (parser == null) {
                sender.sendMessage("§cInternal error: unsupported argument type.");
                throw new IllegalArgumentException("No parser for type: " + type.getSimpleName());
            }

            try {
                parsedArgs[i] = parser.parse(args[i]);
            } catch (Exception e) {
                sender.sendMessage("§cInvalid " + type.getSimpleName() + ": §f'" + args[i] + "'");
                return;
            }
        }

        try {
            handler.invoke(handlerInstance, parsedArgs);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("§cAn error occurred while executing this command.");
        }
    }
}
