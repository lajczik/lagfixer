package xyz.lychee.lagfixer.objects;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.lang.management.ManagementFactory;

@Getter
public class ResourceMonitor extends AbstractMonitor {
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private double cpuProcess;
    private double cpuSystem;
    private double tps;
    private double mspt;
    private long ramFree;
    private long ramUsed;
    private long ramMax;
    private long ramTotal;

    public ResourceMonitor(LagFixer plugin) {
        super(plugin, "resource");
    }

    public double cpuProcess() {
        return this.osBean.getProcessCpuLoad() * 100.0;
    }

    public double cpuSystem() {
        return this.osBean.getCpuLoad() * 100.0;
    }

    public ISupportNms.TickReport tickReport() {
        return SupportManager.getInstance().getNms().getTickReport();
    }

    public double format(double d) {
        return (double) ((int) (d * 100.0)) / 100.0;
    }

    public long formatBytes(long bytes) {
        return bytes / 1024L / 1024L;
    }

    @Override
    public void run() {
        ISupportNms.TickReport tickReport = this.tickReport();
        this.tps = Math.min(this.format(tickReport.tps()), 20.0);
        this.mspt = this.format(tickReport.mspt());
        this.cpuProcess = this.format(this.cpuProcess());
        this.cpuSystem = this.format(this.cpuSystem());

        Runtime r = Runtime.getRuntime();
        this.ramFree = this.formatBytes(r.freeMemory());
        this.ramUsed = this.formatBytes(r.totalMemory() - r.freeMemory());
        this.ramMax = this.formatBytes(r.maxMemory());
        this.ramTotal = this.formatBytes(r.totalMemory());
    }
}