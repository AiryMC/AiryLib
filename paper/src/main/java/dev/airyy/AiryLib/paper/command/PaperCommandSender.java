package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.ICommandSender;
import dev.airyy.AiryLib.core.command.annotation.Permission;
import org.bukkit.command.CommandSender;

public class PaperCommandSender implements ICommandSender {

    private final CommandSender sender;

    public PaperCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public CommandSender getSender() {
        return sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }
}
