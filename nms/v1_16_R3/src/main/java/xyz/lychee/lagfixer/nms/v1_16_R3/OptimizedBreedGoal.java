package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.EntityAnimal;
import net.minecraft.server.v1_16_R3.PathfinderGoal;
import net.minecraft.server.v1_16_R3.PathfinderTargetCondition;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class OptimizedBreedGoal extends PathfinderGoal {
    protected final EntityAnimal animal;
    private final MobAiReducerModule module;
    private final PathfinderTargetCondition targeting;
    protected EntityAnimal partner;

    public OptimizedBreedGoal(MobAiReducerModule module, EntityAnimal entityanimal, PathfinderTargetCondition targeting) {
        this.module = module;
        this.animal = entityanimal;
        this.targeting = targeting;
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
        if (this.module.isBreedTeleport()) {
            this.animal.enderTeleportTo(this.partner.locX(), this.partner.locY(), this.partner.locZ());
        } else {
            this.animal.getNavigation().a(this.partner, this.module.getBreedSpeed());
        }
        this.animal.a(this.animal.getWorld().getMinecraftWorld(), this.partner);
    }

    private EntityAnimal getFreePartner() {
        List<? extends EntityAnimal> nearbyEntities = this.animal.getWorld().a(this.animal.getClass(), this.targeting, this.animal, this.animal.getBoundingBox().g(8.0d));
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