package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractFork {
    private final Plugin plugin;

    public AbstractFork(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean checkTask(long delay) {
        return !this.getPlugin().isEnabled() || delay <= 0L;
    }

    public boolean checkTask(long initialDelay, long delay) {
        return !this.getPlugin().isEnabled() || initialDelay <= 0L || delay <= 0L;
    }

    public abstract boolean isSupported();

    public abstract int getPriority();

    public abstract double getMspt();

    public abstract boolean isSupportMspt();

    public abstract PluginCommand registerCommand(Plugin var1, String var2, List<String> var3, CommandExecutor var4);

    public abstract BukkitTask runNow(boolean async, Location loc, Runnable runnable);

    public abstract BukkitTask runLater(boolean async, Runnable runnable, long delayInMs);

    public abstract BukkitTask runTimer(boolean async, Runnable runnable, long initialDelayInMs, long delayInMs);

    public abstract BukkitTask runLater(boolean async, Runnable runnable, long delay, TimeUnit var5);

    public abstract BukkitTask runTimer(boolean async, Runnable runnable, long initialDelay, long delay, TimeUnit var7);

}

