package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

@Getter
public abstract class AbstractSupportNms implements Listener {
    private final Plugin plugin;

    public AbstractSupportNms(Plugin plugin) {
        this.plugin = plugin;
    }

    public abstract double getTps();

    public abstract ItemStack createSkull(String base64);

    public abstract int getTileEntitiesCount(Chunk chunk);

    public abstract int getPlayerPing(Player player);
}

