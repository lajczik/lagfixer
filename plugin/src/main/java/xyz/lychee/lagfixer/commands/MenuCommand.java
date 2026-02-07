package xyz.lychee.lagfixer.commands;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.menu.HardwareMenu;
import xyz.lychee.lagfixer.menu.MainMenu;
import xyz.lychee.lagfixer.menu.ModulesMenu;
import xyz.lychee.lagfixer.utils.MessageUtils;

@Getter
public class MenuCommand extends CommandManager.Subcommand {
    private static @Getter MenuCommand instance;
    private MainMenu mainMenu;
    private ModulesMenu modulesMenu;
    private HardwareMenu hardwareMenu;

    public MenuCommand(CommandManager commandManager) {
        super(commandManager, "menu", "打开LagFixer菜单并编辑配置", "gui");
        instance = this;
    }

    @Override
    public void load() {
        LagFixer plugin = this.getCommandManager().getPlugin();

        this.mainMenu = new MainMenu(plugin, 27, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &f菜单！&8| &eLagFixer"));
        this.mainMenu.load();

        this.modulesMenu = new ModulesMenu(plugin, 45, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &f模块！&8| &eLagFixer"));
        this.modulesMenu.load();

        try {
            this.hardwareMenu = new HardwareMenu(plugin, 27, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &f硬件！&8| &eLagFixer"));
            this.hardwareMenu.load();
        } catch (Throwable ignored) {}
    }

    @Override
    public void unload() {
        this.mainMenu.unload();
        this.modulesMenu.unload();
        if (this.hardwareMenu != null) {
            this.hardwareMenu.unload();
        }
    }

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player) {
            ((Player) sender).openInventory(this.mainMenu.getInv());
        } else {
            Component text = Language.getMainValue("player_only", true);
            if (text != null) {
                LagFixer.getInstance().getAudiences().sender(sender).sendMessage(text);
            }
        }
        return true;
    }
}