package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.annotation.OptionalArg;
import dev.airyy.AiryLib.core.command.argument.IArgument;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public interface ICommandExecutor {

    default void execute(ICommandSender sender, String[] args, Object handlerInstance, Method defaultHandler, Map<String, Method> subCommands, CommandManager commandManager, String commandName) {
        if (args.length == 0) {
            if (defaultHandler != null) {
                callDefaultHandler(sender, defaultHandler, handlerInstance);
            } else {
                sender.sendMessage("§cNo subcommand specified. Use /" + commandName + " help");
            }
            return;
        }

        String sub = args[0].toLowerCase();
        Method method = subCommands.get(sub);
        if (method == null) {
            sender.sendMessage("§cUnknown subcommand.");
            return;
        }

        try {
            callHandler(sender, method, handlerInstance, args, commandManager, commandName, sub);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage("Error: " + e.getMessage());
        }
    }

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
            if (!params[i].isAnnotationPresent(OptionalArg.class)) {
                requiredArgs++;
            }
        }

        // Validate argument count
        if (providedArgs < requiredArgs) {
            String usage = UsageGenerator.generateUsage(commandName, subcommandName, handler);
            sender.sendMessage("§cUsage: " + usage);
            return;
        }

        if (!isVarargs && providedArgs > totalParams) {
            String usage = UsageGenerator.generateUsage(commandName, subcommandName, handler);
            sender.sendMessage("§cUsage: " + usage);
            return;
        }

        for (int i = 1; i < params.length; i++) {
            int argIndex = i; // args[1] is first user arg (after subcommand)

            Class<?> type = params[i].getType();
            IArgument<?> parser = commandManager.getArgumentParser(type);
            if (parser == null) {
                throw new IllegalArgumentException("No parser for type: " + type.getSimpleName());
            }

            boolean isOptional = params[i].isAnnotationPresent(OptionalArg.class);
            OptionalArg optional = params[i].getAnnotation(OptionalArg.class);

            if (argIndex >= args.length) {
                if (isOptional) {
                    // Use default value or null
                    String defaultRaw = optional.defaultValue();
                    parsedArgs[i] = defaultRaw.isEmpty() ? null : parser.parse(defaultRaw);
                    continue;
                } else {
                    sender.sendMessage("§cMissing argument: " + type.getSimpleName());
                    return;
                }
            }

            try {
                parsedArgs[i] = parser.parse(args[argIndex]);
            } catch (Exception e) {
                sender.sendMessage("§cInvalid " + type.getSimpleName() + ": §f" + args[argIndex]);
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
