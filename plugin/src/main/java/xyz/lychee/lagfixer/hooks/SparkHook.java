package xyz.lychee.lagfixer.hooks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ErrorsManager;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractHook;
import xyz.lychee.lagfixer.objects.ISupportNms;
import xyz.lychee.lagfixer.objects.ResourceMonitor;

import java.util.concurrent.TimeUnit;

@Getter
public class SparkHook extends AbstractHook {
    private ScheduledTask task;

    public SparkHook(LagFixer plugin, HookManager manager) {
        super(plugin, "spark", manager);
    }

    @Override
    public void load() {
        SupportManager support = SupportManager.getInstance();
        support.getResourceMonitor().stop();

        SparkMonitor monitor = new SparkMonitor(this.getPlugin());
        monitor.start();
        support.setResourceMonitor(monitor);

        this.task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.getPlugin(), t -> {
            if (ErrorsManager.getInstance().isEnabled() && Bukkit.getOnlinePlayers().size() > 20) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spark profiler open");
            }
        }, 20 * 60 * 60, 20 * 60 * 60);
    }

    @Override
    public void disable() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    static class SparkMonitor extends ResourceMonitor {
        private final Spark spark = SparkProvider.get();

        public SparkMonitor(LagFixer plugin) {
            super(plugin);
        }

        @Override
        public double cpuProcess() {
            return this.spark.cpuProcess().poll(StatisticWindow.CpuUsage.SECONDS_10) * 100.0;
        }

        @Override
        public double cpuSystem() {
            return this.spark.cpuSystem().poll(StatisticWindow.CpuUsage.SECONDS_10) * 100.0;
        }

        @Override
        public ISupportNms.TickReport tickReport() {
            DoubleStatistic<StatisticWindow.TicksPerSecond> tps = this.spark.tps();
            GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = this.spark.mspt();

            return new ISupportNms.TickReport(
                    mspt == null ? 0.0 : mspt.poll(StatisticWindow.MillisPerTick.SECONDS_10).median(),
                    tps == null ? 20.0 : tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10)
            );
        }
    }
}