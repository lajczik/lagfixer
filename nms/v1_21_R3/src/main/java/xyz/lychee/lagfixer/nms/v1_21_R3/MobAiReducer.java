package xyz.lychee.lagfixer.nms.v1_21_R3;

import com.google.common.collect.MapMaker;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.Item;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftCreature;
<<<<<<< HEAD
=======
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import xyz.lychee.lagfixer.managers.SupportManager;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

import java.util.*;

public class MobAiReducer extends MobAiReducerModule.NMS {
    private final Map<PathfinderMob, Boolean> optimizedMobs = new MapMaker().weakKeys().concurrencyLevel(4).makeMap();
    private final Map<Class<? extends Entity>, TargetingConditions> temptTargeting = new HashMap<>();
    private final TargetingConditions breedTargeting = TargetingConditions.forNonCombat().ignoreLineOfSight();

    public MobAiReducer(MobAiReducerModule module) {
        super(module);
    }

    @Override
    public void load() {
        this.breedTargeting.range(this.getModule().getBreedRange());

        this.register(Horse.class, ItemTags.HORSE_FOOD);
        this.register(Cow.class, ItemTags.COW_FOOD);
        this.register(Sheep.class, ItemTags.SHEEP_FOOD);
        this.register(Fox.class, ItemTags.FOX_FOOD);
        this.register(Pig.class, ItemTags.PIG_FOOD);
        this.register(Chicken.class, ItemTags.CHICKEN_FOOD);
        this.register(Parrot.class, ItemTags.PARROT_FOOD);
        this.register(Frog.class, ItemTags.FROG_FOOD);
        this.register(Axolotl.class, ItemTags.AXOLOTL_FOOD);
        this.register(Goat.class, ItemTags.GOAT_FOOD);
        this.register(Bee.class, ItemTags.BEE_FOOD);
        this.register(Wolf.class, ItemTags.WOLF_FOOD);
        this.register(Turtle.class, ItemTags.TURTLE_FOOD);
        this.register(Strider.class, ItemTags.STRIDER_FOOD);
        this.register(Rabbit.class, ItemTags.RABBIT_FOOD);
        this.register(Piglin.class, ItemTags.PIGLIN_FOOD);
        this.register(Panda.class, ItemTags.PANDA_FOOD);
        this.register(Ocelot.class, ItemTags.OCELOT_FOOD);
        this.register(Llama.class, ItemTags.LLAMA_FOOD);
        this.register(Hoglin.class, ItemTags.HOGLIN_FOOD);
        this.register(Camel.class, ItemTags.CAMEL_FOOD);
        this.register(Armadillo.class, ItemTags.ARMADILLO_FOOD);
        this.register(Sniffer.class, ItemTags.SNIFFER_FOOD);
    }

    private void register(Class<? extends Entity> clazz, TagKey<Item> item) {
        this.temptTargeting.computeIfAbsent(clazz, k -> TargetingConditions.forNonCombat().ignoreLineOfSight())
                .range(this.getModule().getTemptRange())
                .selector((entity, level) ->
                        entity.getMainHandItem().is(item) || (this.getModule().isTemptTriggerBothHands() && entity.getOffhandItem().is(item))
                );
    }

    @Override
    public void optimize(org.bukkit.entity.Entity ent, boolean init) {
        if (!(ent instanceof CraftCreature creature)) return;

        PathfinderMob handle = creature.getHandle();
        if (optimizedMobs.containsKey(handle)) return;

        MobAiReducerModule module = this.getModule();

        boolean keepDedicated = module.isKeep_dedicated();
        boolean aiListMode = module.isAi_list_mode();
        HashSet<String> aiList = module.getAi_list();

        handle.collides = module.isCollides();
        handle.setSilent(module.isSilent());
        optimizedMobs.put(handle, Boolean.TRUE);

        boolean isAnimal = handle instanceof Animal;
        Class<?> handleClass = handle.getClass();
        TargetingConditions temptTargeting = module.isTemptEnabled() ?
                this.temptTargeting.get(handleClass) : null;

        Set<WrappedGoal> goals = handle.goalSelector.getAvailableGoals();
        synchronized (goals) {
            HashSet<WrappedGoal> toAdd = new HashSet<>();
            HashSet<WrappedGoal> toRemove = new HashSet<>();

            for (WrappedGoal pgw : goals) {
                Goal goal = pgw.getGoal();
                Class<?> goalClass = goal.getClass();

                if (keepDedicated && !goalClass.getName().contains("ai.goal")) {
                    continue;
                }

                if (isAnimal && module.isBreedEnabled() && goalClass == BreedGoal.class) {
                    toRemove.add(pgw);
<<<<<<< HEAD
=======
                    pgw.stop();

>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
                    toAdd.add(new WrappedGoal(pgw.getPriority(), new OptimizedBreedGoal(this.getModule(), (Animal) handle, this.breedTargeting)));
                    continue;
                }

                if (module.isTemptEnabled() && goalClass == TemptGoal.class && temptTargeting != null) {
                    toRemove.add(pgw);
<<<<<<< HEAD
=======
                    pgw.stop();

>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
                    toAdd.add(new WrappedGoal(pgw.getPriority(), new OptimizedTemptGoal(this.getModule(), handle, temptTargeting)));
                    continue;
                }

                String simpleName = goalClass.getSimpleName();
                if (aiList.stream().anyMatch(simpleName::contains) == aiListMode) {
                    toRemove.add(pgw);
                    pgw.stop();
                }
            }

            if (!toRemove.isEmpty()) goals.removeAll(toRemove);
            if (!toAdd.isEmpty()) goals.addAll(toAdd);
        }
    }

    @Override
    public void purge() {
        synchronized (this.optimizedMobs) {
            this.optimizedMobs.keySet().removeIf(ent -> !ent.isAlive() || !ent.valid);
        }
    }
<<<<<<< HEAD
=======

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(EntitiesLoadEvent e) {
        if (!this.getModule().canContinue(e.getWorld())) return;

        if (this.getModule().isAsync()) {
            Chunk chunk = e.getChunk();
            SupportManager.getInstance().getFork()
                    .runNow(
                            true,
                            new Location(chunk.getWorld(), chunk.getX() << 4, 64, chunk.getZ() << 4),
                            () -> this.optimizeEntities(e.getEntities())
                    );
        } else {
            this.optimizeEntities(e.getEntities());
        }
    }

    public void optimizeEntities(List<org.bukkit.entity.Entity> list) {
        for (org.bukkit.entity.Entity entity : list) {
            if (this.getModule().isEnabled(entity)) {
                this.optimize(entity, false);
            }
        }
    }
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}