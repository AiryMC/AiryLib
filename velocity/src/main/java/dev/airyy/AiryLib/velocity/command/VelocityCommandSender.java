package dev.airyy.AiryLib.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import dev.airyy.AiryLib.core.command.ICommandSender;

public class VelocityCommandSender implements ICommandSender {

    private final CommandSource sender;

    public VelocityCommandSender(CommandSource sender) {
        this.sender = sender;
    }

    @Override
    public CommandSource getSender() {
        return sender;
    }
}
