package dev.airyy.AiryLib.core.command.argument.impl;

import dev.airyy.AiryLib.core.command.argument.IArgument;
import dev.airyy.AiryLib.core.command.exception.ArgumentParseException;

import java.util.Arrays;
import java.util.List;

public class BooleanArgument implements IArgument<Boolean> {

    @Override
    public Boolean parse(String input) {
        String normalized = input.trim().toLowerCase();

        return switch (normalized) {
            case "true", "yes", "on", "1" -> true;
            case "false", "no", "off", "0" -> false;
            default -> throw new ArgumentParseException("Invalid boolean: " + input +
                    ". Use true/false, yes/no, on/off, or 1/0.");
        };
    }

    @Override
    public List<String> suggest(String input) {
        return Arrays.asList("true", "false");
    }
}
