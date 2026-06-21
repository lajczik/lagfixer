package xyz.lychee.lagfixer.objects;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Getter
public class WorldsMonitor extends AbstractMonitor {
    private int entities = 0;
    private int creatures = 0;
    private int items = 0;
    private int projectiles = 0;
    private int vehicles = 0;
    private int tiles = 0;
    private int chunks = 0;

    public WorldsMonitor(LagFixer plugin) {
        super(plugin, "worlds");
    }

    @Override
    public void run() {
        LongAdder chunksAdder = new LongAdder();
        LongAdder entitiesAdder = new LongAdder();
        LongAdder playersAdder = new LongAdder();
        LongAdder creaturesAdder = new LongAdder();
        LongAdder itemsAdder = new LongAdder();
        LongAdder vehiclesAdder = new LongAdder();
        LongAdder projectilesAdder = new LongAdder();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            if (world.getPlayers().isEmpty()) continue;

            playersAdder.add(world.getPlayerCount());

            RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();

            Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
            regions.forEach((regionPos, chunks) -> {
                chunksAdder.add(chunks.size());

                Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    for (Chunk chunk : chunks) {
                        for (Entity ent : chunk.getEntities()) {
                            switch (ent) {
                                case Mob ignored -> creaturesAdder.increment();
                                case Item ignored -> itemsAdder.increment();
                                case Projectile ignored -> projectilesAdder.increment();
                                case Vehicle ignored -> vehiclesAdder.increment();
                                default -> {}
                            }
                            entitiesAdder.increment();
                        }
                    }
                }, executor);

                futures.add(future);
            });
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(5, TimeUnit.SECONDS)
                .thenAccept(v -> {
                    this.entities = entitiesAdder.intValue();
                    this.creatures = creaturesAdder.intValue();
                    this.items = itemsAdder.intValue();
                    this.projectiles = projectilesAdder.intValue();
                    this.vehicles = vehiclesAdder.intValue();
                    this.tiles = projectilesAdder.intValue();
                    this.chunks = chunksAdder.intValue();
                })
                .exceptionally(ex -> {
                    LagFixer.getInstance().printError(ex);
                    return null;
                });
    }
}