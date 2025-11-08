package dev.airyy.AiryLib.paper.config.parser;

import dev.airyy.AiryLib.core.config.parser.IConfigParser;
import dev.airyy.AiryLib.paper.AiryPlugin;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocationParser implements IConfigParser<Location> {

    @Override
    public Location parse(Object input) {
        if (!(input instanceof Section section)) {
            throw new IllegalArgumentException("Expected type of Map for Location");
        }

        AiryPlugin plugin = AiryPlugin.getInstance();

        String worldName = section.getString("world", null);
        World world = plugin.getServer().getWorld(worldName);

        double x = section.getDouble("x", 0.0);
        double y = section.getDouble("y", 0.0);
        double z = section.getDouble("z", 0.0);

        float pitch = section.getFloat("pitch", 0.0f);
        float yaw = section.getFloat("yaw", 0.0f);

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
}
