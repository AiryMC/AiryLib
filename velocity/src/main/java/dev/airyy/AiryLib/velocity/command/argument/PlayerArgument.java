package dev.airyy.AiryLib.velocity.command.argument;

import com.velocitypowered.api.proxy.Player;
import dev.airyy.AiryLib.core.command.argument.IArgument;
import dev.airyy.AiryLib.core.command.exception.ArgumentParseException;
import dev.airyy.AiryLib.velocity.AiryPlugin;

import java.util.List;
import java.util.Optional;

public class PlayerArgument implements IArgument<Player> {

    @Override
    public Player parse(String input) {
        try {
            Optional<Player> player = AiryPlugin.getInstance().getServer().getPlayer(input);
            return player.get();
        } catch (Exception e) {
            throw new ArgumentParseException("Expected a player, but got: " + input);
        }
    }

    @Override
    public List<String> suggest(String input) {
        return AiryPlugin.getInstance().getServer().getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}
