package xyz.lychee.lagfixer.objects;

import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
<<<<<<< HEAD
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
=======
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
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

<<<<<<< HEAD
    public ResourceMonitor(LagFixer plugin) {
        super(plugin, "resource");
=======
    public ResourceMonitor() {
        super(true, "resource");
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    }

    public double cpuProcess() {
        return this.osBean.getProcessCpuLoad() * 100.0;
    }

    public double cpuSystem() {
        return this.osBean.getCpuLoad() * 100.0;
    }

<<<<<<< HEAD
    public ISupportNms.TickReport tickReport() {
        return SupportManager.getInstance().getNms().getTickReport();
=======
    public double tps() {
        return SupportManager.getInstance().getNms().getTps();
    }

    public double mspt() {
        return SupportManager.getInstance().getFork().getMspt();
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    }

    public double format(double d) {
        return (double) ((int) (d * 100.0)) / 100.0;
    }

    public long formatBytes(long bytes) {
        return bytes / 1024L / 1024L;
    }

    @Override
    public void run() {
<<<<<<< HEAD
        ISupportNms.TickReport tickReport = this.tickReport();
        this.tps = Math.min(this.format(tickReport.tps()), 20.0);
        this.mspt = this.format(tickReport.mspt());
        this.cpuProcess = this.format(this.cpuProcess());
        this.cpuSystem = this.format(this.cpuSystem());

=======
        this.cpuProcess = this.format(this.cpuProcess());
        this.cpuSystem = this.format(this.cpuSystem());
        this.tps = Math.min(this.format(this.tps()), 20.0);
        this.mspt = this.format(this.mspt());
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        Runtime r = Runtime.getRuntime();
        this.ramFree = this.formatBytes(r.freeMemory());
        this.ramUsed = this.formatBytes(r.totalMemory() - r.freeMemory());
        this.ramMax = this.formatBytes(r.maxMemory());
        this.ramTotal = this.formatBytes(r.totalMemory());
    }
}