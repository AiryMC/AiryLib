package dev.airyy.AiryLib.paper.config.parser;

import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import dev.airyy.AiryLib.paper.AiryPlugin;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocationParser implements IConfigParser<Location> {

    @Override
    public Location parse(Object input) {
        if (!(input instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected type of Map for Location");
        }

        AiryPlugin plugin = AiryPlugin.getInstance();

        String worldName = (String) map.getOrDefault("world", null);
        World world = plugin.getServer().getWorld(worldName);
        double x = getDouble(map, "x", 0.0);
        double y = getDouble(map, "y", 0.0);
        double z = getDouble(map, "z", 0.0);
        float pitch = getFloat(map, "pitch", 0.0f);
        float yaw = getFloat(map, "yaw", 0.0f);
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public Object serialize(Location value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", value.getWorld().getName());
        map.put("x", value.x());
        map.put("y", value.y());
        map.put("z", value.z());
        map.put("pitch", value.getPitch());
        map.put("yaw", value.getYaw());
        return map;
    }

    private double getDouble(Map<?, ?> map, String key, double defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number number) {
            return number.doubleValue();
        }
        if (val instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    private float getFloat(Map<?, ?> map, String key, float defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number number) {
            return number.floatValue();
        }
        if (val instanceof String s) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
