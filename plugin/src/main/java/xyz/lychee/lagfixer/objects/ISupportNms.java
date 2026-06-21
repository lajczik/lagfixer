package xyz.lychee.lagfixer.objects;

public interface ISupportNms {
    TickReport getTickReport();

    record TickReport(double mspt, double tps) {}
}