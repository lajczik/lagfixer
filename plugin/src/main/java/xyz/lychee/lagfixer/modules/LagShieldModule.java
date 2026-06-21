package xyz.lychee.lagfixer.modules;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
<<<<<<< HEAD
import org.bukkit.*;
import org.bukkit.entity.*;
=======
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
<<<<<<< HEAD
=======
import xyz.lychee.lagfixer.objects.ISupportNms;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Getter
public class LagShieldModule extends AbstractModule implements Listener {
    private final TreeMap<Double, Integer> dynamic_view_distance_tps = new TreeMap<>();
    private final TreeMap<Double, Integer> dynamic_simulation_distance_tps = new TreeMap<>();
    private final TreeMap<Double, Integer> dynamic_tick_speed_tps = new TreeMap<>();
    private int locks = 0;
<<<<<<< HEAD
    private ScheduledTask task;

=======
    private BukkitTask task;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    private double entitySpawn_tps;
    private double tickHopper_tps;
    private double redstone_tps;
    private double projectiles_tps;
    private double leavesDecay_tps;
    private double mobAi_tps;
    private double liquidFlow_tps;
    private double explosions_tps;
    private double fireworks_tps;
    private boolean entitySpawn;
    private boolean tickHopper;
    private boolean redstone;
    private boolean projectiles;
    private boolean leavesDecay;
    private boolean mobAi;
    private boolean liquidFlow;
    private boolean explosions;
    private boolean fireworks;

    private boolean dynamic_view_distance;
    private boolean dynamic_simulation_distance;
    private boolean dynamic_tick_speed;

    public LagShieldModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, AbstractModule.Impact.HIGH, "LagShield",
                new String[]{
                        "Monitors server load and adjusts settings during latency spikes.",
                        "Addresses fluctuations in server performance to mitigate delays and lag.",
                        "Dynamically adjusts settings, disables unnecessary features, and optimizes resources.",
                        "Ensures smooth gameplay by minimizing the impact of performance fluctuations."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmZjY2ZlNTA5NmEzMzViOWFiNzhhYjRmNzc4YWU0OTlmNGNjYWI0ZTJjOTVmYTM0OTIyN2ZkMDYwNzU5YmFhZiJ9fX0="
        );
    }

<<<<<<< HEAD
=======
    @Override
    public void run() {
        SupportManager support = SupportManager.getInstance();
        ISupportNms nms = support.getNms();
        double tps = support.getResourceMonitor().getTps();
        boolean oldMobAi = this.mobAi;

        this.entitySpawn = tps < this.entitySpawn_tps;
        this.tickHopper = tps < this.tickHopper_tps;
        this.redstone = tps < this.redstone_tps;
        this.projectiles = tps < this.projectiles_tps;
        this.leavesDecay = tps < this.leavesDecay_tps;
        this.mobAi = tps < this.mobAi_tps;
        this.liquidFlow = tps < this.liquidFlow_tps;
        this.explosions = tps < this.explosions_tps;
        this.fireworks = tps < this.fireworks_tps;

        if (this.mobAi) {
            for (World w : this.getAllowedWorlds()) {
                for (LivingEntity le : w.getLivingEntities()) {
                    nms.setEntityAi(le, false);
                }
            }
        } else if (oldMobAi) {
            for (World w : this.getAllowedWorlds()) {
                for (LivingEntity le : w.getLivingEntities()) {
                    nms.setEntityAi(le, true);
                }
            }
        }

        if (this.dynamic_view_distance) {
            Integer viewDistance = this.getThreshold(this.dynamic_view_distance_tps, tps);
            if (viewDistance != null) {
                for (World w : this.getAllowedWorlds()) {
                    nms.setViewDistance(w, viewDistance);
                }
            }
        }

        if (this.dynamic_simulation_distance) {
            Integer simulationDistance = this.getThreshold(this.dynamic_simulation_distance_tps, tps);
            if (simulationDistance != null) {
                for (World w : this.getAllowedWorlds()) {
                    nms.setSimulationDistance(w, simulationDistance);
                }
            }
        }

        if (this.dynamic_tick_speed) {
            Integer tickSpeed = this.getThreshold(this.dynamic_tick_speed_tps, tps);
            if (tickSpeed != null) {
                for (World w : this.getAllowedWorlds()) {
                    w.setGameRule(GameRule.RANDOM_TICK_SPEED, tickSpeed);
                }
            }
        }
    }

