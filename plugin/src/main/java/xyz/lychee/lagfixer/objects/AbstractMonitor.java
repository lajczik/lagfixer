package xyz.lychee.lagfixer.objects;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
<<<<<<< HEAD
import org.bukkit.Bukkit;
=======
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractMonitor implements Runnable {
<<<<<<< HEAD
    private final LagFixer plugin;
    private final String name;
    private ScheduledTask task;

    public AbstractMonitor(LagFixer plugin, String name) {
        this.plugin = plugin;
=======
    private final boolean async;
    private final String name;
    private BukkitTask task;

    public AbstractMonitor(boolean async, String name) {
        this.async = async;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        this.name = name;
    }

    public abstract void run();

    public void start() {
        FileConfiguration config = SupportManager.getInstance().getPlugin().getConfig();
        int interval = config.getBoolean("main.monitor." + this.name + ".enabled") ? config.getInt("main.monitor." + this.name + ".interval") : 0;
        if (interval > 0) {
<<<<<<< HEAD
            this.task = Bukkit.getAsyncScheduler().runAtFixedRate(LagFixer.getInstance(), t -> this.run(), interval / 2, interval, TimeUnit.SECONDS);
=======
            this.task = SupportManager.getInstance().getFork().runTimer(this.async, this, interval / 2, interval, TimeUnit.SECONDS);
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        }
    }

    public void stop() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
}