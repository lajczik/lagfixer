package xyz.lychee.lagfixer.modules;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractFork;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HopperOptimizerModule extends AbstractModule implements Listener {
    private final Set<Hopper> trackedHoppers = ConcurrentHashMap.newKeySet();
    private final Map<String, AtomicInteger> chunkHopperCount = new ConcurrentHashMap<>();
    private final Map<Hopper, Long> hopperLastActivity = new ConcurrentHashMap<>();
    private final Map<Hopper, Long> hopperLastTransfer = new ConcurrentHashMap<>();
    private final Map<Hopper, Integer> hopperTransferCount = new ConcurrentHashMap<>();
    private HopperOptimizerModule.NMS hopperOptimizer;
    private boolean smartThrottling;
    private boolean chunkLimitEnabled;
    private boolean emptyHopperOptimization;
    private int maxHoppersPerChunk;
    private int checkInterval;
    private int emptyHopperCheckDelay;
    private long inactiveThresholdMs;
    private BukkitTask optimizationTask;
    private BukkitTask cleanupTask;
    private BukkitTask resetTask;

    public HopperOptimizerModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.HIGH, "HopperOptimizer",
                new String[]{
                        "Optymalizuje działanie lejków zmniejszając ich obciążenie serwera.",
                        "Wprowadza inteligentne ograniczenia transferu przedmiotów.",
                        "Zapobiega nadmiernej liczbie lejków w chunkach.",
                        "Poprawia wydajność serwera przy dużej ilości lejków."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGViODFlZjg5MDIzNzk2NTBiYTc5ZjQ1NzIzZDZiOWM4ODgzODhhMDBmYzRlMTkyZjM0NTRmZTE5Mzg4MmVlMSJ9fX0="
        );
    }

    private void startOptimizationTasks() {
        AbstractFork fork = SupportManager.getInstance().getFork();

        optimizationTask = fork.runTimer(true, this::optimizeHoppers, 50L, checkInterval, TimeUnit.MILLISECONDS);

        cleanupTask = fork.runTimer(true, this::cleanupInactiveHoppers, 100L, 200L, TimeUnit.MILLISECONDS);

        resetTask = fork.runTimer(true, this::resetTransferCounters, 1L, 1L, TimeUnit.SECONDS);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getSource().getType() == InventoryType.HOPPER) {
            InventoryHolder holder = event.getSource().getHolder();
            if (holder instanceof Hopper) {
                Hopper hopper = (Hopper) holder;
                if (shouldBlockTransfer(hopper, event)) {
                    event.setCancelled(true);
                    return;
                }
                trackHopperActivity(hopper);
            }
        }

        if (event.getDestination().getType() == InventoryType.HOPPER) {
            InventoryHolder holder = event.getDestination().getHolder();
            if (holder instanceof Hopper) {
                trackHopperActivity((Hopper) holder);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.isCancelled() && event.getBlock().getType() == Material.HOPPER) {
            Block block = event.getBlock();
            /*getPlugin().getServer().getRegionScheduler()
                    .runDelayed(getPlugin(), block.getLocation(), task -> {
                        BlockState state = block.getState();
                        if (state instanceof Hopper) {
                            trackHopper((Hopper) state);
                        }
                    }, 1L);*/
            BlockState state = block.getState();
            if (state instanceof Hopper) {
                trackHopper((Hopper) state);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.isCancelled() && event.getBlock().getType() == Material.HOPPER) {
            BlockState state = event.getBlock().getState();
            if (state instanceof Hopper) {
                removeHopper((Hopper) state);
            }
        }
    }

    private boolean shouldBlockTransfer(Hopper hopper, InventoryMoveItemEvent event) {
        String chunkKey = getChunkKey(hopper);
        AtomicInteger count = chunkHopperCount.get(chunkKey);

        if (chunkLimitEnabled && count != null &&
                count.get() >= maxHoppersPerChunk &&
                !trackedHoppers.contains(hopper)) {
            return true;
        }

        if (smartThrottling) {
            long currentTime = System.currentTimeMillis();
            Long lastTransfer = hopperLastTransfer.get(hopper);

            boolean isEmpty = this.isInventoryEmpty(hopper.getInventory());
            if (isEmpty && emptyHopperOptimization &&
                    lastTransfer != null &&
                    currentTime - lastTransfer < emptyHopperCheckDelay) {
                return true;
            }

            Long lastActivity = hopperLastActivity.get(hopper);
            return lastActivity != null &&
                    currentTime - lastActivity > inactiveThresholdMs &&
                    lastTransfer != null &&
                    currentTime - lastTransfer < emptyHopperCheckDelay * 2L;
        }
        return false;
    }

    private boolean isInventoryEmpty(Inventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                return false;
            }
        }
        return true;
    }

    private void trackHopperActivity(Hopper hopper) {
        if (hopper == null || !hopper.isPlaced()) return;

        long currentTime = System.currentTimeMillis();
        hopperLastActivity.put(hopper, currentTime);
        hopperLastTransfer.put(hopper, currentTime);

        hopperTransferCount.merge(hopper, 1, Integer::sum);
    }

    private void trackHopper(Hopper hopper) {
        if (hopper == null || !hopper.isPlaced()) return;

        trackedHoppers.add(hopper);
        hopperLastActivity.put(hopper, System.currentTimeMillis());

        if (chunkLimitEnabled) {
            String chunkKey = getChunkKey(hopper);
            chunkHopperCount.computeIfAbsent(chunkKey, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    private void removeHopper(Hopper hopper) {
        trackedHoppers.remove(hopper);
        hopperLastActivity.remove(hopper);
        hopperLastTransfer.remove(hopper);
        hopperTransferCount.remove(hopper);

        if (chunkLimitEnabled) {
            String chunkKey = getChunkKey(hopper);
            AtomicInteger count = chunkHopperCount.get(chunkKey);
            if (count != null) {
                count.decrementAndGet();
                if (count.get() <= 0) {
                    chunkHopperCount.remove(chunkKey);
                }
            }
        }
    }

    private void optimizeHoppers() {
        Set<Hopper> hoppersToProcess = new HashSet<>(trackedHoppers);

        for (Hopper hopper : hoppersToProcess) {
            optimizeHopper(hopper);
        }
    }

    private void optimizeHopper(Hopper hopper) {
        if (hopper == null) return;

        if (!hopper.isPlaced() || !hopper.getChunk().isLoaded()) {
            removeHopper(hopper);
            return;
        }

        int cooldown = calculateOptimalCooldown(hopper);
        this.hopperOptimizer.hopperCooldown(hopper, cooldown);
    }

    private int calculateOptimalCooldown(Hopper hopper) {
        if (!smartThrottling) return 8;

        boolean isEmpty = this.isInventoryEmpty(hopper.getInventory());
        if (isEmpty && emptyHopperOptimization) {
            return Math.max(8 * 3, 20);
        }

        Long lastActivity = hopperLastActivity.get(hopper);
        if (lastActivity != null) {
            long timeSinceActivity = System.currentTimeMillis() - lastActivity;
            if (timeSinceActivity > inactiveThresholdMs * 2) {
                return Math.min(8 * 4, 50);
            }
            if (timeSinceActivity > inactiveThresholdMs) {
                return Math.min(8 * 2, 25);
            }
        }
        return 8;
    }

    private void resetTransferCounters() {
        hopperTransferCount.clear();
    }

    private void cleanupInactiveHoppers() {
        long currentTime = System.currentTimeMillis();
        AtomicInteger removedCount = new AtomicInteger(0);

        trackedHoppers.removeIf(hopper -> {
            if (hopper == null) {
                removedCount.incrementAndGet();
                return true;
            }

            if (!hopper.isPlaced() || !hopper.getChunk().isLoaded()) {
                removedCount.incrementAndGet();
                return true;
            }

            Long lastActivity = hopperLastActivity.get(hopper);
            if (lastActivity != null && currentTime - lastActivity > 300000) {
                removeHopper(hopper);
                removedCount.incrementAndGet();
                return true;
            }
            return false;
        });

        if (chunkLimitEnabled) {
            chunkHopperCount.entrySet().removeIf(entry -> {
                String[] parts = entry.getKey().split(":");
                if (parts.length != 3) return true;

                String worldName = parts[0];
                int chunkX = Integer.parseInt(parts[1]);
                int chunkZ = Integer.parseInt(parts[2]);

                World world = Bukkit.getWorld(worldName);
                return world == null || !world.isChunkLoaded(chunkX, chunkZ);
            });
        }
    }

    private String getChunkKey(Hopper hopper) {
        Chunk chunk = hopper.getChunk();
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }

    @Override
    public void load() {
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        startOptimizationTasks();
    }

    @Override
    public boolean loadConfig() {
        this.hopperOptimizer = ReflectionUtils.createInstance("HopperOptimizer", this);

        this.smartThrottling = getSection().getBoolean("smart_throttling.enabled", true);
        this.chunkLimitEnabled = getSection().getBoolean("chunk_limiting.enabled", false);
        this.emptyHopperOptimization = getSection().getBoolean("empty_hopper_optimization", true);
        this.maxHoppersPerChunk = getSection().getInt("chunk_limiting.maxHoppersPerChunk", 16);
        this.checkInterval = getSection().getInt("checkInterval", 2000);
        this.emptyHopperCheckDelay = getSection().getInt("emptyHopperCheckDelay", 100);
        this.inactiveThresholdMs = getSection().getLong("inactiveThresholdMs", 30000L);

        return this.hopperOptimizer != null;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);

        if (optimizationTask != null) optimizationTask.cancel();
        if (cleanupTask != null) cleanupTask.cancel();
        if (resetTask != null) resetTask.cancel();

        trackedHoppers.clear();
        hopperLastActivity.clear();
        hopperLastTransfer.clear();
        hopperTransferCount.clear();
        chunkHopperCount.clear();
    }

    @Getter
    public static abstract class NMS implements Listener {
        private final HopperOptimizerModule module;

        public NMS(HopperOptimizerModule module) {
            this.module = module;
        }

        public abstract void hopperCooldown(Hopper hopper, int cooldown);
    }
}