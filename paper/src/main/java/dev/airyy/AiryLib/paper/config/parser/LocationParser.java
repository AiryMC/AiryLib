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
        return new Location(world, x, y, z);
    }

    @Override
    public Object serialize(Location value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", value.getWorld().getName());
        map.put("x", value.x());
        map.put("y", value.y());
        map.put("z", value.z());
        return map;
    }

    private double getDouble(Map<?, ?> map, String key, double defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }
}
