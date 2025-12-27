package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.concurrent.TimeUnit;

@Getter
public abstract class AbstractMonitor {
    private double cpuProcess;
    private double cpuSystem;
    private double tps;
    private double mspt;
    private long ramFree;
    private long ramUsed;
    private long ramMax;
    private long ramTotal;
    private BukkitTask task;

    protected abstract double cpuProcess();

    protected abstract double cpuSystem();

    protected abstract double tps();

    protected abstract double mspt();

    public double format(double d) {
        return (double) ((int) (d * 100.0)) / 100.0;
    }

    public long formatBytes(long bytes) {
        return bytes / 1024L / 1024L;
    }

    public void start(int interval) {
        this.task = SupportManager.getInstance().getFork().runTimer(true, () -> {
            this.cpuProcess = this.format(this.cpuProcess());
            this.cpuSystem = this.format(this.cpuSystem());
            this.tps = Math.min(this.format(this.tps()), 20.0);
            this.mspt = this.format(this.mspt());
            Runtime r = Runtime.getRuntime();
            this.ramFree = this.formatBytes(r.freeMemory());
            this.ramUsed = this.formatBytes(r.totalMemory() - r.freeMemory());
            this.ramMax = this.formatBytes(r.maxMemory());
            this.ramTotal = this.formatBytes(r.totalMemory());
        }, 5L, interval, TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
}

