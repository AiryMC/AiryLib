package dev.airyy.AiryLib.paper.config.parser;

import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import org.bukkit.Material;

import java.util.Locale;

public class MaterialParser implements IConfigParser<Material> {
    @Override
    public Material parse(Object input) {
        if (!(input instanceof String string)) {
            throw new IllegalArgumentException("Expected type of String for Material");
        }

        try {
            return Material.valueOf(string.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid Material name: " + string, ex);
        }
    }

    @Override
    public Object serialize(Material value) {
        return value.toString();
    }
}
