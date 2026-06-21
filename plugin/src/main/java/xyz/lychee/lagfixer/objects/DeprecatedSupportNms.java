package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeprecatedSupportNms implements ISupportNms {
    private static final TickReport DEFAULT_TICK_REPORT = new TickReport(0, 20);

    @Override
    public TickReport getTickReport() {
        return DEFAULT_TICK_REPORT;
    }
}