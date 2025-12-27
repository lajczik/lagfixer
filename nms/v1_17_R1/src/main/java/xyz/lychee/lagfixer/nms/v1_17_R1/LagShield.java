package xyz.lychee.lagfixer.nms.v1_17_R1;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
import org.bukkit.entity.Entity;
import xyz.lychee.lagfixer.modules.LagShieldModule;

public class LagShield extends LagShieldModule.NMS {
    public LagShield(LagShieldModule module) {
        super(module);
    }

    @Override
    public boolean isSupportSimulation() {
        return false;
    }

    @Override
    public void setViewDistance(World world, int view) {
        int clampedView = Mth.clamp(view, 2, 32);

        ServerLevel level = ((CraftWorld) world).getHandle();
        if (level.spigotConfig.viewDistance != clampedView) {
            level.spigotConfig.viewDistance = clampedView;
            level.getChunkSource().setViewDistance(clampedView);
        }
    }

    @Override
    public void setSimulationDistance(World world, int simulation) {
        //Not supported
    }

    @Override
    public void setEntityAi(Entity ent, boolean bl) {
        if (ent instanceof CraftCreature) {
            PathfinderMob mob = ((CraftCreature) ent).getHandle();
            if (mob.isNoAi() != bl) return;

            mob.setNoAi(!bl);
            mob.setAggressive(!bl);
            mob.setSilent(!bl);
            mob.collides = !bl;
        }
    }
}