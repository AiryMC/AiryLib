package dev.airyy.AiryLib.core.command;

public interface ICommandSender {

    <T> T getSender();

    void sendMessage(String message);
}
