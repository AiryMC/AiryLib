package dev.airyy.AiryLib.command.arguments;

public interface ArgumentConverter<T> {
    T from(String string) throws Exception;
    String to(T object);
    boolean canConvert(String string);
}
