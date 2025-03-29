package dev.airyy.AiryLib.command;

import dev.airyy.AiryLib.command.arguments.ArgumentConverter;
import dev.airyy.AiryLib.command.arguments.IntegerArgument;

public abstract class CommandManager {

    public abstract <T> void registerCommand(T command);
    public abstract void registerArgument(Class<?> type, ArgumentConverter<?> argument);
}
