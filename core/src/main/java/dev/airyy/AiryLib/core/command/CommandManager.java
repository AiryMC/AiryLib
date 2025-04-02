package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.arguments.IArgumentConverter;

import java.util.HashMap;
import java.util.Map;

public abstract class CommandManager {

    private final Map<String, IArgumentConverter<?>> converters;

    protected CommandManager() {
        this.converters = new HashMap<>();
    }

    public abstract <T> void registerCommand(T command);

    public abstract void registerArgument(Class<?> type, IArgumentConverter<?> argument);

    protected Map<String, IArgumentConverter<?>> getConverters() {
        return converters;
    }
}
