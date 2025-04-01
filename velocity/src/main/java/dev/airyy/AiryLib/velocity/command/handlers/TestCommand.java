package dev.airyy.AiryLib.velocity.command.handlers;

import com.velocitypowered.api.proxy.Player;
import dev.airyy.AiryLib.command.annotations.Command;
import dev.airyy.AiryLib.command.annotations.Default;
import dev.airyy.AiryLib.command.annotations.SubCommand;
import net.kyori.adventure.text.Component;

@Command("test")
public class TestCommand {

    @Default
    public void onDefault(Player player, int value) {
        player.sendMessage(Component.text("Hello! + " + value));
    }

    @Default
    public void onDefault(Player player, Player target) {
        player.sendMessage(Component.text("Hello, " + target.getUsername() + "!"));
    }

    @SubCommand("sudo")
    public void onSudo(Player player, Player target, String message) {
        target.sendMessage(Component.text(target.getUsername() + " says: " + message));
    }

    @SubCommand("sudo")
    public void onSudo(Player player, Player target, int value) {
        target.sendMessage(Component.text(target.getUsername() + " says value: " + value));
    }

    @SubCommand("sudo")
    public void onSudo(Player player, String target, int value) {
        player.sendMessage(Component.text(target + " says value: " + value));
    }
}
