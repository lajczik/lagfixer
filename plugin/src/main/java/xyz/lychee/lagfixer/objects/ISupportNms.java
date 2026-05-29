package xyz.lychee.lagfixer.objects;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ISupportNms {
    double getTps();

    ItemStack createSkull(String base64);

    int getTileEntitiesCount(Chunk chunk);

    int getPlayerPing(Player player);

    boolean isSupportSimulation();

    void setViewDistance(World world, int view);

    void setSimulationDistance(World world, int simulation);

    void setEntityAi(Entity ent, boolean bl);
}