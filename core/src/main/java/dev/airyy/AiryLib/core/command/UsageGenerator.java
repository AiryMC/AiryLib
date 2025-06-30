package dev.airyy.AiryLib.core.command;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class UsageGenerator {
    public static String generateUsage(String baseCommand, String subcommand, Method method) {
        StringBuilder usage = new StringBuilder("/").append(baseCommand).append(" ").append(subcommand);

        Parameter[] params = method.getParameters();

        // Start from 1 to skip the sender (first parameter)
        for (int i = 1; i < params.length; i++) {
            Parameter param = params[i];
            String name = param.getName();

            usage.append(" <").append(name).append(">");
        }

        return usage.toString();
    }
}
