package dev.airyy.AiryLib.core.command.argument.impl;

import dev.airyy.AiryLib.core.command.argument.IArgument;
import dev.airyy.AiryLib.core.command.exception.ArgumentParseException;

public class IntegerArgument implements IArgument<Integer> {
    @Override
    public Integer parse(String input) {
        try {
            // Try int first
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new ArgumentParseException("Expected an int, but got: " + input);
        }
    }
}
