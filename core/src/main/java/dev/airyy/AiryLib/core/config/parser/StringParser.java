package dev.airyy.AiryLib.core.config.parser;

public class StringParser implements IConfigParser<String> {
    @Override
    public String parse(Object input) {
        if (!(input instanceof String string))
            throw new IllegalArgumentException("Expected type of String for String");

        return string;
    }

    @Override
    public Object serialize(String value) {
        return value;
    }
}
