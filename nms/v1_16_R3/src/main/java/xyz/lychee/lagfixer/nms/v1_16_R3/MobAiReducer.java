package xyz.lychee.lagfixer.nms.v1_16_R3;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_16_R3.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    @Override
    public void optimize(org.bukkit.entity.Entity ent, boolean init) {
        if (!(ent instanceof CraftCreature)) return;

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

        if (this.fieldGoals == null) return;

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
                    toAdd.add(new PathfinderGoalWrapped(pgw.h(), new OptimizedBreedGoal((EntityAnimal) handle)));
                    continue;
                }
                if (module.isTemptEnabled() && goalClass == PathfinderGoalTempt.class && temptTargeting != null) {
                    toRemove.add(pgw);
                    toAdd.add(new PathfinderGoalWrapped(pgw.h(), new OptimizedTemptGoal(handle, temptTargeting)));
                    continue;
                }

                String simpleName = goalClass.getSimpleName();
                if (aiList.stream().anyMatch(simpleName::contains) == aiListMode) {
                    toRemove.add(pgw);
                }
            }

            if (!toRemove.isEmpty()) goals.removeAll(toRemove);
            if (!toAdd.isEmpty()) goals.addAll(toAdd);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
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

        SupportManager.getInstance().getExecutor().execute(() -> {
            org.bukkit.entity.Entity[] entities = e.getChunk().getEntities();
            for (org.bukkit.entity.Entity entity : entities) {
                if (this.getModule().isEnabled(entity)) {
                    this.optimize(entity, false);
                }
            }
        });
    }

    public void removeGoals(Set<PathfinderGoalWrapped> goals, Predicate<PathfinderGoalWrapped> filter) {
        for (Iterator<PathfinderGoalWrapped> it = goals.iterator(); it.hasNext(); ) {
            PathfinderGoalWrapped wg = it.next();
            if (wg != null && filter.test(wg)) {
                wg.d();
                it.remove();
            }
        }
    }

    /*public class OptimizedPanicGoal extends PathfinderGoal {
        protected final EntityCreature mob;
        private EntityLiving lastHurtByMob;
        private int cooldown = 0;

        public OptimizedPanicGoal(EntityCreature mob) {
            this.mob = mob;
            a(EnumSet.of(Type.MOVE));
        }

        public boolean a() {
            int i = this.cooldown;
            this.cooldown = i - 1;
            if (i <= 0) {
                EntityLiving lastHurtByMob = this.mob.getLastDamager();
                this.lastHurtByMob = lastHurtByMob;
                return lastHurtByMob != null;
            }
            return false;
        }

        public boolean b() {
            return this.mob.h(this.lastHurtByMob) < getModule().getPanicRange();
        }

        public void e() {
            Vec3D randomPos = RandomPositionGenerator.c(this.mob, 16, 7, this.lastHurtByMob.getPositionVector());
            if (randomPos != null) {
                this.cooldown = getModule().getPanicCooldown();
                this.mob.getNavigation().a(randomPos.x, randomPos.y, randomPos.z, getModule().getPanicSpeed());
            }
        }
    }*/

    public class OptimizedTemptGoal extends PathfinderGoal {
        private final EntityCreature mob;
        private final PathfinderTargetCondition targeting;
        private int cooldown = 0;

        public OptimizedTemptGoal(EntityCreature mob, PathfinderTargetCondition targeting) {
            this.mob = mob;
            this.targeting = targeting;
            a(EnumSet.of(Type.MOVE));
        }

        @Override
        public boolean a() {
            return --this.cooldown <= 0;
        }

        @Override
        public void e() {
            this.cooldown = getModule().getTemptCooldown();
            EntityHuman player = this.mob.getWorld().a(this.targeting, this.mob);
            if (player != null) {
                if (getModule().isTemptEvent()) {
                    EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, player, EntityTargetEvent.TargetReason.TEMPT);
                    if (event.isCancelled()) {
                        return;
                    }
                }
                if (this.mob.h(player) >= 6.25d || getModule().isTemptTeleport()) {
                    if (getModule().isTemptTeleport()) {
                        this.mob.enderTeleportTo(player.locX(), player.locY(), player.locZ());
                    } else {
                        this.mob.getNavigation().a(player, this.mob instanceof EntityAnimal ? getModule().getTemptSpeed() : 0.35d);
                    }
                    return;
                }
                this.mob.getNavigation().o();
            }
        }
    }

    public class OptimizedBreedGoal extends PathfinderGoal {
        protected final EntityAnimal animal;
        protected EntityAnimal partner;

        public OptimizedBreedGoal(EntityAnimal entityanimal) {
            this.animal = entityanimal;
            a(EnumSet.of(Type.MOVE));
        }

        public boolean a() {
            if (this.animal.isInLove()) {
                EntityAnimal freePartner = getFreePartner();
                this.partner = freePartner;
                return freePartner != null;
            }
            return false;
        }

        public boolean b() {
            return this.partner.isAlive() && this.partner.isInLove();
        }

        public void e() {
            if (getModule().isBreedTeleport()) {
                this.animal.enderTeleportTo(this.partner.locX(), this.partner.locY(), this.partner.locZ());
            } else {
                this.animal.getNavigation().a(this.partner, getModule().getBreedSpeed());
            }
            this.animal.a(this.animal.getWorld().getMinecraftWorld(), this.partner);
        }

        private EntityAnimal getFreePartner() {
            List<? extends EntityAnimal> nearbyEntities = this.animal.getWorld().a(this.animal.getClass(), MobAiReducer.this.breedTargeting, this.animal, this.animal.getBoundingBox().g(8.0d));
            if (nearbyEntities.isEmpty()) {
                return null;
            }
            Stream<? extends EntityAnimal> stream = nearbyEntities.stream();
            EntityAnimal entityAnimal = this.animal;
            Objects.requireNonNull(entityAnimal);
            return stream.filter(entityAnimal::mate)
                    .min(Comparator.comparingDouble(other -> other.h(this.animal)))
                    .orElse(null);
        }
    }
}