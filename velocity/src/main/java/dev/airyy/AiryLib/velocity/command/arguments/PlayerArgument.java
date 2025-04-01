package dev.airyy.AiryLib.velocity.command.arguments;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.airyy.AiryLib.command.arguments.ArgumentConverter;
import dev.airyy.AiryLib.velocity.AiryPlugin;

public class PlayerArgument implements ArgumentConverter<Player> {
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
}
