package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMinecart;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMinecartChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.IdentityHashMap;
import java.util.function.Function;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS implements Listener {
    private static final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> VEHICLES = new IdentityHashMap<>(7);

    static {
        VEHICLES.put(EntityBoat.class, e -> new OptimizedEntities.OBoat((EntityBoat) e));
        VEHICLES.put(EntityMinecartChest.class, e -> new OptimizedEntities.OMinecartChest((EntityMinecartChest) e));
        VEHICLES.put(EntityMinecartHopper.class, e -> new OptimizedEntities.OMinecartHopper((EntityMinecartHopper) e));
        VEHICLES.put(EntityMinecartFurnace.class, e -> new OptimizedEntities.OMinecartFurnace((EntityMinecartFurnace) e));
        VEHICLES.put(EntityMinecartTNT.class, e -> new OptimizedEntities.OMinecartTNT((EntityMinecartTNT) e));
        VEHICLES.put(EntityMinecartRideable.class, e -> new OptimizedEntities.OMinecart((EntityMinecartRideable) e));
    }

    public VehicleMotionReducer(VehicleMotionReducerModule module) {
        super(module);
    }

    @Override
    public boolean optimizeVehicle(org.bukkit.entity.Entity vehicle) {
        if (vehicle instanceof CraftBoat) {
            if (getModule().isBoat()) {
                return processEntity(((CraftBoat) vehicle).getHandle());
            }
            return false;
        } else if (!(vehicle instanceof CraftMinecart) || !getModule().isMinecart()) {
            return false;
        } else if ((vehicle instanceof CraftMinecartChest) && getModule().isMinecart_remove_chest()) {
            EntityMinecartContainer mc = ((CraftMinecartChest) vehicle).getHandle();
            mc.clear();
            mc.die();
            return true;
        }
        return processEntity(((CraftMinecart) vehicle).getHandle());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(ChunkLoadEvent e) {
        org.bukkit.entity.Entity[] entities = e.getChunk().getEntities();
        for (org.bukkit.entity.Entity entity : entities) {
            this.optimizeVehicle(entity);
        }
    }

    private boolean processEntity(Entity original) {
        Function<Entity, Entity> function;
        if (original instanceof OptimizedEntities || (function = VEHICLES.get(original.getClass())) == null) {
            return false;
        }

        Entity newVehicle = function.apply(original);
        newVehicle.setSilent(true);
        this.copyLocation(original, newVehicle);
        original.getWorld().addEntity(newVehicle);
        this.copyItems(original, newVehicle);
        original.die();
        return true;
    }

    private void copyItems(Entity from, Entity to) {
        if (from instanceof EntityMinecartContainer && to instanceof EntityMinecartContainer) {
            for (int i = 0; i < ((EntityMinecartContainer) from).getSize(); i++) {
                ItemStack is = ((EntityMinecartContainer) from).getItem(i);
                if (!is.isEmpty()) {
                    ((EntityMinecartContainer) to).setItem(i, is.cloneItemStack());
                }
            }
            ((EntityMinecartContainer) from).clear();
        }
    }

    private void copyLocation(Entity from, Entity to) {
        to.setPosition(from.lastX, from.lastY, from.lastZ);
        to.setMot(Vec3D.ORIGIN);
        to.lastX = from.lastX;
        to.lastY = from.lastY;
        to.lastZ = from.lastZ;

    }
}