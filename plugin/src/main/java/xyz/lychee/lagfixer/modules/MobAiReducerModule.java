package xyz.lychee.lagfixer.modules;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Getter
public class MobAiReducerModule extends AbstractModule implements Listener {
    private final EnumSet<EntityType> list = EnumSet.noneOf(EntityType.class);
    private final EnumSet<CreatureSpawnEvent.SpawnReason> reasons = EnumSet.noneOf(CreatureSpawnEvent.SpawnReason.class);
    private final HashSet<String> ai_list = new HashSet<>();
    private ScheduledTask task;
    private NMS mobAiReducer;
    private boolean ignore_models;
    private boolean animals;
    private boolean monsters;
    private boolean villagers;
    private boolean tameable;
    private boolean birds;
    private boolean others;
    private boolean list_mode;
    private boolean force_load;
    private boolean click_event;
    private int purge_interval;
    private boolean collides;
    private boolean silent;
    private boolean keep_dedicated;
    private boolean ai_list_mode;

    private boolean temptEnabled;
    private double temptRange;
    private double temptSpeed;
    private int temptCooldown;
    private boolean temptTriggerBothHands;
    private boolean temptEvent;
    private boolean temptTeleport;

    private boolean breedEnabled;
    private double breedRange;
    private boolean breedEvent;
    private boolean breedTeleport;
    private double breedSpeed;

    public MobAiReducerModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, AbstractModule.Impact.VERY_HIGH, "MobAiReducer",
                new String[]{
                        "Replaces creature movement to optimize and reduce behavior.",
                        "Addresses inefficiencies caused by default animal behavior like unnecessary random movements or constant looking around.",
                        "MobAiReducer intervenes by disabling unnecessary PathFinders or replacing them with more efficient ones.",
                        "Crucial in scenarios with numerous animals as even minor movements can strain server resources."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGM3NTA1ZjIyNGQ1MTY0YTExN2Q4YzY5ZjAxNWY5OWVmZjQzNDQ3MWM4YTJkZjkwNzA5NmM0MjQyYzM1MjRlOCJ9fX0=");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpawn(CreatureSpawnEvent e) {
        if (!reasons.contains(e.getSpawnReason())
                || !this.isEnabled(e.getEntity())
                || !this.canContinue(e.getEntity().getWorld())
        ) return;

<<<<<<< HEAD
        this.mobAiReducer.optimize(e.getEntity(), false);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(EntitiesLoadEvent e) {
        if (this.canContinue(e.getWorld())) {
            for (Entity entity : e.getEntities()) {
                if (this.isEnabled(entity)) {
                    this.mobAiReducer.optimize(entity, false);
                }
            }
=======
        if (this.async) {
            SupportManager.getInstance().getFork().runNow(true, e.getLocation(), () -> this.mobAiReducer.optimize(e.getEntity(), false));
        } else {
            this.mobAiReducer.optimize(e.getEntity(), false);
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        }
    }

    public boolean isEnabled(Entity ent) {
        if ((!this.ignore_models && HookManager.getInstance().hasModel(ent)) || this.list.contains(ent.getType()) != this.list_mode)
            return false;

        return switch (ent) {
            case Villager ignored -> villagers;
            case Tameable ignored -> tameable;
            case Flying ignored -> birds;
            case Animals ignored -> animals;
            case Monster ignored -> monsters;
            default -> others;
        };
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
        Bukkit.getPluginManager().registerEvents(this.mobAiReducer, this.getPlugin());

        if (this.force_load) {
            RegionScheduler scheduler = Bukkit.getRegionScheduler();

            this.getAllowedWorlds().forEach(world -> {
                Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
                regions.forEach((regionPos, chunks) -> {
                    Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        for (Chunk chunk : chunks) {
                            Entity[] entities = chunk.getEntities();
                            for (Entity entity : entities) {
                                if (this.isEnabled(entity)) {
                                    this.mobAiReducer.optimize(entity, true);
                                }
                            }
                        }
                    }, executor);
                });
            });
        }

        this.task = Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), t -> this.mobAiReducer.purge(), 60, this.purge_interval, TimeUnit.SECONDS);
    }

    @Override
    public boolean loadConfig() {
        this.mobAiReducer = ReflectionUtils.createInstance("MobAiReducer", this);

        this.ignore_models = HookManager.getInstance().noneModels() || this.getSection().getBoolean("ignore_models");
        this.animals = this.getSection().getBoolean("entities.animals");
        this.monsters = this.getSection().getBoolean("entities.monsters");
        this.villagers = this.getSection().getBoolean("entities.villagers");
        this.tameable = this.getSection().getBoolean("entities.tameable");
        this.birds = this.getSection().getBoolean("entities.birds");
        this.others = this.getSection().getBoolean("entities.others");
        this.force_load = this.getSection().getBoolean("force_load");
        this.click_event = this.getSection().getBoolean("click_event");
        this.purge_interval = this.getSection().getInt("purge_interval");

        this.list_mode = this.getSection().getBoolean("list_mode");
        ReflectionUtils.convertEnums(EntityType.class, this.list, this.getSection().getStringList("list"));
        ReflectionUtils.convertEnums(CreatureSpawnEvent.SpawnReason.class, this.reasons, this.getSection().getStringList("spawn_reasons"));

        this.collides = this.getSection().getBoolean("collides");
        this.silent = this.getSection().getBoolean("silent");

        this.keep_dedicated = this.getSection().getBoolean("pathfinder.keep_dedicated");
        this.ai_list_mode = this.getSection().getBoolean("pathfinder.list_mode");
        this.ai_list.clear();
        this.ai_list.addAll(this.getSection().getStringList("pathfinder.list"));

        this.temptEnabled = this.getSection().getBoolean("animals.tempt.enabled");
        this.temptRange = this.getSection().getDouble("animals.tempt.range");
        this.temptSpeed = this.getSection().getDouble("animals.tempt.speed");
        this.temptCooldown = this.getSection().getInt("animals.tempt.cooldown");
        this.temptTriggerBothHands = this.getSection().getBoolean("animals.tempt.trigger_two_hands");
        this.temptEvent = this.getSection().getBoolean("animals.tempt.event");
        this.temptTeleport = this.getSection().getBoolean("animals.tempt.teleport");

        this.breedEnabled = this.getSection().getBoolean("animals.breed.enabled");
        this.breedRange = this.getSection().getDouble("animals.breed.range");
        this.breedSpeed = this.getSection().getDouble("animals.breed.speed");
        this.breedEvent = this.getSection().getBoolean("animals.breed.event");
        this.breedTeleport = this.getSection().getBoolean("animals.breed.teleport");

        /*this.panicEnabled = this.getSection().getBoolean("animals.tempt.enabled");
        this.panicRange = this.getSection().getDouble("animals.panic.range");
        this.panicCooldown = this.getSection().getInt("animals.panic.cooldown");
        this.panicSpeed = this.getSection().getDouble("animals.panic.speed");*/

        if (this.mobAiReducer != null) {
            this.mobAiReducer.load();
            return true;
        }

        return false;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.task != null && !this.task.isCancelled())
            this.task.cancel();
    }

    @Getter
    public static abstract class NMS implements Listener {
        private final MobAiReducerModule module;

        public NMS(MobAiReducerModule module) {
            this.module = module;
        }

        public abstract void load();

        public abstract void optimize(Entity entity, boolean init);

        public abstract void purge();
    }
}