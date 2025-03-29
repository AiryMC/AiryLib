package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.command.arguments.ArgumentConverter;
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
import java.util.List;
import java.util.Map;

public class PaperCommandHandler<T> extends Command {

    private final JavaPlugin plugin;
    private final String name;
    private final List<String> aliases;
    @Nullable
    private final Method defaultHandler;
    private final T commandClass;
    private final Map<String, ArgumentConverter<?>> converters;

    public PaperCommandHandler(JavaPlugin plugin, String name, List<String> aliases, @Nullable Method defaultHandler, T commandClass, Map<String, ArgumentConverter<?>> converters) {
        super(name, "todo", "todo", aliases);

        this.plugin = plugin;
        this.name = name;
        this.aliases = aliases;
        this.defaultHandler = defaultHandler;
        this.commandClass = commandClass;
        this.converters = converters;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!label.equalsIgnoreCase(getName()))
            return true;

        if (getDefaultHandler() != null) {
            if (sender instanceof Player player) {
                try {
                    handlePlayer(getDefaultHandler(), player, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }

        return true;
    }

    private void handlePlayer(Method method, Player player, String[] args) throws Exception {
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

            plugin.getLogger().info("Parameter type: " + parameter.getType().getTypeName());
            if (!converters.containsKey(parameter.getType().getTypeName())) {
                return null;
            }
            ArgumentConverter<?> converter = converters.get(parameter.getType().getTypeName());
            if (!converter.canConvert(arg))
                return null;

            params.add(converter.from(arg));
        }

        return params;
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

    public @Nullable Method getDefaultHandler() {
        return defaultHandler;
    }
}
