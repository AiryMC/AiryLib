package dev.airyy.AiryLib.core.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Pattern;

/**
 * A utility class for handling messages.
 * <p>
 * This class provides static methods for managing and formatting messages in the application.
 * It is designed to be used as a helper class and does not require instantiation.
 */
public final class Messages {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Serializes a {@link Component} into a string using MiniMessage formatting.
     *
     * @param component the component to serialize
     * @return the serialized string representation of the component
     */
    public static String serialize(Component component) {
        return miniMessage.serialize(component);
    }

    /**
     * Deserializes a MiniMessage-formatted string into a {@link Component}.
     *
     * @param component the MiniMessage string to deserialize
     * @return the deserialized component
     */
    public static Component deserialize(String component) {
        return miniMessage.deserialize(component);
    }

    /**
     * Replaces all occurrences of specified placeholders in the given component with their replacements.
     * The placeholders and replacements must be provided in pairs (e.g., "key1", "value1", "key2", "value2", ...).
     * If the number of strings is not even, the original component is returned unchanged.
     *
     * @param component the component to perform replacements on
     * @param strings a varargs list of placeholder-replacement pairs
     * @return the component with placeholders replaced, or the original component if the input is invalid
     */
    public static Component replaceAll(Component component, String... strings) {
        if (!Maths.isMultipleOf(2, strings.length)) {
            return component;
        }

        for (int i = 0; i < strings.length; i += 2) {
            String placeholder = strings[i];
            Component replacement = deserialize(strings[i + 1]);

            component = component.replaceText((builder) -> builder.match(Pattern.quote(placeholder)).replacement(replacement));
        }

        return component;
    }

    /**
     * Replaces all occurrences of a single placeholder in the given component with the specified replacement.
     *
     * @param component the component to modify
     * @param placeholder the text to replace
     * @param replacement the replacement text
     * @return the updated component with the placeholder replaced
     */
    public static Component replace(Component component, String placeholder, String replacement) {
        return component.replaceText((builder) -> builder.match(Pattern.quote(placeholder)).replacement(replacement));
    }
}
