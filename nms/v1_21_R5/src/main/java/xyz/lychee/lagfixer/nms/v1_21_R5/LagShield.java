package xyz.lychee.lagfixer.nms.v1_21_R5;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.entity.Entity;
import xyz.lychee.lagfixer.modules.LagShieldModule;

public class LagShield extends LagShieldModule.NMS {
    public LagShield(LagShieldModule module) {
        super(module);
    }

    @Override
    public boolean isSupportSimulation() {
        return true;
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
        ServerLevel level = ((CraftWorld) world).getHandle();

        int clampedSimulation = Mth.clamp(simulation, 1, level.spigotConfig.viewDistance);
        if (level.spigotConfig.simulationDistance != clampedSimulation) {
            level.spigotConfig.simulationDistance = clampedSimulation;
            level.getChunkSource().setSimulationDistance(clampedSimulation);
        }
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