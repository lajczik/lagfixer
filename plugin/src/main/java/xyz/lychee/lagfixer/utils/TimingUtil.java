package xyz.lychee.lagfixer.utils;

import lombok.Getter;

@Getter
public class TimingUtil {
    private long startTime;
    private long endTime;

    public static TimingUtil startNew() {
        return new TimingUtil().start();
    }

    public TimingUtil start() {
        this.startTime = System.nanoTime();
        return this;
    }

    public TimingUtil stop() {
        this.endTime = System.nanoTime();
        return this;
    }

    public long getExecutingTime() {
        return this.getExecutingNanoTime() / 1_000_000L;
    }

    public long getExecutingNanoTime() {
        if (this.startTime == 0 || this.endTime == 0) {
            return 0L;
        }
        return this.endTime - this.startTime;
    }

    @Override
    public String toString() {
        return MathUtils.round(this.getExecutingNanoTime() / 1_000_000D, 3) + "ms";
    }
}