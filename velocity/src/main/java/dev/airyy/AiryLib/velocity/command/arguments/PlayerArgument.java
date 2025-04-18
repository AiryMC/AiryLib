package dev.airyy.AiryLib.velocity.command.arguments;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.airyy.AiryLib.core.command.arguments.IArgumentConverter;
import dev.airyy.AiryLib.velocity.AiryPlugin;

public class PlayerArgument implements IArgumentConverter<Player> {
    private final ProxyServer server = AiryPlugin.getInstance().getServer();

    @Override
    public Player from(String string) throws Exception {
        return server.getPlayer(string).orElse(null);
    }

    @Override
    public String to(Player object) {
        return object.getUsername();
    }

    @Override
    public boolean canConvert(String string) {
        return server.getPlayer(string).isPresent();
    }

    @Override
    public boolean isValid(Class<?> clazz) {
        return clazz == Player.class;
    }
}
