package xyz.lychee.lagfixer.objects;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface ISupportNms {
    double getTps();

    ItemStack createSkull(String base64);

    int getTileEntitiesCount(Chunk chunk);

    int getPlayerPing(Player player);

    void setViewDistance(World world, int view);

    void setSimulationDistance(World world, int simulation);

    void hideEntity(Plugin plugin, Player player, Entity entity);

    void showEntity(Plugin plugin, Player player, Entity entity);

    void setEntityAi(Entity ent, boolean bl);
}