package dev.airyy.AiryLib.core.command.argument;

import java.util.Collections;
import java.util.List;

/**
 * Represents a parser that converts a raw string into a typed value.
 *
 * <p>This interface is used to parse individual command arguments from their
 * raw string representation into the expected type {@code T}.
 *
 * @param <T> the type the input string should be parsed into
 */
public interface IArgument<T> {

    /**
     * Parses the given input string into an instance of type {@code T}.
     *
     * @param input the raw string input from the command
     * @return the parsed value
     * @throws IllegalArgumentException if the input cannot be parsed
     */
    T parse(String input);

    default List<String> suggest(String input) {
        return Collections.emptyList();
    }
}
