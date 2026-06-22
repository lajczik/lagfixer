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
import org.bukkit.scheduler.BukkitTask;
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
    private final EnumMap<EntityType, Float> yieldLimitPerEntity = new EnumMap<>(EntityType.class);
    private final Map<String, Location> recentExplosions = new ConcurrentHashMap<>();
    private final Map<String, Location> protectedLocations = new ConcurrentHashMap<>();
    private boolean yieldLimitEnabled;
    private float yieldLimitDefault;
    private boolean antiChainEnabled;
    private boolean preventTntChains;
    private boolean preventCreeperChains;
    private boolean preventCrystalChains;
    private boolean preventBlockIgnition;
    private double chainRadius;
    private double chainRadiusSquared;
    private int maxChainLength;
    private long chainCooldownMs;
    private BukkitTask antiChainCleanupTask;

    private boolean managementEnabled;
    private boolean cancelBlockExplosions;
    private boolean cancelCreepers;
    private boolean cancelCrystals;
    private boolean cancelFireballs;
    private boolean cancelTnt;
    private boolean cancelWitherSkulls;
    private boolean allowExplosionDamage;
    private float explosionDamageMultiplier;
    private boolean allowExplosionSound;
    private float explosionSoundVolume;
    private boolean allowExplosionKnockback;
    private float explosionKnockbackMultiplier;
    private boolean explosionKnockbackFastIntSqrt;

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

        if (managementEnabled && shouldDeactivateExplosion(event.getEntity())) {
            event.setCancelled(true);
            this.handleExplosionEffects(event.getEntity(), 4.0f, event.getEntity().getLocation());
            return;
        }

        if (antiChainEnabled && shouldPreventChain(event.getEntity(), event.getEntity().getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (yieldLimitEnabled) {
            float currentYield = event.getRadius();
            float newYield = yieldLimitForEntity(event.getEntity(), currentYield);
            if (currentYield > newYield) {
                event.setRadius(newYield);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(@NotNull EntityExplodeEvent event) {
        if (!this.canContinue(event.getLocation().getWorld()) || event.blockList().isEmpty()) return;

        if (managementEnabled && shouldDeactivateExplosion(event.getEntity())) {
            event.setCancelled(true);
            this.handleExplosionEffects(event.getEntity(), event.getYield(), event.getLocation());
            return;
        }

        if (antiChainEnabled) {
            if (preventBlockIgnition) {
                event.blockList().removeIf(block -> block.getType() == Material.TNT);
            }
            this.addProtectedArea(event.getLocation());
        }

        if (yieldLimitEnabled) {
            float currentYield = event.getYield();
            float newYield = yieldLimitForEntity(event.getEntity(), currentYield);
            if (currentYield > newYield) {
                event.setYield(newYield);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(@NotNull BlockExplodeEvent event) {
        if (!this.canContinue(event.getBlock().getWorld()) || event.blockList().isEmpty()) return;

        if (managementEnabled && this.cancelBlockExplosions) {
            event.setCancelled(true);
            this.handleExplosionEffects(null, event.getYield(), event.getBlock().getLocation());
            return;
        }

        if (antiChainEnabled) {
            if (shouldPreventBlockExplosion(event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }

            if (preventBlockIgnition) {
                event.blockList().removeIf(block -> block.getType() == Material.TNT);
            }
            this.addProtectedArea(event.getBlock().getLocation());
            this.trackExplosion(event.getBlock().getLocation());
        }

        if (yieldLimitEnabled) {
            float currentYield = event.getYield();
            float newYield = Math.min(currentYield, yieldLimitDefault);
            if (currentYield > newYield) {
                event.setYield(newYield);
            }
        }
    }

    private float yieldLimitForEntity(@Nullable Entity entity, float currentYield) {
        if (entity == null) {
            return Math.min(currentYield, yieldLimitDefault);
        }
        if (currentYield <= yieldLimitDefault) {
            return currentYield;
        }
        return Math.min(currentYield, yieldLimitPerEntity.getOrDefault(entity.getType(), yieldLimitDefault));
    }

    private boolean shouldDeactivateExplosion(Entity entity) {
        return (entity instanceof TNTPrimed && cancelTnt)
                || (entity instanceof Creeper && cancelCreepers)
                || (entity instanceof EnderCrystal && cancelCrystals)
                || (entity instanceof WitherSkull && cancelWitherSkulls)
                || (entity instanceof Fireball && cancelFireballs);
    }

    private void handleExplosionEffects(Entity tnt, float power, Location location) {
        if (location == null || location.getWorld() == null) return;

        if (this.allowExplosionSound) {
            location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, this.explosionSoundVolume);
        }

        if (!this.allowExplosionDamage && !this.allowExplosionKnockback) return;

        double radius = power * 2.0;
        double radiusSquared = radius * radius;
        double invRadiusSquared = 1.0 / radiusSquared;

        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity)) continue;

            Location eLoc = entity.getLocation();
            double distanceSquared = location.distanceSquared(eLoc);

            if (this.allowExplosionDamage) {
                double falloff = (radiusSquared - distanceSquared) * invRadiusSquared;
                double damage = falloff * power * this.explosionDamageMultiplier;

                if (damage > 0) {
                    ((LivingEntity) entity).damage(damage, tnt);
                }
            }

            if (this.allowExplosionKnockback && distanceSquared > 0.01) {
                Vector direction = eLoc.toVector().subtract(location.toVector());

                double invSqrt;
                if (this.explosionKnockbackFastIntSqrt) {
                    double half = 0.5 * distanceSquared;
                    long i = Double.doubleToLongBits(distanceSquared);
                    i = 0x5fe6eb50c7b537a9L - (i >> 1);
                    double y = Double.longBitsToDouble(i);
                    invSqrt = y * (1.5 - half * y * y);
                } else {
                    invSqrt = 1.0 / Math.sqrt(distanceSquared);
                }

                double falloff = (radiusSquared - distanceSquared) * invRadiusSquared;
                double strength = (falloff * falloff) * power * this.explosionKnockbackMultiplier;

                Vector knockback = direction.multiply(invSqrt * strength);
                knockback.setY((knockback.getY() * 0.3) + 0.2);

                entity.setVelocity(entity.getVelocity().add(knockback));
            }
        }
    }

    private boolean shouldPreventChain(Entity entity, Location location) {
        if ((entity instanceof TNTPrimed && !preventTntChains)
                || (entity instanceof Creeper && !preventCreeperChains)
                || (entity instanceof EnderCrystal && !preventCrystalChains))
            return false;

        for (Location protectedLoc : protectedLocations.values()) {
            if (Objects.equals(protectedLoc.getWorld(), location.getWorld())
                    && protectedLoc.distanceSquared(location) <= chainRadiusSquared) {
                return true;
            }
        }

        return recentExplosions.containsValue(location);
    }

    private boolean shouldPreventBlockExplosion(Location location) {
        for (Location protectedLoc : protectedLocations.values()) {
            if (Objects.equals(protectedLoc.getWorld(), location.getWorld())
                    && protectedLoc.distanceSquared(location) <= chainRadiusSquared) {
                return true;
            }
        }
        return recentExplosions.containsValue(location);
    }

    private void trackExplosion(Location location) {
        String locationKey = getLocationKey(location);
        recentExplosions.put(locationKey, location);
        SupportManager.getInstance().getFork().runLater(true, () -> recentExplosions.remove(locationKey), chainCooldownMs);
    }

    private void addProtectedArea(Location location) {
        String locationKey = location.toString();
        recentExplosions.put(locationKey, location);
        SupportManager.getInstance().getFork().runLater(true, () -> protectedLocations.remove(locationKey), chainCooldownMs);
    }

    private String getLocationKey(Location location) {
        if (location == null || location.getWorld() == null) return "unknown";
        long x = Math.round(location.getX() / chainRadius);
        long y = Math.round(location.getY() / chainRadius);
        long z = Math.round(location.getZ() / chainRadius);
        return location.getWorld().getName() + ":" + x + ":" + y + ":" + z;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

        /*if (antiChainEnabled) {
            antiChainCleanupTask = SupportManager.getInstance().getFork().runTimer(true, () -> {

            }, 30, 30, TimeUnit.SECONDS);
        }*/
    }

    @Override
    public boolean loadConfig() {
        yieldLimitEnabled = getSection().getBoolean("yield_limit.enabled", true);
        if (yieldLimitEnabled) {
            yieldLimitDefault = (float) getSection().getDouble("yield_limit.default", 4.0d);
            for (String entity : this.getSection().getStringList("yield_limit.per_entity")) {
                try {
                    String[] split = entity.split(":");
                    yieldLimitPerEntity.put(
                            EntityType.valueOf(split[0].toUpperCase()),
                            Float.parseFloat(split[1])
                    );
                } catch (Exception e) {
                    getPlugin().getLogger().warning("Unknown \"" + EntityType.class.getSimpleName() + "\" enum value: " + entity);
                }
            }
        }

        antiChainEnabled = getSection().getBoolean("anti_chain.enabled", true);
        if (antiChainEnabled) {
            preventTntChains = getSection().getBoolean("anti_chain.prevent_tnt_chains", true);
            preventCreeperChains = getSection().getBoolean("anti_chain.prevent_creeper_chains", false);
            preventCrystalChains = getSection().getBoolean("anti_chain.prevent_crystal_chains", true);
            preventBlockIgnition = getSection().getBoolean("anti_chain.prevent_block_ignition", true);
            chainRadius = getSection().getDouble("anti_chain.chain_radius", 8.0);
            chainRadiusSquared = chainRadius * chainRadius;
            maxChainLength = getSection().getInt("anti_chain.max_chain_size", 3);
            chainCooldownMs = getSection().getLong("anti_chain.chain_cooldown", 1000L);
        }

        managementEnabled = getSection().getBoolean("management.enabled", false);
        if (managementEnabled) {
            cancelBlockExplosions = getSection().getBoolean("management.cancel_block_explosions", true);
            cancelCreepers = getSection().getBoolean("management.cancel_creepers", false);
            cancelCrystals = getSection().getBoolean("management.cancel_crystals", true);
            cancelFireballs = getSection().getBoolean("management.cancel_fireballs", true);
            cancelTnt = getSection().getBoolean("management.cancel_tnt", true);
            cancelWitherSkulls = getSection().getBoolean("management.cancel_wither_skulls", true);
            allowExplosionDamage = getSection().getBoolean("management.explosion_damage.enabled", false);
            explosionDamageMultiplier = (float) getSection().getDouble("management.explosion_damage.multiplier", 2.5);
            allowExplosionSound = getSection().getBoolean("management.explosion_sound.enabled", true);
            explosionSoundVolume = (float) getSection().getDouble("management.explosion_sound.volume", 1);
            allowExplosionKnockback = getSection().getBoolean("management.explosion_knockback.enabled", false);
            explosionKnockbackMultiplier = (float) getSection().getDouble("management.explosion_knockback.multiplier", 0.6);
            explosionKnockbackFastIntSqrt = getSection().getBoolean("management.explosion_knockback.use_newton_raphson", true);
        }

        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (antiChainCleanupTask != null) antiChainCleanupTask.cancel();
        recentExplosions.clear();
        protectedLocations.clear();
    }
}