package xyz.lychee.lagfixer.objects;

import lombok.Data;

import java.util.concurrent.atomic.LongAdder;

@Data
public class RegionsEntityReport {
    private LongAdder chunks = new LongAdder();
    private LongAdder players = new LongAdder();
    private LongAdder entities = new LongAdder();
    private LongAdder creatures = new LongAdder();
    private LongAdder items = new LongAdder();
    private LongAdder projectiles = new LongAdder();
    private LongAdder vehicles = new LongAdder();
}