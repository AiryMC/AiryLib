package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.command.annotations.SubCommand;
import dev.airyy.AiryLib.command.arguments.ArgumentConverter;
import dev.airyy.AiryLib.utils.Strings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private final Map<String, ArgumentConverter<?>> converters;

    public PaperCommandHandler(JavaPlugin plugin, String name, List<String> aliases, List<Method> defaultHandlers, Map<String, List<Method>> subCommands, T commandClass, Map<String, ArgumentConverter<?>> converters) {
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

        if (sender instanceof Player player) {
            if (args.length > 0) {
                String subCommandName = args[0];
                if (!subCommands.containsKey(subCommandName))
                    return true;

                List<String> filteredArgs = Arrays.stream(args).skip(1).toList();
                List<Method> subCommandMethods = subCommands.get(subCommandName);

                // Sort methods by match score, highest first
                subCommandMethods.sort((a, b) -> Integer.compare(getMatchScore(b, filteredArgs.toArray(new String[0])), getMatchScore(a, filteredArgs.toArray(new String[0]))));

                for (Method subCommand : subCommandMethods) {
                    int matchScore = getMatchScore(subCommand, filteredArgs.toArray(new String[0]));
                    if (matchScore == -1)
                        continue;

                    try {
                        handlePlayer(subCommand, player, filteredArgs.toArray(new String[0]));
                        return true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            defaultHandlers.sort((a, b) -> Integer.compare(getMatchScore(b, args), getMatchScore(a, args)));
            for (Method defaultHandler : getDefaultHandlers()) {
                if (!checkArguments(defaultHandler, args))
                    continue;

                try {
                    handlePlayer(defaultHandler, player, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
    }

    private void handlePlayer(Method method, Player player, String[] args) throws Exception {
        if (!checkArguments(method, args)) {
            return;
        }

        List<Object> objects = getParams(method, args);
        if (objects == null) {
            return;
        }

        // If the first parameter is of player type and there are no args
        if (method.getParameters().length > 0 && !isParamPlayer(method.getParameters()[0]) && args.length == 0) {
            return;
        }

        if (args.length != method.getParameters().length - 1) {
            return;
        }

        objects.addFirst(player);

        try {
            if (method.getParameters().length > 0 && !objects.isEmpty()) {
                method.invoke(commandClass, objects.toArray());
            } else {
                method.invoke(commandClass);
            }
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
            if (isParamPlayer(parameter) && i == 0)
                continue;

            if (argIndex >= args.length)
                return false;

            String arg = args[argIndex++];

            String typeName = parameter.getType().getTypeName();

            if (typeName.equals(String.class.getTypeName())) {
                // If it's a string, it's always valid
                continue;
            }

            ArgumentConverter<?> converter = converters.get(typeName);
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
            if (isParamPlayer(parameter) && i == 0)
                continue;

            if (args.length == 0) {
                break;
            }
            String arg = args[i > 0 ? i - 1 : i];

            if (!converters.containsKey(parameter.getType().getTypeName())) {
                plugin.getLogger().warning("Argument converter not found for type: " + parameter.getType().getTypeName());
                return null;
            }
            ArgumentConverter<?> converter = converters.get(parameter.getType().getTypeName());
            if (!converter.canConvert(arg) && !Strings.isNumeric(arg)) {
                plugin.getLogger().warning("Could not convert argument \"" + arg + "\".");
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
                ArgumentConverter<?> converter = converters.get(typeName);
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
        return parameter.getType().getTypeName().equals(Player.class.getTypeName());
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
