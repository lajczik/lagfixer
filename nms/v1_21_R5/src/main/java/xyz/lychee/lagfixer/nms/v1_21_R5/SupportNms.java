package xyz.lychee.lagfixer.nms.v1_21_R5;

<<<<<<< HEAD
import com.google.common.util.concurrent.AtomicDouble;
import io.papermc.paper.threadedregions.TickData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.ISupportNms;

import java.util.concurrent.atomic.LongAdder;

public class SupportNms implements ISupportNms {

=======
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lychee.lagfixer.objects.ReflectionSupportNms;

import java.lang.reflect.Method;
import java.util.UUID;

public class SupportNms extends ReflectionSupportNms {
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    @Override
    public TickReport getTickReport() {
        long currTime = System.nanoTime();
        AtomicDouble tpsByRegion = new AtomicDouble();
        AtomicDouble msptByRegion = new AtomicDouble();
        LongAdder regions = new LongAdder();

        for (World world : Bukkit.getWorlds()) {
            ((CraftWorld) world).getHandle().regioniser.computeForAllRegions(region -> {
                TickData.TickReportData report = region.getData().getRegionSchedulingHandle().getTickReport15s(currTime);
                if (report != null) {
                    tpsByRegion.addAndGet(report.tpsData().segmentAll().average());
                    msptByRegion.addAndGet(report.timePerTickData().segmentAll().average() / 1_000_000.0D);
                    regions.increment();
                }
            });
        }
<<<<<<< HEAD

        int regionsInt = regions.intValue();
        if (regionsInt == 0) {
            return new TickReport(0, 20);
=======

        try {
            UUID uuid = UUID.randomUUID();
            GameProfile gameProfile = new GameProfile(uuid, uuid.toString().substring(0, 16));
            gameProfile.getProperties().put("textures", new Property("textures", base64));

            ResolvableProfile resolvableProfile = new ResolvableProfile(gameProfile);

            Method mtd = meta.getClass().getDeclaredMethod("setProfile", ResolvableProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(meta, resolvableProfile);
            is.setItemMeta(meta);
            return is;
        } catch (Throwable ex) {
            return super.createSkull(base64);
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        }

        return new TickReport(msptByRegion.get() / regionsInt, tpsByRegion.get() / regionsInt);
    }

    @Override
    public void setViewDistance(World world, int view) {
        int clampedView = Math.clamp(view, 2, 32);

        ServerLevel level = ((CraftWorld) world).getHandle();
        if (level.spigotConfig.viewDistance != clampedView) {
            level.spigotConfig.viewDistance = clampedView;
            level.getChunkSource().setViewDistance(clampedView);
        }
    }

    @Override
    public void setSimulationDistance(World world, int simulation) {
        ServerLevel level = ((CraftWorld) world).getHandle();

        int clampedSimulation = Math.clamp(simulation, 1, level.spigotConfig.viewDistance);
        if (level.spigotConfig.simulationDistance != clampedSimulation) {
            level.spigotConfig.simulationDistance = clampedSimulation;
            level.getChunkSource().setSimulationDistance(clampedSimulation);
        }
    }

    @Override
    public void setEntityAi(Entity ent, boolean bl) {
        if (ent instanceof CraftCreature creature) {
            PathfinderMob mob = creature.getHandle();
            if (mob.isNoAi() != bl) return;

            mob.setNoAi(!bl);
            mob.setAggressive(!bl);
            mob.setSilent(!bl);
            mob.collides = !bl;
        }
    }
}