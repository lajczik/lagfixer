package xyz.lychee.lagfixer.nms.v1_20_R4;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftChunk;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class SupportNms extends AbstractSupportNms {
    public SupportNms(Plugin plugin) {
        super(plugin);
    }

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
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            return is;
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
        return 1_000_000_000.0 / ((CraftServer) Bukkit.getServer()).getServer().getAverageTickTimeNanos();
    }
}