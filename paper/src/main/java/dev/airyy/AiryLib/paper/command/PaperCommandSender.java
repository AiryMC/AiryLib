package dev.airyy.AiryLib.paper.command;

import dev.airyy.AiryLib.core.command.ICommandSender;
import org.bukkit.command.CommandSender;

public class PaperCommandSender implements ICommandSender {

    private final CommandSender sender;

    public PaperCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandSender getSender() {
        return sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }
}
