package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractMonitor implements Runnable {
    private final String name;
    private BukkitTask task;

    public AbstractMonitor(String name) {
        this.name = name;
    }

    public abstract void run();

    public void start() {
        FileConfiguration config = SupportManager.getInstance().getPlugin().getConfig();
        int interval = config.getBoolean("main.monitor."+this.name+".enabled") ? config.getInt("main.monitor."+this.name+".interval") : 0;
        if (interval > 0) {
            this.task = SupportManager.getInstance().getFork().runTimer(true, this, interval / 2, interval, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
}