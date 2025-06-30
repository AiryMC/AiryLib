package dev.airyy.AiryLib.core.command.argument.impl;

import dev.airyy.AiryLib.core.command.argument.IArgument;

import java.util.List;

public class StringArgument implements IArgument<String> {
    @Override
    public String parse(String input) {
        return input;
    }

    @Override
    public List<String> suggest(String input) {
        return List.of();
    }
}
