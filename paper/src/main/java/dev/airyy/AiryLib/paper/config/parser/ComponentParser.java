package dev.airyy.AiryLib.paper.config.parser;

import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentParser implements IConfigParser<Component> {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public Component parse(Object input) {
        if (!(input instanceof String string)) {
            throw new IllegalArgumentException("Expected type of String for Component");
        }

        return miniMessage.deserialize(string);
    }

    @Override
    public Object serialize(Component value) {
        return miniMessage.serialize(value);
    }
}
