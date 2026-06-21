package xyz.lychee.lagfixer.objects;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

@Getter
@Setter
public class ReflectionSupportNms implements ISupportNms {
    private Method getServerMethod;
    private Field recentTpsField;
    private Method setProfileMethod;
    private Field profileField;
    private Method getHandleMethod;
    private Field pingField;

    public ReflectionSupportNms() {
        try {
            Object craftServer = Bukkit.getServer();
            getServerMethod = craftServer.getClass().getMethod("getServer");
            Object minecraftServer = getServerMethod.invoke(craftServer);
            recentTpsField = minecraftServer.getClass().getField("recentTps");
        } catch (Exception ignored) {}

        ItemMeta tempMeta = Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
        if (tempMeta != null) {
            try {
                setProfileMethod = tempMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                setProfileMethod.setAccessible(true);
            } catch (NoSuchMethodException ignored) {}

            try {
                profileField = tempMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
            } catch (NoSuchFieldException ignored) {}
        }
    }

    @Override
    public double getTps() {
        if (getServerMethod == null || recentTpsField == null) return -1;

        try {
            Object craftServer = Bukkit.getServer();
            Object minecraftServer = getServerMethod.invoke(craftServer);
            if (minecraftServer == null) return -1;

            double[] tps = (double[]) recentTpsField.get(minecraftServer);
            int index = Math.min(1, tps.length - 1);
            return Math.clamp(tps[index], 0.0, 20.0);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public ItemStack createSkull(String base64) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = is.getItemMeta();
        if (meta == null) return is;

        try {
            UUID uuid = UUID.randomUUID();
            GameProfile profile = new GameProfile(uuid, uuid.toString().substring(0, 8));
            profile.getProperties().put("textures", new Property("textures", base64));

            if (setProfileMethod != null) {
                try {
                    setProfileMethod.invoke(meta, profile);
                    is.setItemMeta(meta);
                    return is;
                } catch (Throwable ignored) {}
            }

            if (profileField != null) {
                try {
                    profileField.set(meta, profile);
                    is.setItemMeta(meta);
                    return is;
                } catch (Throwable ignored) {}
            }

            return is;
        } catch (Throwable ignored) {
            return is;
        }
    }

    @Override
    public int getTileEntitiesCount(Chunk chunk) {
        return 0;
    }

    @Override
    public int getPlayerPing(Player player) {
        if (getHandleMethod == null || pingField == null) {
            try {
                getHandleMethod = player.getClass().getMethod("getHandle");
                Object entityPlayer = getHandleMethod.invoke(player);
                pingField = entityPlayer.getClass().getField("ping");
                pingField.setAccessible(true);
            } catch (Exception e) {
                return -1;
            }
        }

        try {
            Object handle = getHandleMethod.invoke(player);
            return pingField.getInt(handle);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void setViewDistance(World world, int view) {}

    @Override
    public void setSimulationDistance(World world, int simulation) {}

    @Override
    public void setEntityAi(Entity ent, boolean bl) {}
}