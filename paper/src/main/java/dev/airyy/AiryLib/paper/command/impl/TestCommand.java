package dev.airyy.AiryLib.paper.command.impl;

import dev.airyy.AiryLib.core.command.annotation.Command;
import dev.airyy.AiryLib.core.command.annotation.Default;
import dev.airyy.AiryLib.core.command.annotation.Handler;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Command("test")
public class TestCommand {

    @Default
    public void OnDefault(Player player) {
        player.sendMessage(Component.text("Hello, Default!"));
    }

    @Handler("me")
    public void OnMe(Player player) {
        player.sendMessage(Component.text("Hello, Me!"));
    }

    @Handler("add")
    public void OnAdd(Player player, int a, int b) {
        player.sendMessage(Component.text("Result: " + (a + b)));
    }

    @Handler("state")
    public void OnState(Player player, boolean state) {
        player.sendMessage(Component.text("State: " + state));
    }
}
