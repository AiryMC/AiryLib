package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.arguments.ArgumentConverter;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandManager {

    private final Map<String, ArgumentConverter<?>> converters;

    protected CommandManager() {
        this.converters = new HashMap<>();
    }

    public abstract <T> void registerCommand(T command);
    public abstract void registerArgument(Class<?> type, ArgumentConverter<?> argument);

    protected Map<String, ArgumentConverter<?>> getConverters() {
        return converters;
    }
}
