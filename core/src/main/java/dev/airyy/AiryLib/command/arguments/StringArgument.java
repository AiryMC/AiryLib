package dev.airyy.AiryLib.command.arguments;

import dev.airyy.AiryLib.utils.Strings;

public class StringArgument implements ArgumentConverter<String> {
    @Override
    public String from(String string) throws Exception {
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
}
