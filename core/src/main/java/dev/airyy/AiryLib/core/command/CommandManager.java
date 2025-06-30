package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.argument.IArgument;

/**
 * An abstract manager responsible for registering command handler objects.
 * Subclasses should implement the logic for how commands are stored and
 * invoked.
 */
public abstract class CommandManager {

    /**
     * Registers a command handler.
     *
     * @param <T>            the type of the command object
     * @param commandHandler the command instance to register
     */
    public abstract <T> void register(T commandHandler);

    /**
     * Registers an argument parser for a specific target type.
     *
     * <p>The parser will be used to convert raw string arguments into the appropriate
     * Java object during command execution. Each parser is typically associated with
     * the type it parses via {@link IArgument#getType()}.
     *
     * @param <T>    the target type the parser handles
     * @param clazz the {@link Class<T>} instance to register
     * @param parser the {@link IArgument} instance to register
     * @throws IllegalStateException if a parser for the same type is already registered
     */
    public abstract <T> void registerArgumentParser(Class<T> clazz, IArgument<T> parser);

    /**
     * Retrieves an argument parser capable of converting a string input into the specified type.
     *
     * <p>This method is used during command execution to parse raw string arguments
     * (e.g., from the chat) into typed Java objects expected by the handler methods.
     *
     * @param <T>  the target type to parse into
     * @param type the class representing the target type
     * @return an {@link IArgument} instance for the specified type
     * @throws IllegalArgumentException if no parser is registered for the given type
     */
    public abstract <T> IArgument<T> getArgumentParser(Class<T> type);
}
