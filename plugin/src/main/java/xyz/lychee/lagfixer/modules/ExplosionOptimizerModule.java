package xyz.lychee.lagfixer.modules;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ExplosionOptimizerModule extends AbstractModule implements Listener {
    private final Map<String, Location> recent_explosions = new ConcurrentHashMap<>();
    private final Map<String, Location> protected_locations = new ConcurrentHashMap<>();

    private EnumMap<EntityType, Float> yield_limit_per_entity;
    private boolean yield_limit_enabled;
    private float yield_limit_default;

    private boolean anti_chain_enabled;
    private boolean anti_chain_prevent_tnt;
    private boolean anti_chain_prevent_creeper;
    private boolean anti_chain_prevent_crystal;
    private boolean anti_chain_prevent_block_ignition;
    private double anti_chain_radius;
    private double anti_chain_radius_squared;
    private int anti_chain_max_size;
    private long anti_chain_cooldown;
    //private BukkitTask anti_chain_cleanup_task;

    private boolean management_enabled;
    private boolean management_cancel_block_explosions;
    private boolean management_cancel_creepers;
    private boolean management_cancel_crystals;
    private boolean management_cancel_fireballs;
    private boolean management_cancel_tnt;
    private boolean management_cancel_wither_skulls;
    private boolean management_explosion_damage;
    private float management_explosion_damage_multiplier;
    private boolean management_explosion_sound;
    private float management_explosion_sound_volume;
    private boolean management_explosion_knockback;
    private float management_explosion_knockback_multiplier;

    public ExplosionOptimizerModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.HIGH, "ExplosionOptimizer",
                new String[]{
                        "Limits explosion power and prevents chain reactions to reduce lag and destruction.",
                        "Useful for servers with frequent TNT, creepers, or End Crystal usage.",
                        "Prevents excessive explosions from causing performance issues.",
                        "Maintains stable server performance while controlling destructive events."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDgxNzliMTc1ZGFhNzlmNzNjNjY1YjYxMTYzMzY0ZjY2MjdlM2QwMmI3MjUzZDQyN2ViZDJmZjY4MThkZTZjZSJ9fX0="
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosionPrime(@NotNull ExplosionPrimeEvent event) {
        if (!this.canContinue(event.getEntity().getWorld())) return;

        if (this.management_enabled && this.shouldDeactivateExplosion(event.getEntity())) {
            event.setCancelled(true);
            this.handleExplosionEffects(event.getEntity(), 4.0f, event.getEntity().getLocation());
            return;
        }

        if (this.anti_chain_enabled && this.shouldPreventChain(event.getEntity(), event.getEntity().getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (this.yield_limit_enabled) {
            float currentYield = event.getRadius();
            float newYield = this.yieldLimitForEntity(event.getEntity(), currentYield);
            if (currentYield > newYield) {
                event.setRadius(newYield);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        if (!this.canContinue(event.getLocation().getWorld()) || event.blockList().isEmpty()) return;

        if (this.management_enabled && this.shouldDeactivateExplosion(event.getEntity())) {
            event.setCancelled(true);
            this.handleExplosionEffects(event.getEntity(), event.getYield(), event.getLocation());
            return;
        }

        if (this.anti_chain_enabled) {
            if (this.anti_chain_prevent_block_ignition) {
                event.blockList().removeIf(block -> block.getType() == Material.TNT);
            }
            this.addProtectedArea(event.getLocation());
        }

        if (this.yield_limit_enabled) {
            float currentYield = event.getYield();
            float newYield = this.yieldLimitForEntity(event.getEntity(), currentYield);
            if (currentYield > newYield) {
                event.setYield(newYield);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        if (!this.canContinue(event.getBlock().getWorld()) || event.blockList().isEmpty()) return;

        if (this.management_enabled && this.management_cancel_block_explosions) {
            event.setCancelled(true);
            this.handleExplosionEffects(null, event.getYield(), event.getBlock().getLocation());
            return;
        }

        if (this.anti_chain_enabled) {
            if (this.shouldPreventBlockExplosion(event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }

            if (this.anti_chain_prevent_block_ignition) {
                event.blockList().removeIf(block -> block.getType() == Material.TNT);
            }
            this.addProtectedArea(event.getBlock().getLocation());
            this.trackExplosion(event.getBlock().getLocation());
        }

        if (this.yield_limit_enabled) {
            float currentYield = event.getYield();
            float newYield = Math.min(currentYield, this.yield_limit_default);
            if (currentYield > newYield) {
                event.setYield(newYield);
            }
        }
    }

    private float yieldLimitForEntity(@Nullable Entity entity, float currentYield) {
        float defaultLimit = this.yield_limit_default;

        if (entity == null || currentYield <= defaultLimit) {
            return Math.min(currentYield, defaultLimit);
        }

        float entityLimit = this.yield_limit_per_entity.getOrDefault(entity.getType(), defaultLimit);
        return Math.min(currentYield, entityLimit);
    }

    private boolean shouldDeactivateExplosion(Entity entity) {
        return (entity instanceof TNTPrimed && this.management_cancel_tnt)
                || (entity instanceof Creeper && this.management_cancel_creepers)
                || (entity instanceof EnderCrystal && this.management_cancel_crystals)
                || (entity instanceof WitherSkull && this.management_cancel_wither_skulls)
                || (entity instanceof Fireball && this.management_cancel_fireballs);
    }

    private void handleExplosionEffects(Entity tnt, float power, Location location) {
        if (location == null || location.getWorld() == null) return;

        if (this.management_explosion_sound) {
            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, this.management_explosion_sound_volume);
        }

        if (!this.management_explosion_damage && !this.management_explosion_knockback) return;

        double radius = power * 2.0;
        double radiusSquared = radius * radius;
        double invRadiusSquared = 1.0 / radiusSquared;

        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity)) continue;

            Location eLoc = entity.getLocation();
            double distanceSquared = location.distanceSquared(eLoc);

            if (this.management_explosion_damage) {
                double falloff = (radiusSquared - distanceSquared) * invRadiusSquared;
                double damage = falloff * power * this.management_explosion_damage_multiplier;

                if (damage > 0) {
                    ((LivingEntity) entity).damage(damage, tnt);
                }
            }

            if (this.management_explosion_knockback && distanceSquared > 0.01) {
                Vector direction = eLoc.toVector().subtract(location.toVector());

                // Newton-Raphson method for fast inverse square root
                double half = 0.5 * distanceSquared;
                long i = Double.doubleToLongBits(distanceSquared);
                i = 0x5fe6eb50c7b537a9L - (i >> 1);
                double y = Double.longBitsToDouble(i);
                double invSqrt = y * (1.5 - half * y * y);

                double falloff = (radiusSquared - distanceSquared) * invRadiusSquared;
                double strength = (falloff * falloff) * power * this.management_explosion_knockback_multiplier;

                Vector knockback = direction.multiply(invSqrt * strength);
                knockback.setY((knockback.getY() * 0.3) + 0.2);

                entity.setVelocity(entity.getVelocity().add(knockback));
            }
        }
    }

    private boolean shouldPreventChain(Entity entity, Location location) {
        if ((entity instanceof TNTPrimed && !this.anti_chain_prevent_tnt)
                || (entity instanceof Creeper && !this.anti_chain_prevent_creeper)
                || (entity instanceof EnderCrystal && !this.anti_chain_prevent_crystal))
            return false;

        for (Location protectedLoc : this.protected_locations.values()) {
            if (Objects.equals(protectedLoc.getWorld(), location.getWorld())
                    && protectedLoc.distanceSquared(location) <= this.anti_chain_radius_squared) {
                return true;
            }
        }

        return this.recent_explosions.containsValue(location);
    }

    private boolean shouldPreventBlockExplosion(Location location) {
        for (Location protectedLoc : this.protected_locations.values()) {
            if (Objects.equals(protectedLoc.getWorld(), location.getWorld())
                    && protectedLoc.distanceSquared(location) <= this.anti_chain_radius_squared) {
                return true;
            }
        }
        return this.recent_explosions.containsValue(location);
    }

    private void trackExplosion(Location location) {
        String locationKey = getLocationKey(location);
        this.recent_explosions.put(locationKey, location);
        SupportManager.getInstance().getFork().runLater(true, () -> this.recent_explosions.remove(locationKey), this.anti_chain_cooldown);
    }

    private void addProtectedArea(Location location) {
        String locationKey = location.toString();
        this.protected_locations.put(locationKey, location);
        SupportManager.getInstance().getFork().runLater(true, () -> this.protected_locations.remove(locationKey), this.anti_chain_cooldown);
    }

    private String getLocationKey(Location location) {
        if (location == null || location.getWorld() == null) return "unknown";
        long x = Math.round(location.getX() / this.anti_chain_radius);
        long y = Math.round(location.getY() / this.anti_chain_radius);
        long z = Math.round(location.getZ() / this.anti_chain_radius);
        return location.getWorld().getName() + ":" + x + ":" + y + ":" + z;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

        /*if (anti_chain_enabled) {
            antiChainCleanupTask = SupportManager.getInstance().getFork().runTimer(true, () -> {

            }, 30, 30, TimeUnit.SECONDS);
        }*/
    }

    @Override
    public boolean loadConfig() {
        this.yield_limit_enabled = getSection().getBoolean("yield_limit.enabled");
        if (this.yield_limit_enabled) {
            this.yield_limit_default = (float) getSection().getDouble("yield_limit.default");
            this.yield_limit_per_entity = new EnumMap<>(EntityType.class);
            for (String entity : this.getSection().getStringList("yield_limit.per_entity")) {
                try {
                    String[] split = entity.split(":");
                    this.yield_limit_per_entity.put(
                            EntityType.valueOf(split[0].toUpperCase()),
                            Float.parseFloat(split[1])
                    );
                } catch (Exception e) {
                    getPlugin().getLogger().warning("Unknown \"" + EntityType.class.getSimpleName() + "\" enum value: " + entity);
                }
            }
        }

        this.anti_chain_enabled = getSection().getBoolean("anti_chain.enabled");
        if (this.anti_chain_enabled) {
            this.anti_chain_prevent_tnt = getSection().getBoolean("anti_chain.prevent_tnt_chains");
            this.anti_chain_prevent_creeper = getSection().getBoolean("anti_chain.prevent_creeper_chains");
            this.anti_chain_prevent_crystal = getSection().getBoolean("anti_chain.prevent_crystal_chains");
            this.anti_chain_prevent_block_ignition = getSection().getBoolean("anti_chain.prevent_block_ignition");
            this.anti_chain_radius = getSection().getDouble("anti_chain.radius");
            this.anti_chain_radius_squared = this.anti_chain_radius * this.anti_chain_radius;
            this.anti_chain_max_size = getSection().getInt("anti_chain.max_chain_size");
            this.anti_chain_cooldown = getSection().getLong("anti_chain.chain_cooldown");
        }

        this.management_enabled = getSection().getBoolean("management.enabled");
        if (this.management_enabled) {
            this.management_cancel_block_explosions = getSection().getBoolean("management.cancel_block_explosions");
            this.management_cancel_creepers = getSection().getBoolean("management.cancel_creepers");
            this.management_cancel_crystals = getSection().getBoolean("management.cancel_crystals");
            this.management_cancel_fireballs = getSection().getBoolean("management.cancel_fireballs");
            this.management_cancel_tnt = getSection().getBoolean("management.cancel_tnt");
            this.management_cancel_wither_skulls = getSection().getBoolean("management.cancel_wither_skulls");

            this.management_explosion_damage = getSection().getBoolean("management.explosion_damage.enabled");
            this.management_explosion_damage_multiplier = (float) getSection().getDouble("management.explosion_damage.multiplier");

            this.management_explosion_sound = getSection().getBoolean("management.explosion_sound.enabled");
            this.management_explosion_sound_volume = (float) getSection().getDouble("management.explosion_sound.volume");

            this.management_explosion_knockback = getSection().getBoolean("management.explosion_knockback.enabled");
            this.management_explosion_knockback_multiplier = (float) getSection().getDouble("management.explosion_knockback.multiplier");
        }

        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        /*if (antiChainCleanupTask != null) {
            antiChainCleanupTask.cancel();
        }*/
    }
}