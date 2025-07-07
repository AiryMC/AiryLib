package dev.airyy.AiryLib.core.config.parser;

import java.util.ArrayList;
import java.util.List;

public class ListParser<T> implements IConfigParser<List<T>> {

    private final IConfigParser<T> elementParser;

    public ListParser(IConfigParser<T> elementParser) {
        this.elementParser = elementParser;
    }

    @Override
    public List<T> parse(Object raw) {
        if (!(raw instanceof List<?> rawList)) {
            throw new IllegalArgumentException("Expected a list, got: " + raw);
        }

        List<T> parsed = new ArrayList<>();
        for (Object entry : rawList) {
            parsed.add(elementParser.parse(entry));
        }
        return parsed;
    }

    @Override
    public Object serialize(List<T> value) {
        List<Object> serialized = new ArrayList<>();
        for (T entry : value) {
            serialized.add(elementParser.serialize(entry));
        }
        return serialized;
    }
}
