package dev.airyy.AiryLib.paper.command.handlers;

import dev.airyy.AiryLib.command.annotations.Command;
import dev.airyy.AiryLib.command.annotations.Default;
import dev.airyy.AiryLib.command.annotations.SubCommand;
import dev.airyy.AiryLib.paper.AiryPlugin;
import org.bukkit.entity.Player;

@Command("test")
public class TestCommand {

    @Default
    public void onDefault(Player player, int value) {
        player.sendMessage("Hello! + " + value);
        AiryPlugin.getPlugin(AiryPlugin.class).getLogger().info("Default command handler for command \"test\" was called!");
    }

    @Default
    public void onDefault(Player player, Player target) {
        player.sendMessage("Hello, " + target.getName() + "!");
        AiryPlugin.getPlugin(AiryPlugin.class).getLogger().info("Default command handler for command \"test\" was called!");
    }

    @SubCommand("sudo")
    public void onSudo(Player player, Player target, String message) {
        target.sendMessage(target.getName() + " says: " + message);
    }

    @SubCommand("sudo")
    public void onSudo(Player player, Player target, int value) {
        target.sendMessage(target.getName() + " says value: " + value);
    }
}
