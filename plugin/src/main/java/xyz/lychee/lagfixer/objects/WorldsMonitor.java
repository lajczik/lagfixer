package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.List;

@Getter
public class WorldsMonitor extends AbstractMonitor {
    private int entities = 0;
    private int creatures = 0;
    private int items = 0;
    private int projectiles = 0;
    private int vehicles = 0;
    private int tiles = 0;
    private int chunks = 0;

    public WorldsMonitor() {
        super("worlds");
    }

    @Override
    public void run() {
        int entities = 0, creatures = 0, items = 0, projectiles = 0, vehicles = 0, tiles = 0, chunks = 0;

        ISupportNms nms = SupportManager.getInstance().getNms();
        for (World world : Bukkit.getWorlds()) {
            Chunk[] loaded = world.getLoadedChunks();
            List<Entity> list = world.getEntities();
            entities += list.size();
            chunks += loaded.length;

            for (Entity entity : list) {
                if (entity instanceof Mob) {
                    creatures++;
                } else if (entity instanceof Vehicle) {
                    vehicles++;
                } else if (entity instanceof Item) {
                    items++;
                } else if (entity instanceof Projectile) {
                    projectiles++;
                }
            }

            for (Chunk chunk : loaded) {
                tiles += nms.getTileEntitiesCount(chunk);
            }
        }

        this.entities = entities;
        this.creatures = creatures;
        this.items = items;
        this.projectiles = projectiles;
        this.vehicles = vehicles;
        this.tiles = tiles;
        this.chunks = chunks;
    }
}