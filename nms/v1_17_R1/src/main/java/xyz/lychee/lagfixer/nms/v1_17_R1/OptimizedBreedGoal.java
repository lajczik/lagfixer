package xyz.lychee.lagfixer.nms.v1_17_R1;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.craftbukkit.v1_17_R1.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class OptimizedBreedGoal extends Goal {
    private final MobAiReducerModule module;
    private final TargetingConditions targeting;
    private final Animal animal;
    private Animal partner;

    public OptimizedBreedGoal(MobAiReducerModule module, Animal entityanimal, TargetingConditions targeting) {
        this.module = module;
        this.animal = entityanimal;
        this.targeting = targeting;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    public boolean canUse() {
        if (!this.animal.isInLove()) return false;

        this.partner = getFreePartner();
        if (this.partner == null || !this.partner.isAlive() || !this.partner.isInLove()) return false;

        if (this.module.isBreedEvent()) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.animal, this.partner, EntityTargetEvent.TargetReason.CUSTOM);
            return !event.isCancelled();
        }
        return true;
    }

    public void tick() {
        if (this.module.isBreedTeleport()) {
            this.animal.teleportTo(this.partner.getX(), this.partner.getY(), this.partner.getZ());
        } else {
            this.animal.getNavigation().moveTo(this.partner, this.module.getBreedSpeed());
        }
        this.animal.spawnChildFromBreeding(this.animal.level.getMinecraftWorld(), this.partner);
    }

    private Animal getFreePartner() {
        List<? extends Animal> nearbyEntities = this.animal.level.getNearbyEntities(this.animal.getClass(), this.targeting, this.animal, this.animal.getBoundingBox().inflate(8.0d));
        return nearbyEntities.stream()
                .filter(this.animal::canMate)
                .min(Comparator.comparingDouble(other -> other.distanceToSqr(this.animal)))
                .orElse(null);
    }
}