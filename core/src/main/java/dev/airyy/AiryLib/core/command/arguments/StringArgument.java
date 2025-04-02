package dev.airyy.AiryLib.core.command.arguments;

import dev.airyy.AiryLib.core.utils.Strings;

public class StringArgument implements IArgumentConverter<String> {
    @Override
    public String from(String string) {
        return string;
    }

    @Override
    public String to(String object) {
        return object;
    }

    @Override
    public boolean canConvert(String string) {
        return !Strings.isNumeric(string);
    }

    @Override
    public boolean isValid(Class<?> clazz) {
        return clazz == String.class;
    }
}
