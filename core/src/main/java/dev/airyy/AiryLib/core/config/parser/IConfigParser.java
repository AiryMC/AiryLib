package dev.airyy.AiryLib.core.config.parser;

public interface IConfigParser<T> {

    /**
     * Convert raw YAML-loaded data (Map/List/String/primitive) into T.
     */
    T parse(Object input);

    /**
     * Convert T into an object that SnakeYAML can serialize (Map/List/String/primitive).
     */
    Object serialize(T value);
}