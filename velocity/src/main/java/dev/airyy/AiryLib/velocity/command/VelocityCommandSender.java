package dev.airyy.AiryLib.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import dev.airyy.AiryLib.core.command.ICommandSender;
import net.kyori.adventure.text.Component;

public class VelocityCommandSender implements ICommandSender {

    private final CommandSource sender;

    public VelocityCommandSender(CommandSource sender) {
        this.sender = sender;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandSource getSender() {
        return sender;
    }

    @Override
    public void sendMessage(String message) {
        sender.sendMessage(Component.text(message));
    }
}
