package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.EntityCreature;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftCreature;
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
        int clampedView = MathHelper.clamp(view, 2, 32);

        WorldServer level = ((CraftWorld) world).getHandle();
        if (level.spigotConfig.viewDistance != clampedView) {
            level.spigotConfig.viewDistance = clampedView;
            level.getChunkProvider().setViewDistance(clampedView);
        }
    }

    @Override
    public void setSimulationDistance(World world, int simulation) {
        //Not supported
    }

    @Override
    public void setEntityAi(Entity ent, boolean bl) {
        if (ent instanceof CraftCreature) {
            EntityCreature mob = ((CraftCreature) ent).getHandle();
            if (mob.isNoAI() != bl) return;

            mob.setNoAI(!bl);
            mob.setAggressive(!bl);
            mob.setSilent(!bl);
            mob.collides = !bl;
        }
    }
}