package dev.airyy.AiryLib.paper.command.argument;

import dev.airyy.AiryLib.core.command.argument.IArgument;
import dev.airyy.AiryLib.core.command.exception.ArgumentParseException;
import dev.airyy.AiryLib.paper.AiryPlugin;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerArgument implements IArgument<Player> {

    @Override
    public Player parse(String input) {
        try {
            return AiryPlugin.getInstance().getServer().getPlayer(input);
        } catch (Exception e) {
            throw new ArgumentParseException("Expected a player, but got: " + input);
        }
    }

    @Override
    public List<String> suggest(String input) {
        return AiryPlugin.getInstance().getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}
