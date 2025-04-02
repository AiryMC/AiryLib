package dev.airyy.AiryLib.paper.command.arguments;

import dev.airyy.AiryLib.core.command.arguments.ArgumentConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerArgument implements ArgumentConverter<Player> {
    @Override
    public Player from(String string) {
        return Bukkit.getPlayer(string);
    }

    @Override
    public String to(Player object) {
        return object.getName();
    }

    @Override
    public boolean canConvert(String string) {
        return Bukkit.getPlayer(string) != null;
    }

    @Override
    public boolean isValid(Class<?> clazz) {
        return clazz == Player.class;
    }
}
