package xyz.lychee.lagfixer.nms.v1_16_R3;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MobAiReducer extends MobAiReducerModule.NMS implements Listener {
    private final Map<EntityCreature, Boolean> optimizedMobs = new MapMaker().weakKeys().concurrencyLevel(4).makeMap();
    private final HashMap<Class<? extends Entity>, PathfinderTargetCondition> temptTargeting = new HashMap<>();
    private final PathfinderTargetCondition breedTargeting = new PathfinderTargetCondition().a().b().d().c();
    private final Field fieldGoals;

    public MobAiReducer(MobAiReducerModule module) {
        super(module);

        this.fieldGoals = ReflectionUtils.getPrivateField(PathfinderGoalSelector.class, Set.class);
    }

    @Override
    public void load() {
        this.breedTargeting.a(this.getModule().getBreedRange());

        this.register(EntityCow.class, Items.WHEAT);
        this.register(EntityMushroomCow.class, Items.WHEAT);
        this.register(EntitySheep.class, Items.WHEAT);
        this.register(EntityPig.class, Items.CARROT, Items.POTATO, Items.BEETROOT);
        this.register(EntityChicken.class, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
        this.register(EntityRabbit.class, Items.CARROT, Items.GOLDEN_CARROT);
        this.register(EntityHorse.class, Items.APPLE, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.SUGAR, Items.WHEAT);
        this.register(EntityLlama.class, Items.WHEAT, Blocks.HAY_BLOCK.getItem());
        this.register(EntityParrot.class, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
        this.register(EntityOcelot.class, Items.COD, Items.SALMON);
        this.register(EntityCat.class, Items.COD, Items.SALMON);
        this.register(EntityPanda.class, Blocks.BAMBOO.getItem());
        this.register(EntityFox.class, Items.SWEET_BERRIES);
        this.register(EntityStrider.class, Items.bx, Items.WARPED_FUNGUS_ON_A_STICK);
    }

    private void register(Class<? extends Entity> clazz, Item... items) {
        Set<Item> itemSet = Sets.newHashSet(items);
        this.temptTargeting.computeIfAbsent(clazz, k -> new PathfinderTargetCondition().a().b().d().c())
                .a(this.getModule().getTemptRange())
                .a(entity ->
                        itemSet.contains(entity.getItemInMainHand().getItem()) || (this.getModule().isTemptTriggerBothHands() && itemSet.contains(entity.getItemInOffHand().getItem()))
                );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void optimize(org.bukkit.entity.Entity ent, boolean init) {
        if (!(ent instanceof CraftCreature) || this.fieldGoals == null) return;

        EntityCreature handle = ((CraftCreature) ent).getHandle();
        if (this.optimizedMobs.containsKey(handle)) return;

        MobAiReducerModule module = this.getModule();

        boolean keepDedicated = module.isKeep_dedicated();
        boolean aiListMode = module.isAi_list_mode();
        HashSet<String> aiList = module.getAi_list();

        handle.collides = module.isCollides();
        handle.setSilent(module.isSilent());
        optimizedMobs.put(handle, Boolean.TRUE);

        boolean isAnimal = handle instanceof EntityAnimal;
        Class<?> handleClass = handle.getClass();
        PathfinderTargetCondition temptTargeting = module.isTemptEnabled() ?
                this.temptTargeting.get(handleClass) : null;

        try {
            Set<PathfinderGoalWrapped> goals = (Set<PathfinderGoalWrapped>) this.fieldGoals.get(handle.goalSelector);

            HashSet<PathfinderGoalWrapped> toAdd = new HashSet<>();
            HashSet<PathfinderGoalWrapped> toRemove = new HashSet<>();

            for (PathfinderGoalWrapped pgw : goals) {
                PathfinderGoal goal = pgw.j();
                Class<?> goalClass = goal.getClass();

                if (keepDedicated && !StringUtils.contains(goalClass.getName(), '$')) {
                    continue;
                }

                if (isAnimal && module.isBreedEnabled() && goalClass == PathfinderGoalBreed.class) {
                    toRemove.add(pgw);
                    pgw.d();

                    toAdd.add(new PathfinderGoalWrapped(pgw.h(), new OptimizedBreedGoal(this.getModule(), (EntityAnimal) handle, this.breedTargeting)));
                    continue;
                }
                if (module.isTemptEnabled() && goalClass == PathfinderGoalTempt.class && temptTargeting != null) {
                    toRemove.add(pgw);
                    pgw.d();

                    toAdd.add(new PathfinderGoalWrapped(pgw.h(), new OptimizedTemptGoal(this.getModule(), handle, temptTargeting)));
                    continue;
                }

                String simpleName = goalClass.getSimpleName();
                if (aiList.stream().anyMatch(simpleName::contains) == aiListMode) {
                    toRemove.add(pgw);
                    pgw.d();
                }
            }

            if (!toRemove.isEmpty()) goals.removeAll(toRemove);
            if (!toAdd.isEmpty()) goals.addAll(toAdd);
        } catch (IllegalAccessException ex) {
            this.getModule().getPlugin().printError(ex);
        }
    }

    @Override
    public void purge() {
        synchronized (this.optimizedMobs) {
            this.optimizedMobs.keySet().removeIf(ent -> !ent.isAlive() || !ent.valid);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(ChunkLoadEvent e) {
        if (!this.getModule().canContinue(e.getWorld())) return;

        if (this.getModule().isAsync()) {
            Chunk chunk = e.getChunk();
            SupportManager.getInstance().getFork()
                    .runNow(
                            true,
                            new Location(chunk.getWorld(), chunk.getX() << 4, 64, chunk.getZ() << 4),
                            () -> this.optimizeEntities(e.getChunk().getEntities())
                    );
        } else {
            this.optimizeEntities(e.getChunk().getEntities());
        }
    }

    public void optimizeEntities(org.bukkit.entity.Entity[] array) {
        for (org.bukkit.entity.Entity entity : array) {
            if (this.getModule().isEnabled(entity)) {
                this.optimize(entity, false);
            }
        }
    }
}