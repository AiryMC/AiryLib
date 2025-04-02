package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.arguments.IArgumentConverter;
import dev.airyy.AiryLib.core.utils.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PaperCommandHandler<T> extends Command {

    private final JavaPlugin plugin;
    private final String name;
    private final List<String> aliases;
    private final List<Method> defaultHandlers;
    private final Map<String, List<Method>> subCommands;
    private final T commandClass;
    private final Map<String, IArgumentConverter<?>> converters;

    public PaperCommandHandler(JavaPlugin plugin, String name, List<String> aliases, List<Method> defaultHandlers, Map<String, List<Method>> subCommands, T commandClass, Map<String, IArgumentConverter<?>> converters) {
        super(name, "todo", "todo", aliases);

        this.plugin = plugin;
        this.name = name;
        this.aliases = aliases;
        this.defaultHandlers = defaultHandlers;
        this.subCommands = subCommands;
        this.commandClass = commandClass;
        this.converters = converters;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!label.equalsIgnoreCase(getName()))
            return true;

        if (args.length > 0) {
            String subCommandName = args[0];
            if (subCommands.containsKey(subCommandName)) {
                List<String> filteredArgs = Arrays.stream(args).skip(1).toList();
                List<Method> subCommandMethods = subCommands.get(subCommandName);

                // Sort methods by match score, highest first
                subCommandMethods.sort((a, b) -> Integer.compare(getMatchScore(b, filteredArgs.toArray(new String[0])), getMatchScore(a, filteredArgs.toArray(new String[0]))));

                for (Method subCommand : subCommandMethods) {
                    if (Modifier.isPrivate(subCommand.getModifiers()) || Modifier.isProtected(subCommand.getModifiers()))
                        continue;

                    int matchScore = getMatchScore(subCommand, filteredArgs.toArray(new String[0]));
                    if (matchScore == -1)
                        continue;

                    try {
                        if (handleCommand(subCommand, sender, filteredArgs.toArray(new String[0]), false)) {
                            return true;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        defaultHandlers.sort((a, b) -> Integer.compare(getMatchScore(b, args), getMatchScore(a, args)));
        for (Method defaultHandler : getDefaultHandlers()) {
            if (Modifier.isPrivate(defaultHandler.getModifiers()) || Modifier.isProtected(defaultHandler.getModifiers()))
                continue;

            if (!checkArguments(defaultHandler, args))
                continue;

            try {
                if (handleCommand(defaultHandler, sender, args, true)) {
                    return true;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    private boolean handleCommand(Method method, CommandSender sender, String[] args, boolean isDefault) throws Exception {
        if (!checkArguments(method, args)) {
            return false;
        }

        List<Object> objects = getParams(method, args);
        if (objects == null) {
            return false;
        }

        Parameter[] parameters = method.getParameters();
        if (parameters.length > 0 && args.length == 0 && !isDefault) {
            return false;
        }

        if (args.length != method.getParameters().length - 1) {
            return false;
        }

        if (sender instanceof Player player && isParamPlayer(parameters[0])) {
            return handlePlayer(method, player, objects);
        } else if (sender instanceof ConsoleCommandSender consoleSender && isParamConsole(parameters[0])) {
            return handleConsole(method, consoleSender, objects);
        } else if (isParamSender(parameters[0])) {
            return handleGeneric(method, sender, objects);
        }

        sender.sendPlainMessage("You cannot use this command.");

        return true;
    }

    private boolean handlePlayer(Method method, Player player, List<Object> objects) throws Exception {
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

    private boolean handleConsole(Method method, ConsoleCommandSender console, List<Object> objects) throws Exception {
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

    private boolean handleGeneric(Method method, CommandSender sender, List<Object> objects) throws Exception {
        objects.addFirst(sender);

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

    private boolean checkArguments(Method method, String[] args) {
        if (args.length == 0)
            return true;

        Parameter[] parameters = method.getParameters();

        for (int i = 0, argIndex = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // Skip player parameter if it's the first argument
            if (i == 0)
                continue;

            if (argIndex >= args.length)
                return false;

            String arg = args[argIndex++];

            String typeName = parameter.getType().getTypeName();

            if (typeName.equals(String.class.getTypeName())) {
                // If it's a string, it's always valid
                continue;
            }

            IArgumentConverter<?> converter = converters.get(typeName);
            if (converter == null || !converter.canConvert(arg)) {
                return false;
            }
        }

        return true;
    }

    private @Nullable List<Object> getParams(Method method, String[] args) throws Exception {
        List<Object> params = new ArrayList<>();

        for (int i = 0; i < method.getParameters().length; i++) {
            Parameter parameter = method.getParameters()[i];
            if (i == 0)
                continue;

            if (args.length == 0) {
                break;
            }
            String arg = args[i - 1];

            if (!converters.containsKey(parameter.getType().getTypeName())) {
                plugin.getLogger().warning("Argument converter not found for type: " + parameter.getType().getTypeName());
                return null;
            }

            IArgumentConverter<?> converter = converters.get(parameter.getType().getTypeName());
            if (!converter.canConvert(arg) && !Strings.isNumeric(arg)) {
                plugin.getLogger().warning("Could not convert argument \"" + arg + "\".");
                return null;
            }

            if (converter.isValid(parameter.getType())) {
                return null;
            }

            params.add(converter.from(arg));
        }

        return params;
    }

    private int getMatchScore(Method method, String[] args) {
        Parameter[] parameters = method.getParameters();
        int score = 0;

        for (int i = 0, argIndex = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // Skip player parameter
            if (isParamPlayer(parameter) && i == 0) continue;
            if (argIndex >= args.length) return -1;

            String arg = args[argIndex++];
            String typeName = parameter.getType().getTypeName();

            if (typeName.equals(String.class.getTypeName())) {
                score += 1;
            } else {
                IArgumentConverter<?> converter = converters.get(typeName);
                if (converter != null && converter.canConvert(arg)) {
                    score += 2;
                } else {
                    return -1;
                }
            }
        }

        return score;
    }

    private boolean isParamPlayer(Parameter parameter) {
        return parameter.getType() == Player.class;
    }

    private boolean isParamConsole(Parameter parameter) {
        return parameter.getType() == ConsoleCommandSender.class;
    }

    private boolean isParamSender(Parameter parameter) {
        return parameter.getType() == CommandSender.class;
    }


    public @NotNull String getName() {
        return name;
    }

    public @NotNull List<String> getAliases() {
        return aliases;
    }

    public List<Method> getDefaultHandlers() {
        return defaultHandlers;
    }

    public Map<String, List<Method>> getSubCommands() {
        return subCommands;
    }
}
