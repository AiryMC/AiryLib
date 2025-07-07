package dev.airyy.AiryLib.core.command;

import dev.airyy.AiryLib.core.command.annotation.Permission;

public interface ICommandSender {

    <T> T getSender();

    void sendMessage(String message);

    boolean hasPermission(String permission);
}
