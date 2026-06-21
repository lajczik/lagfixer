package xyz.lychee.lagfixer.nms.v1_20_R3;

import io.papermc.paper.threadedregions.TickData;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.ISupportNms;

public class SupportNms implements ISupportNms {

    @Override
    public TickReport getTickReport() {
        long currTime = System.nanoTime();
        DoubleArrayList tpsByRegion = new DoubleArrayList();
        DoubleArrayList msptByRegion = new DoubleArrayList();

        for (World world : Bukkit.getWorlds()) {
            ((CraftWorld) world).getHandle().regioniser.computeForAllRegions(region -> {
                TickData.TickReportData report = region.getData().getRegionSchedulingHandle().getTickReport15s(currTime);
                if (report != null) {
                    tpsByRegion.add(report.tpsData().segmentAll().average());
                    msptByRegion.add(report.timePerTickData().segmentAll().average() / 1_000_000.0D);
                }
            });
        }

        if (tpsByRegion.isEmpty()) {
            return new TickReport(20, 0);
        }

        int middle = tpsByRegion.size() >> 1;
        double medTps;
        double medMspt;
        if ((tpsByRegion.size() & 1) == 0) {
            medTps = (tpsByRegion.getDouble(middle - 1) + tpsByRegion.getDouble(middle)) / 2.0d;
            medMspt = (msptByRegion.getDouble(middle - 1) + msptByRegion.getDouble(middle)) / 2.0d;
        } else {
            medTps = tpsByRegion.getDouble(middle);
            medMspt = msptByRegion.getDouble(middle);
        }

        return new TickReport(medMspt, medTps);
    }
}