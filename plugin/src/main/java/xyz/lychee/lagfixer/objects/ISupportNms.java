package xyz.lychee.lagfixer.objects;

<<<<<<< HEAD
public interface ISupportNms {
    TickReport getTickReport();

    record TickReport(double mspt, double tps) {}
=======
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

    void setViewDistance(World world, int view);

    void setSimulationDistance(World world, int simulation);

    void setEntityAi(Entity ent, boolean bl);
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}