>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    private Integer getThreshold(TreeMap<Double, Integer> map, double tps) {
        if (map.isEmpty()) return null;
        Map.Entry<Double, Integer> entry = map.ceilingEntry(tps);
        if (entry != null) return entry.getValue();
        entry = map.lastEntry();
        return entry != null ? entry.getValue() : null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRedstone(BlockRedstoneEvent e) {
        if (e.getNewCurrent() != 0 && this.redstone && this.canContinue(e.getBlock().getWorld())) {
            e.setNewCurrent(0);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(VehicleCreateEvent e) {
        if (this.entitySpawn && this.canContinue(e.getVehicle().getWorld())) {
            e.setCancelled(true);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        if (this.entitySpawn && this.canContinue(e.getEntity().getWorld())) {
            e.setCancelled(true);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent e) {
        if (this.projectiles && this.canContinue(e.getEntity().getWorld())) {
            e.setCancelled(true);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopper(InventoryMoveItemEvent e) {
        if (e.getSource().getType() == InventoryType.HOPPER && this.tickHopper) {
            Location loc = e.getSource().getLocation();
            if (loc != null && !this.canContinue(loc.getWorld())) return;

            e.setCancelled(true);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDecay(LeavesDecayEvent e) {
        if (this.leavesDecay && this.canContinue(e.getBlock().getWorld())) {
            e.setCancelled(true);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (this.liquidFlow && e.getBlock().isLiquid() && this.canContinue(e.getBlock().getWorld())) {
            e.setCancelled(true);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(BlockExplodeEvent e) {
        if (this.explosions && this.canContinue(e.getBlock().getWorld())) {
            e.setCancelled(true);
            ++this.locks;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFirework(FireworkExplodeEvent e) {
        if (this.fireworks && this.canContinue(e.getEntity().getWorld())) {
            e.setCancelled(true);
            ++this.locks;
        }
    }

    public void loadThreshold(Map<Double, Integer> map, String key) {
        map.clear();
        for (String threshold : this.getSection().getStringList(key)) {
            try {
                String[] split = threshold.split(":");
                map.put(
                        Double.parseDouble(split[0]),
                        Integer.parseInt(split[1])
                );
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void load() throws Exception {
<<<<<<< HEAD
        this.task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.getPlugin(), t -> {
            double tps = SupportManager.getInstance().getResourceMonitor().getTps();
            boolean oldMobAi = this.mobAi;

            this.entitySpawn = tps < this.entitySpawn_tps;
            this.tickHopper = tps < this.tickHopper_tps;
            this.redstone = tps < this.redstone_tps;
            this.projectiles = tps < this.projectiles_tps;
            this.leavesDecay = tps < this.leavesDecay_tps;
            this.mobAi = tps < this.mobAi_tps;
            this.liquidFlow = tps < this.liquidFlow_tps;
            this.explosions = tps < this.explosions_tps;
            this.fireworks = tps < this.fireworks_tps;

            if (this.mobAi) {
                for (World w : this.getAllowedWorlds()) {
                    this.setEntityAi(w, false);
                }
            } else if (oldMobAi) {
                for (World w : this.getAllowedWorlds()) {
                    this.setEntityAi(w, true);
                }
            }

            if (this.dynamic_view_distance) {
                Integer viewDistance = this.getThreshold(this.dynamic_view_distance_tps, tps);
                if (viewDistance != null) {
                    viewDistance = Math.clamp(viewDistance, 2, 32);
                    for (World w : this.getAllowedWorlds()) {
                        w.setViewDistance(viewDistance);
                    }
                }
            }

            if (this.dynamic_simulation_distance) {
                Integer simulationDistance = this.getThreshold(this.dynamic_simulation_distance_tps, tps);
                if (simulationDistance != null) {
                    simulationDistance = Math.clamp(simulationDistance, 2, 32);
                    for (World w : this.getAllowedWorlds()) {
                        w.setSimulationDistance(simulationDistance);
                    }
                }
            }

            if (this.dynamic_tick_speed) {
                Integer tickSpeed = this.getThreshold(this.dynamic_tick_speed_tps, tps);
                if (tickSpeed != null) {
                    for (World w : this.getAllowedWorlds()) {
                        w.setGameRule(GameRule.RANDOM_TICK_SPEED, tickSpeed);
                    }
                }
            }
        }, 20L * 60L, 20L * 60L);
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    public void setEntityAi(World world, boolean mobAi) {
        Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
        regions.forEach((regionPos, chunks) -> {
            Executor executor = task -> Bukkit.getRegionScheduler().execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (Chunk chunk : chunks) {
                    Entity[] entities = chunk.getEntities();
                    for (Entity entity : entities) {
                        if (!(entity instanceof Mob mob) || mob.hasAI() == mobAi) continue;

                        mob.setAI(mobAi);
                        mob.setCollidable(!mobAi);
                        mob.setSilent(!mobAi);
                    }
                }
            }, executor);
        });
=======
        SupportManager support = SupportManager.getInstance();
        this.task = support.getFork().runTimer(false, this, 1L, 1L, TimeUnit.MINUTES);
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    }

    @Override
    public boolean loadConfig() {
        this.entitySpawn_tps = this.getSection().getDouble("tps_threshold.entity_spawn");
        this.tickHopper_tps = this.getSection().getDouble("tps_threshold.tick_hopper");
        this.redstone_tps = this.getSection().getDouble("tps_threshold.redstone");
        this.projectiles_tps = this.getSection().getDouble("tps_threshold.projectiles");
        this.leavesDecay_tps = this.getSection().getDouble("tps_threshold.leaves_decay");
        this.mobAi_tps = this.getSection().getDouble("tps_threshold.mobai");
        this.liquidFlow_tps = this.getSection().getDouble("tps_threshold.liquid_flow");
        this.explosions_tps = this.getSection().getDouble("tps_threshold.explosions");
        this.fireworks_tps = this.getSection().getDouble("tps_threshold.fireworks");

        this.dynamic_view_distance = this.getSection().getBoolean("dynamic_view_distance.enabled");
        if (this.dynamic_view_distance) {
            this.loadThreshold(this.dynamic_view_distance_tps, "dynamic_view_distance.tps_thresholds");
        }

        this.dynamic_simulation_distance = this.getSection().getBoolean("dynamic_simulation_distance.enabled");
        if (this.dynamic_simulation_distance) {
            this.loadThreshold(this.dynamic_simulation_distance_tps, "dynamic_simulation_distance.tps_thresholds");
        }

        this.dynamic_tick_speed = this.getSection().getBoolean("dynamic_tick_speed.enabled");
        if (this.dynamic_tick_speed) {
            this.loadThreshold(this.dynamic_tick_speed_tps, "dynamic_tick_speed.tps_thresholds");
        }

        return true;
    }

    @Override
    public void disable() {
        if (this.task != null) {
            this.task.cancel();
        }
        HandlerList.unregisterAll(this);
    }
<<<<<<< HEAD
}
=======
}

>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
