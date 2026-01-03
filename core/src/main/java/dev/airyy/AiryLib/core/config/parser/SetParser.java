package dev.airyy.AiryLib.core.config.parser;

import java.util.HashSet;
import java.util.Set;

public class SetParser<T> implements IConfigParser<Set<T>> {

    private final IConfigParser<T> elementParser;

    public SetParser(IConfigParser<T> elementParser) {
        this.elementParser = elementParser;
    }

    @Override
    public Set<T> parse(Object raw) {
        if (!(raw instanceof Set<?> rawList)) {
            throw new IllegalArgumentException("Expected set, got: " + raw);
        }

        Set<T> parsed = new HashSet<>();
        for (Object entry : rawList) {
            parsed.add(elementParser.parse(entry));
        }
        return parsed;
    }

    @Override
    public Object serialize(Set<T> value) {
        Set<Object> serialized = new HashSet<>();
        for (T entry : value) {
            serialized.add(elementParser.serialize(entry));
        }
        return serialized;
    }
}