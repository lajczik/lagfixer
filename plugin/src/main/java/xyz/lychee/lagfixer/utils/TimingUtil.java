package xyz.lychee.lagfixer.utils;

public class TimingUtil {
    private long startTime;
    private long endTime;
    private long nanoStartTime;
    private long nanoEndTime;

    public static TimingUtil startNew() {
        return new TimingUtil().start();
    }

    public TimingUtil start() {
        this.startTime = System.currentTimeMillis();
        this.nanoStartTime = System.nanoTime();
        return this;
    }

    public TimingUtil stop() {
        this.endTime = System.currentTimeMillis();
        this.nanoEndTime = System.nanoTime();
        return this;
    }

    public long getExecutingTime() {
        if (this.startTime == 0L || this.endTime == 0L) {
            return 0L;
        }
        return this.endTime - this.startTime;
    }

    public long getExecutingNanoTime() {
        if (this.nanoStartTime == 0L || this.nanoEndTime == 0L) {
            return 0L;
        }
        return this.nanoEndTime - this.nanoStartTime;
    }

    public String toString() {
        return this.getExecutingTime() + "ms (" + this.getExecutingNanoTime() + "ns)";
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getNanoStartTime() {
        return this.nanoStartTime;
    }

    public void setNanoStartTime(long nanoStartTime) {
        this.nanoStartTime = nanoStartTime;
    }

    public long getNanoEndTime() {
        return this.nanoEndTime;
    }

    public void setNanoEndTime(long nanoEndTime) {
        this.nanoEndTime = nanoEndTime;
    }
}

