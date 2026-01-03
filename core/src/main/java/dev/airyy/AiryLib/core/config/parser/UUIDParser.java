package dev.airyy.AiryLib.core.config.parser;

import java.util.UUID;

public class UUIDParser implements IConfigParser<UUID> {
    @Override
    public UUID parse(Object input) {
        if (!(input instanceof String string))
            throw new IllegalArgumentException("Expected type of String for UUID");

        return UUID.fromString(string);
    }

    @Override
    public Object serialize(UUID value) {
        return value.toString();
    }
}
