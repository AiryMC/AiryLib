package dev.airyy.AiryLib.core.command.arguments;

public interface IArgumentConverter<T> {
    T from(String string) throws Exception;
    String to(T object);
    boolean canConvert(String string);

    boolean isValid(Class<?> clazz);
}
