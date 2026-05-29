package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import xyz.lychee.lagfixer.modules.MobAiReducerModule;

import java.util.EnumSet;

public class OptimizedTemptGoal extends PathfinderGoal {
    private final MobAiReducerModule module;
    private final EntityCreature mob;
    private final PathfinderTargetCondition targeting;
    private int cooldown = 0;

    public OptimizedTemptGoal(MobAiReducerModule module, EntityCreature mob, PathfinderTargetCondition targeting) {
        this.module = module;
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
        this.cooldown = this.module.getTemptCooldown();
        EntityHuman player = this.mob.getWorld().a(this.targeting, this.mob);
        if (player != null) {
            if (this.module.isTemptEvent()) {
                EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.mob, player, EntityTargetEvent.TargetReason.TEMPT);
                if (event.isCancelled()) {
                    return;
                }
            }
            if (this.mob.h(player) >= 6.25d || this.module.isTemptTeleport()) {
                if (this.module.isTemptTeleport()) {
                    this.mob.enderTeleportTo(player.locX(), player.locY(), player.locZ());
                } else {
                    this.mob.getNavigation().a(player, this.mob instanceof EntityAnimal ? this.module.getTemptSpeed() : 0.35d);
                }
                return;
            }
            this.mob.getNavigation().o();
        }
    }
}