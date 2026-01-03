package dev.airyy.AiryLib.core.config.parser;

import java.util.HashMap;
import java.util.Map;

public class MapParser<K, V> implements IConfigParser<Map<K, V>> {

    private final IConfigParser<K> keyParser;
    private final IConfigParser<V> valueParser;

    public MapParser(IConfigParser<K> keyParser, IConfigParser<V> valueParser) {
        this.keyParser = keyParser;
        this.valueParser = valueParser;
    }

    @Override
    public Map<K, V> parse(Object input) {
        if (!(input instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Expected a map, got: " + input);
        }

        Map<K, V> parsed = new HashMap<>();
        for (Object key : rawMap.keySet()) {
            var rawValue = rawMap.get(key);

            var parsedKey = keyParser.parse(key);
            var parsedValue = valueParser.parse(rawValue);

            parsed.put(parsedKey, parsedValue);
        }

        return parsed;
    }

    @Override
    public Object serialize(Map<K, V> value) {
        Map<Object, Object> serialized = new HashMap<>();
        for (Map.Entry<K, V> entry : value.entrySet()) {
            Object sKey = keyParser.serialize(entry.getKey());
            Object sValue = valueParser.serialize(entry.getValue());
            serialized.put(sKey, sValue);
        }
        return serialized;
    }
}
