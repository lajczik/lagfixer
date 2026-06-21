package xyz.lychee.lagfixer.nms.v1_21_R5;

import com.google.common.util.concurrent.AtomicDouble;
import io.papermc.paper.threadedregions.TickData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.ISupportNms;

import java.util.concurrent.atomic.LongAdder;

public class SupportNms implements ISupportNms {

    @Override
    public TickReport getTickReport() {
        long currTime = System.nanoTime();
        AtomicDouble tpsByRegion = new AtomicDouble();
        AtomicDouble msptByRegion = new AtomicDouble();
        LongAdder regions = new LongAdder();

        for (World world : Bukkit.getWorlds()) {
            ((CraftWorld) world).getHandle().regioniser.computeForAllRegions(region -> {
                TickData.TickReportData report = region.getData().getRegionSchedulingHandle().getTickReport15s(currTime);
                if (report != null) {
                    tpsByRegion.addAndGet(report.tpsData().segmentAll().average());
                    msptByRegion.addAndGet(report.timePerTickData().segmentAll().average() / 1_000_000.0D);
                    regions.increment();
                }
            });
        }

        int regionsInt = regions.intValue();
        if (regionsInt == 0) {
            return new TickReport(0, 20);
        }

        return new TickReport(msptByRegion.get() / regionsInt, tpsByRegion.get() / regionsInt);
    }
}