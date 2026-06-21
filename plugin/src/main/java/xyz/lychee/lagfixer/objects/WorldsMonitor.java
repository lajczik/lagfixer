package xyz.lychee.lagfixer.objects;

<<<<<<< HEAD
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
=======
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
<<<<<<< HEAD
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
=======
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.List;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90

@Getter
public class WorldsMonitor extends AbstractMonitor {
    private int entities = 0;
    private int creatures = 0;
    private int items = 0;
    private int projectiles = 0;
    private int vehicles = 0;
    private int tiles = 0;
    private int chunks = 0;

<<<<<<< HEAD
    public WorldsMonitor(LagFixer plugin) {
        super(plugin, "worlds");
=======
    public WorldsMonitor() {
        super(false, "worlds");
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    }

    @Override
    public void run() {
<<<<<<< HEAD
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
=======
        int entities = 0, creatures = 0, items = 0, projectiles = 0, vehicles = 0, tiles = 0, chunks = 0;

        ISupportNms nms = SupportManager.getInstance().getNms();
        for (World world : Bukkit.getWorlds()) {
            Chunk[] loaded = world.getLoadedChunks();
            List<Entity> list = world.getEntities();
            entities += list.size();
            chunks += loaded.length;

            for (Entity entity : list) {
                if (entity instanceof Mob) {
                    creatures++;
                } else if (entity instanceof Vehicle) {
                    vehicles++;
                } else if (entity instanceof Item) {
                    items++;
                } else if (entity instanceof Projectile) {
                    projectiles++;
                }
            }

            for (Chunk chunk : loaded) {
                tiles += nms.getTileEntitiesCount(chunk);
            }
        }

        this.entities = entities;
        this.creatures = creatures;
        this.items = items;
        this.projectiles = projectiles;
        this.vehicles = vehicles;
        this.tiles = tiles;
        this.chunks = chunks;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    }
}