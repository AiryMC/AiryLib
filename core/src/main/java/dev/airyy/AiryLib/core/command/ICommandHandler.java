package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.arguments.IArgumentConverter;
import dev.airyy.AiryLib.core.utils.Strings;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ICommandHandler {

    boolean handleCommand(Method command, ICommandSender sender, List<Object> objects, Parameter[] parameters);

    boolean isSubCommand(String subCommand);

    List<Method> getSubCommands(String subCommandName);

    List<Method> getDefaultHandlers();

    @Nullable IArgumentConverter<?> getConverter(String typeName);

    default boolean checkCommandCall(Method command, String[] args, List<Object> objects) {
        if (!checkArguments(command, args)) {
            return false;
        }

        if (objects == null) {
            return false;
        }

        Parameter[] parameters = command.getParameters();
        if (parameters.length == 0) {
            return false;
        }

        return args.length == command.getParameters().length - 1;
    }

    default void execute(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            String subCommandName = args[0];
            if (isSubCommand(subCommandName)) {
                List<String> filteredArgs = Arrays.stream(args).skip(1).toList();
                List<Method> subCommandMethods = getSubCommands(subCommandName);

                // Sort methods by match score, highest first
                subCommandMethods.sort((a, b) -> Integer.compare(getMatchScore(b, filteredArgs.toArray(new String[0])), getMatchScore(a, filteredArgs.toArray(new String[0]))));

                for (Method subCommand : subCommandMethods) {
                    if (Modifier.isPrivate(subCommand.getModifiers()) || Modifier.isProtected(subCommand.getModifiers()))
                        continue;

                    int matchScore = getMatchScore(subCommand, filteredArgs.toArray(new String[0]));
                    if (matchScore == -1)
                        continue;

                    try {
                        String[] newArgs = filteredArgs.toArray(new String[0]);
                        List<Object> objects = getParams(subCommand, newArgs);

                        if (!checkCommandCall(subCommand, newArgs, objects)) {
                            System.out.println("Command call check failed!");
                            return;
                        }

                        if (handleCommand(subCommand, sender, objects, subCommand.getParameters())) {
                            return;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        getDefaultHandlers().sort((a, b) -> Integer.compare(getMatchScore(b, args), getMatchScore(a, args)));
        for (Method defaultHandler : getDefaultHandlers()) {
            if (Modifier.isPrivate(defaultHandler.getModifiers()) || Modifier.isProtected(defaultHandler.getModifiers()))
                continue;

            if (!checkArguments(defaultHandler, args))
                continue;

            try {
                List<Object> objects = getParams(defaultHandler, args);
                if (!checkCommandCall(defaultHandler, args, objects)) {
                    System.out.println("Command call check failed!");
                    return;
                }

                if (handleCommand(defaultHandler, sender, objects, defaultHandler.getParameters())) {
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    default boolean checkArguments(Method method, String[] args) {
        if (args.length == 0)
            return true;

        Parameter[] parameters = method.getParameters();

        for (int i = 0, argIndex = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // Skip player parameter if it's the first argument
            if (i == 0)
                continue;

            if (argIndex >= args.length) {
                return false;
            }

            String arg = args[argIndex++];

            String typeName = parameter.getType().getTypeName();

            if (typeName.equals(String.class.getTypeName())) {
                // If it's a string, it's always valid
                continue;
            }

            IArgumentConverter<?> converter = getConverter(typeName);
            if (converter == null || !converter.canConvert(arg)) {
                return false;
            }
        }

        return true;
    }

    default @Nullable List<Object> getParams(Method method, String[] args) throws Exception {
        List<Object> params = new ArrayList<>();

        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            if (i == 0)
                continue;

            if (args.length == 0) {
                break;
            }
            String arg = args[i - 1];

            IArgumentConverter<?> converter = getConverter(parameter.getType().getTypeName());
            if (converter == null) {
                System.out.println("Argument converter not found for type: " + parameter.getType().getTypeName());
                // plugin.getLogger().warning("Argument converter not found for type: " + parameter.getType().getTypeName());
                return null;
            }


            if (!converter.canConvert(arg) && !Strings.isNumeric(arg)) {
                System.out.println("Could not convert argument \"" + arg + "\".");
                // plugin.getLogger().warning("Could not convert argument \"" + arg + "\".");
                return null;
            }

            if (!converter.isValid(parameter.getType())) {
                System.out.println("Could not convert argument \"" + arg + "\".");
                return null;
            }

            params.add(converter.from(arg));
        }

        return params;
    }

    default int getMatchScore(Method method, String[] args) {
        Parameter[] parameters = method.getParameters();
        int score = 0;

        for (int i = 0, argIndex = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (i == 0) continue;
            if (argIndex >= args.length) return -1;

            String arg = args[argIndex++];
            String typeName = parameter.getType().getTypeName();

            if (typeName.equals(String.class.getTypeName())) {
                score += 1;
            } else {
                IArgumentConverter<?> converter = getConverter(typeName);
                if (converter != null && converter.canConvert(arg)) {
                    score += 2;
                } else {
                    return -1;
                }
            }
        }

        return score;
    }
}
