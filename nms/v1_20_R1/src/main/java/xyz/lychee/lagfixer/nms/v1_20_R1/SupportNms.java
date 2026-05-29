package xyz.lychee.lagfixer.nms.v1_20_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftCreature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lychee.lagfixer.objects.ReflectionSupportNms;

import java.lang.reflect.Method;
import java.util.UUID;

public class SupportNms extends ReflectionSupportNms {
    @Override
    public ItemStack createSkull(String base64) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = is.getItemMeta();
        if (meta == null) {
            return is;
        }

        try {
            UUID uuid = UUID.randomUUID();
            GameProfile gameProfile = new GameProfile(uuid, uuid.toString().substring(0, 16));
            gameProfile.getProperties().put("textures", new Property("textures", base64));

            Method mtd = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(meta, gameProfile);
            is.setItemMeta(meta);
            return is;
        } catch (Throwable ex) {
            return super.createSkull(base64);
        }
    }

    @Override
    public int getTileEntitiesCount(Chunk c) {
        if (c.isLoaded()) {
            return ((CraftChunk) c).getHandle(ChunkStatus.FULL).blockEntities.size();
        }
        return 0;
    }

    @Override
    public int getPlayerPing(Player player) {
        return player.getPing();
    }

    @Override
    public double getTps() {
        return ((CraftServer) Bukkit.getServer()).getServer().recentTps[2];
    }
    
    @Override
    public boolean isSupportSimulation() {
        return true;
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