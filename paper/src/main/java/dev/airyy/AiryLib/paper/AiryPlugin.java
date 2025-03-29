package dev.airyy.AiryLib.paper;

import dev.airyy.AiryLib.command.CommandManager;
import dev.airyy.AiryLib.paper.command.PaperCommandManager;
import dev.airyy.AiryLib.paper.command.handlers.TestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class AiryPlugin extends JavaPlugin {

    private CommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new TestCommand());

        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
