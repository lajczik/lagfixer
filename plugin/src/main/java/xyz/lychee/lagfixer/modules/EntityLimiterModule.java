package xyz.lychee.lagfixer.modules;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class EntityLimiterModule extends AbstractModule implements Listener {
    private final EnumSet<CreatureSpawnEvent.SpawnReason> reasons = EnumSet.noneOf(CreatureSpawnEvent.SpawnReason.class);
    private final EnumSet<EntityType> whitelist = EnumSet.noneOf(EntityType.class);
    private BukkitTask overflow_task;

    private boolean ignore_models;
    private int creatures;
    private int items;
    private int vehicles;
    private int projectiles;

    private boolean overflow_enabled;
    private int overflow_interval;
    private double overflow_multiplier;
    private boolean overflow_creatures;
    private boolean overflow_items;
    private boolean overflow_vehicles;
    private boolean overflow_projectiles;
    private boolean overflow_named;

    public EntityLimiterModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, AbstractModule.Impact.HIGH, "EntityLimiter",
                new String[]{
                        "Restricts the number of entities per chunk.",
                        "Essential for survival servers with expansive animal farms.",
                        "Prevents excessive entity accumulation and associated performance issues.",
                        "Maintains stable performance levels even in environments with high entity density."
                }, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRjMzZjOWNiNTBhNTI3YWE1NTYwN2EwZGY3MTg1YWQyMGFhYmFhOTAzZThkOWFiZmM3ODI2MDcwNTU0MGRlZiJ9fX0="
        );
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent e) {
        e.setCancelled(this.handleEvent(e.getLocation(), e.getSpawnReason(), e.getEntityType(), this.creatures, ent -> ent instanceof Mob));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSpawn(SpawnerSpawnEvent e) {
        e.setCancelled(this.handleEvent(e.getLocation(), CreatureSpawnEvent.SpawnReason.SPAWNER, e.getEntityType(), this.creatures, ent -> ent instanceof Mob));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(this.handleEvent(e.getItemDrop().getLocation(), null, e.getItemDrop().getType(), this.items, ent -> ent instanceof Item));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicle(VehicleCreateEvent e) {
        e.setCancelled(this.handleEvent(e.getVehicle().getLocation(), null, e.getVehicle().getType(), this.vehicles, ent -> ent instanceof Vehicle));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent e) {
        e.setCancelled(this.handleEvent(e.getEntity().getLocation(), null, e.getEntity().getType(), this.projectiles, ent -> ent instanceof Projectile));
    }

    public boolean handleEvent(Location loc, CreatureSpawnEvent.SpawnReason reason, EntityType type, int limit, Predicate<Entity> filter) {
        if (limit < 1
                || !this.canContinue(loc.getWorld())
                || (reason != null && !this.reasons.contains(reason))
                || this.whitelist.contains(type)
                || !loc.getChunk().isLoaded()
        ) {
            return false;
        }

        int count = 0;
        for (Entity entity : loc.getChunk().getEntities()) {
            if (filter.test(entity) && !this.whitelist.contains(entity.getType()) && ++count >= limit) {
                //this.locks++;
                return true;
            }
        }

        return false;
    }

    @Override
    public void load() {
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());

        if (this.overflow_enabled) {
            final int limit_creatures = (int) (this.creatures * this.overflow_multiplier);
            final int limit_items = (int) (this.items * this.overflow_multiplier);
            final int limit_vehicles = (int) (this.vehicles * this.overflow_multiplier);
            final int limit_projectiles = (int) (this.projectiles * this.overflow_multiplier);

            final boolean checkCreatures = this.overflow_creatures;
            final boolean checkItems = this.overflow_items;
            final boolean checkVehicles = this.overflow_vehicles;
            final boolean checkProjectiles = this.overflow_projectiles;

            this.overflow_task = SupportManager.getInstance().getFork().runTimer(false, () -> {
                this.getAllowedWorlds().forEach(w -> {
                    Chunk[] chunks = w.getLoadedChunks();

                    for (Chunk chunk : chunks) {
                        Entity[] entities = chunk.getEntities();
                        if (entities.length == 0) continue;

                        int creatures = 0, items = 0, vehicles = 0, projectiles = 0;

                        for (Entity entity : entities) {
                            if (this.whitelist.contains(entity.getType())
                                    || (!this.overflow_named && entity.getCustomName() != null)
                                    || (!this.ignore_models && HookManager.getInstance().getModel().hasModel(entity))) {
                                continue;
                            }

                            boolean removed = false;

                            if (entity instanceof Mob) {
                                if (creatures < limit_creatures) creatures++;
                                else if (checkCreatures) removed = true;
                            } else if (entity instanceof Item) {
                                if (items < limit_items) items++;
                                else if (checkItems) removed = true;
                            } else if (entity instanceof Vehicle) {
                                if (vehicles < limit_vehicles) vehicles++;
                                else if (checkVehicles) removed = true;
                            } else if (entity instanceof Projectile) {
                                if (projectiles < limit_projectiles) projectiles++;
                                else if (checkProjectiles) removed = true;
                            }

                            if (removed) {
                                entity.remove();
                            }
                        }
                    }
                });
            }, this.overflow_interval, this.overflow_interval, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean loadConfig() {
        this.ignore_models = HookManager.getInstance().noneModels() || this.getSection().getBoolean("ignore_models");
        this.creatures = this.getSection().getInt("creatures");
        this.items = this.getSection().getInt("items");
        this.vehicles = this.getSection().getInt("vehicles");
        this.projectiles = this.getSection().getInt("projectiles");

        ReflectionUtils.convertEnums(CreatureSpawnEvent.SpawnReason.class, this.reasons, this.getSection().getStringList("reasons"));
        ReflectionUtils.convertEnums(EntityType.class, this.whitelist, this.getSection().getStringList("whitelist"));

        this.overflow_interval = this.getSection().getInt("overflow_purge.interval");
        this.overflow_enabled = this.overflow_interval > 0 && this.getSection().getBoolean("overflow_purge.enabled");
        if (this.overflow_enabled) {
            this.overflow_multiplier = this.getSection().getDouble("overflow_purge.limit_multiplier");
            this.overflow_creatures = this.creatures > 0 && this.getSection().getBoolean("overflow_purge.types.creatures");
            this.overflow_items = this.items > 0 && this.getSection().getBoolean("overflow_purge.types.items");
            this.overflow_vehicles = this.vehicles > 0 && this.getSection().getBoolean("overflow_purge.types.vehicles");
            this.overflow_projectiles = this.projectiles > 0 && this.getSection().getBoolean("overflow_purge.types.projectiles");
            this.overflow_named = this.getSection().getBoolean("overflow_purge.types.named");
        }
        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.overflow_task != null) {
            this.overflow_task.cancel();
        }
    }
}

