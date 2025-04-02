package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.arguments.ArgumentConverter;

public abstract class CommandManager {

    public abstract <T> void registerCommand(T command);
    public abstract void registerArgument(Class<?> type, ArgumentConverter<?> argument);
}
