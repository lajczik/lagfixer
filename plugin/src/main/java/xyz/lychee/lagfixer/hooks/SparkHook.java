package xyz.lychee.lagfixer.hooks;

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
import xyz.lychee.lagfixer.objects.AbstractMonitor;

import java.util.concurrent.TimeUnit;

@Getter
public class SparkHook extends AbstractHook {
    private BukkitTask task;

    public SparkHook(LagFixer plugin, HookManager manager) {
        super(plugin, "spark", manager);
    }

    @Override
    public void load() {
        SupportManager support = SupportManager.getInstance();
        support.getMonitor().stop();

        SparkMonitor monitor = new SparkMonitor();
        monitor.start(this.getPlugin().getConfig().getInt("main.monitor_interval"));
        support.setMonitor(monitor);

        this.task = SupportManager.getInstance().getFork().runTimer(false, () -> {
            if (ErrorsManager.getInstance().isEnabled() && Bukkit.getOnlinePlayers().size() > 20) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spark profiler open");
            }
        }, 1L, 1L, TimeUnit.HOURS);
    }

    @Override
    public void disable() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    static class SparkMonitor extends AbstractMonitor {
        private final Spark spark = SparkProvider.get();

        @Override
        protected double cpuProcess() {
            return this.spark.cpuProcess().poll(StatisticWindow.CpuUsage.SECONDS_10) * 100.0;
        }

        @Override
        protected double cpuSystem() {
            return this.spark.cpuSystem().poll(StatisticWindow.CpuUsage.SECONDS_10) * 100.0;
        }

        @Override
        protected double tps() {
            DoubleStatistic<StatisticWindow.TicksPerSecond> tps = this.spark.tps();
            return tps == null ? 20.0 : tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
        }

        @Override
        protected double mspt() {
            GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = this.spark.mspt();
            return mspt == null ? 0.0 : mspt.poll(StatisticWindow.MillisPerTick.SECONDS_10).median();
        }
    }
}

