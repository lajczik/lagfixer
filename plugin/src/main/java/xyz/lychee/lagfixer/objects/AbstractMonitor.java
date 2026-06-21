package xyz.lychee.lagfixer.objects;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractMonitor implements Runnable {
    private final LagFixer plugin;
    private final String name;
    private ScheduledTask task;

    public AbstractMonitor(LagFixer plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public abstract void run();

    public void start() {
        FileConfiguration config = SupportManager.getInstance().getPlugin().getConfig();
        int interval = config.getBoolean("main.monitor." + this.name + ".enabled") ? config.getInt("main.monitor." + this.name + ".interval") : 0;
        if (interval > 0) {
            this.task = Bukkit.getAsyncScheduler().runAtFixedRate(LagFixer.getInstance(), t -> this.run(), interval / 2, interval, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
}