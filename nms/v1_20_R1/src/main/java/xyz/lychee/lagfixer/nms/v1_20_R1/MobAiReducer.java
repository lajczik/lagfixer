package xyz.lychee.lagfixer.nms.v1_20_R1;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftCreature;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

        this.register(Cow.class, Items.WHEAT);
        this.register(MushroomCow.class, Items.WHEAT);
        this.register(Sheep.class, Items.WHEAT);
        this.register(Pig.class, Items.CARROT, Items.POTATO, Items.BEETROOT);
        this.register(Chicken.class, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
        this.register(Rabbit.class, Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION);
        this.register(Horse.class, Items.APPLE, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.SUGAR, Items.HAY_BLOCK, Items.WHEAT);
        this.register(Donkey.class, Items.APPLE, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT, Items.SUGAR, Items.HAY_BLOCK, Items.WHEAT);
        this.register(Llama.class, Items.HAY_BLOCK, Items.WHEAT);
        this.register(Parrot.class, Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
        this.register(Ocelot.class, Items.COD, Items.SALMON);
        this.register(Cat.class, Items.COD, Items.SALMON);
        this.register(Panda.class, Items.BAMBOO, Items.CAKE);
        this.register(Fox.class, Items.SWEET_BERRIES, Items.GLOW_BERRIES);
        this.register(Turtle.class, Items.SEAGRASS);
        this.register(Bee.class, Items.POPPY, Items.DANDELION, Items.BLUE_ORCHID, Items.ALLIUM, Items.AZURE_BLUET, Items.RED_TULIP, Items.ORANGE_TULIP, Items.WHITE_TULIP, Items.PINK_TULIP, Items.OXEYE_DAISY, Items.CORNFLOWER, Items.LILY_OF_THE_VALLEY, Items.SUNFLOWER);
        this.register(Strider.class, Items.WARPED_FUNGUS);
        this.register(Goat.class, Items.WHEAT);
        this.register(Axolotl.class, Items.TROPICAL_FISH_BUCKET);
        this.register(Frog.class, Items.SLIME_BALL);
        this.register(Camel.class, Items.CACTUS);
        this.register(Sniffer.class, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);
    }

    private void register(Class<? extends Entity> clazz, Item... items) {
        Set<Item> itemSet = Sets.newHashSet(items);
        this.temptTargeting.computeIfAbsent(clazz, k -> TargetingConditions.forNonCombat().ignoreLineOfSight())
                .range(this.getModule().getTemptRange())
                .selector(entity ->
                        itemSet.contains(entity.getMainHandItem().getItem()) || (this.getModule().isTemptTriggerBothHands() && itemSet.contains(entity.getOffhandItem().getItem()))
                );
    }

    @Override
    public void optimize(org.bukkit.entity.Entity ent, boolean init) {
        if (!(ent instanceof CraftCreature)) return;

        PathfinderMob handle = ((CraftCreature) ent).getHandle();
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
                    toAdd.add(new WrappedGoal(pgw.getPriority(), new OptimizedBreedGoal(this.getModule(), (Animal) handle, this.breedTargeting)));
                    continue;
                }

                if (module.isTemptEnabled() && goalClass == TemptGoal.class && temptTargeting != null) {
                    toRemove.add(pgw);
                    toAdd.add(new WrappedGoal(pgw.getPriority(), new OptimizedTemptGoal(this.getModule(), handle, temptTargeting)));
                    continue;
                }

                String simpleName = goalClass.getSimpleName();
                if (aiList.stream().anyMatch(simpleName::contains) == aiListMode) {
                    toRemove.add(pgw);
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
}