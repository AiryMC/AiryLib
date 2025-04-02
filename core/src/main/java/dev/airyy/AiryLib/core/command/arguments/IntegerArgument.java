package dev.airyy.AiryLib.core.command.arguments;

public class IntegerArgument implements ArgumentConverter<Integer> {

    @Override
    public Integer from(String string) {
        return Integer.parseInt(string);
    }

    @Override
    public String to(Integer object) {
        return object.toString();
    }

    @Override
    public boolean canConvert(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    @Override
    public boolean isValid(Class<?> clazz) {
        return clazz == int.class;
    }
}
