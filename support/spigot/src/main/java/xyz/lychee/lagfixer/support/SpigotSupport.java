package xyz.lychee.lagfixer.support;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.objects.AbstractFork;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpigotSupport extends AbstractFork {

    public SpigotSupport(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public int getPriority() {
        return -1;
    }

    @Override
    public double getMspt() {
        return 0;
    }

    @Override
    public boolean isSupportMspt() {
        return false;
    }

    @Override
    public PluginCommand registerCommand(Plugin plugin, String name, List<String> alliases, CommandExecutor executor) {
        try {
            Constructor<PluginCommand> declaredConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            declaredConstructor.setAccessible(true);
            PluginCommand cmd = declaredConstructor.newInstance(name, plugin);
            cmd.setAliases(alliases);
            cmd.setExecutor(executor);
            if (executor instanceof TabCompleter)
                cmd.setTabCompleter((TabCompleter) executor);

            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(name, cmd);
            return cmd;
        } catch (Exception ex) {
            //this.getPlugin().printError(ex);
            return null;
        }
    }

    @Override
    public BukkitTask runNow(boolean async, Location loc, Runnable run) {
        if (getPlugin().isEnabled()) {
            return async ?
                    Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), run)
                    : Bukkit.getScheduler().runTask(getPlugin(), run);
        }
        return null;
    }

    @Override
    public BukkitTask runLater(boolean async, Runnable run, long delayInMs) {
        if (this.checkTask(delayInMs)) return null;

        if (async) {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(this.getPlugin(), run, delayInMs / 50);
        } else {
            return Bukkit.getScheduler().runTaskLater(this.getPlugin(), run, delayInMs / 50);
        }
    }

    @Override
    public BukkitTask runTimer(boolean async, Runnable run, long initialDelayInMs, long delayInMs) {
        if (this.checkTask(initialDelayInMs, delayInMs)) return null;

        if (async) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(this.getPlugin(), run, initialDelayInMs / 50, delayInMs / 50);
        } else {
            return Bukkit.getScheduler().runTaskTimer(this.getPlugin(), run, initialDelayInMs / 50, delayInMs / 50);
        }
    }

    @Override
    public BukkitTask runLater(boolean async, Runnable run, long delay, TimeUnit unit) {
        if (this.checkTask(delay)) return null;

        if (async) {
            return Bukkit.getScheduler().runTaskLaterAsynchronously(this.getPlugin(), run, unit.toMillis(delay) / 50);
        } else {
            return Bukkit.getScheduler().runTaskLater(this.getPlugin(), run, unit.toMillis(delay) / 50);
        }
    }

    @Override
    public BukkitTask runTimer(boolean async, Runnable run, long initialDelay, long delay, TimeUnit unit) {
        if (this.checkTask(initialDelay, delay)) return null;

        if (async) {
            return Bukkit.getScheduler().runTaskTimerAsynchronously(this.getPlugin(), run, unit.toMillis(initialDelay) / 50, unit.toMillis(delay) / 50);
        } else {
            return Bukkit.getScheduler().runTaskTimer(this.getPlugin(), run, unit.toMillis(initialDelay) / 50, unit.toMillis(delay) / 50);
        }
    }
}
