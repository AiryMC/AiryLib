package dev.airyy.AiryLib.paper;

import dev.airyy.AiryLib.core.IAiryPlugin;
import dev.airyy.AiryLib.core.command.argument.impl.BooleanArgument;
import dev.airyy.AiryLib.core.command.argument.impl.IntegerArgument;
import dev.airyy.AiryLib.paper.command.PaperCommandManager;
import dev.airyy.AiryLib.paper.command.argument.PlayerArgument;
import dev.airyy.AiryLib.paper.command.impl.TestCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AiryPlugin extends JavaPlugin implements IAiryPlugin {

    private static AiryPlugin instance;

    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        commandManager = new PaperCommandManager(this);

        commandManager.registerArgumentParser(int.class, new IntegerArgument());
        commandManager.registerArgumentParser(Integer.class, new IntegerArgument());

        commandManager.registerArgumentParser(boolean.class, new BooleanArgument());
        commandManager.registerArgumentParser(Boolean.class, new BooleanArgument());

        commandManager.registerArgumentParser(Player.class, new PlayerArgument());

        // commandManager.register(new TestCommand());

        onInit();
    }

    @Override
    public void onDisable() {
        onDestroy();
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onDestroy() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends AiryPlugin> T getInstance() {
        return (T) instance;
    }
}
