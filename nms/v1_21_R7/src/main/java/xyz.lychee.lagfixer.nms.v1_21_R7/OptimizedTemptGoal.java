package xyz.lychee.lagfixer.nms.v1_21_R7;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

import java.util.EnumSet;

public class OptimizedTemptGoal extends Goal {
    private final MobAiReducerModule module;
    private final PathfinderMob mob;
    private final TargetingConditions targeting;
    private int cooldown = 0;
    private Player targetPlayer;

    public OptimizedTemptGoal(MobAiReducerModule module, PathfinderMob mob, TargetingConditions targeting) {
        this.module = module;
        this.mob = mob;
        this.targeting = targeting;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.targetPlayer = getServerLevel(this.mob).getNearestPlayer(this.targeting, this.mob);
        if (this.targetPlayer == null) return false;

        if (this.module.isTemptEvent()) {
            EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, this.targetPlayer, EntityTargetEvent.TargetReason.TEMPT);
            return !event.isCancelled();
        }
        return true;
    }

    public void tick() {
        if (this.mob.distanceToSqr(this.targetPlayer) >= 6.25d || this.module.isTemptTeleport()) {
            if (this.module.isTemptTeleport()) {
                this.mob.teleportTo(this.targetPlayer.getX(), this.targetPlayer.getY(), this.targetPlayer.getZ());
            } else {
                this.mob.getNavigation().moveTo(this.targetPlayer, this.mob instanceof Animal ? this.module.getTemptSpeed() : 0.35d);
            }
        } else {
            this.mob.getNavigation().stop();
        }
        this.cooldown = this.module.getTemptCooldown();
    }
}