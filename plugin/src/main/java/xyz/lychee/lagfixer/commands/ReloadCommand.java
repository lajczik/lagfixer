package xyz.lychee.lagfixer.commands;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class ReloadCommand extends CommandManager.Subcommand {
    private volatile boolean reload = false;

    public ReloadCommand(CommandManager commandManager) {
        super(commandManager, "reload", "reload all plugin configuration");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.reload) {
            return MessageUtils.sendMessage(true, sender, "&7Reload is running, wait for results in console!");
        }

        this.reload = true;
        Thread thread = new Thread(() -> {
            TimingUtil t = TimingUtil.startNew();

            LagFixer plugin = this.getCommandManager().getPlugin();
            plugin.sendHeader();

            plugin.reloadConfig();
            try {
                ConfigManager.getInstance().load();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            ModuleManager.getInstance().getModules().forEach((clazz, m) -> {
                boolean enabled = m.getConfig().getBoolean(m.getName() + ".enabled");

                try {
                    if (enabled) {
                        if (!m.isLoaded()) {
                            m.load();
                            m.setLoaded(true);
                        }
                        m.loadAllConfig();
                        plugin.getLogger().info("&rConfiguration for &e" + m.getName() + " &rsuccessfully reloaded!");
                    } else if (m.isLoaded()) {
                        m.disable();
                        m.setLoaded(false);
                        plugin.getLogger().info("&rSuccessfully disabled module &e" + m.getName() + "&r!");
                    }
                    m.getMenu().updateAll();
                } catch (Exception ex) {
                    plugin.printError(ex);
                    plugin.getLogger().info("&rError reloading configuration for &c" + m.getName() + "&r!");
                }
            });

            MessageUtils.sendMessage(true, sender, "&7Reloaded modules configurations in &f" + t.stop().getExecutingTime() + "&7ms." +
                    "\n " +
                    "\n &7Working methods to apply all changes:" +
                    "\n  &8{*} &7Server restart (&frecommended&7)" +
                    "\n  &8{*} &7All plugins reload, command: &f/reload confirm" +
                    "\n  &8{*} &7Plugman reload, command: &f/plugman reload LagFixer");
            this.reload = false;
        });
        thread.setName("LagFixer Reload");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return true;
    }
}