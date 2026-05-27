package xyz.lychee.lagfixer.nms.v1_20_R2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS implements Listener {
    private static final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> VEHICLES = new IdentityHashMap<>(8);

    static {
        VEHICLES.put(Boat.class, e -> new OptimizedEntities.OBoat((Boat) e));
        VEHICLES.put(ChestBoat.class, e -> new OptimizedEntities.OChestBoat((ChestBoat) e));

        VEHICLES.put(MinecartChest.class, e -> new OptimizedEntities.OMinecartChest((MinecartChest) e));
        VEHICLES.put(MinecartHopper.class, e -> new OptimizedEntities.OMinecartHopper((MinecartHopper) e));
        VEHICLES.put(MinecartFurnace.class, e -> new OptimizedEntities.OMinecartFurnace((MinecartFurnace) e));
        VEHICLES.put(MinecartSpawner.class, e -> new OptimizedEntities.OMinecartSpawner((MinecartSpawner) e));
        VEHICLES.put(MinecartTNT.class, e -> new OptimizedEntities.OMinecartTNT((MinecartTNT) e));
        VEHICLES.put(Minecart.class, e -> new OptimizedEntities.OMinecart((Minecart) e));
    }

    public VehicleMotionReducer(VehicleMotionReducerModule module) {
        super(module);
    }

    @Override
    public boolean optimizeVehicle(org.bukkit.entity.Entity vehicle) {
        if (vehicle instanceof CraftBoat boat) {
            if (!this.getModule().isBoat()) return false;

            return this.processEntity(boat.getHandle());
        } else if (vehicle instanceof CraftMinecart minecart) {
            if (!this.getModule().isMinecart()) return false;

            return this.processEntity(minecart.getHandle());
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpawn(EntitiesLoadEvent e) {
        List<org.bukkit.entity.Entity> entities = e.getEntities();
        for (org.bukkit.entity.Entity entity : entities) {
            this.optimizeVehicle(entity);
        }
    }

    private boolean processEntity(Entity original) {
        if (original instanceof OptimizedEntities) return false;

        Function<Entity, ? extends Entity> factory = VEHICLES.get(original.getClass());
        if (factory == null) return false;

        Entity newVehicle = factory.apply(original);
        this.copyLocation(original, newVehicle);
        original.level().addFreshEntity(newVehicle);
        this.copyItems(original, newVehicle);
        original.remove(Entity.RemovalReason.DISCARDED);
        return true;
    }

    private void copyItems(Entity from, Entity to) {
        if (from instanceof ContainerEntity fromContainer && to instanceof ContainerEntity toContainer) {
            for (int i = 0; i < fromContainer.getContainerSize(); i++) {
                ItemStack is = fromContainer.getItem(i);
                if (!is.isEmpty()) {
                    toContainer.setItem(i, is.copyAndClear());
                }
            }
            fromContainer.clearContent();
        }
    }

    private void copyLocation(Entity from, Entity to) {
        to.setPos(from.xo, from.yo, from.zo);
        to.xo = from.xo;
        to.yo = from.yo;
        to.zo = from.zo;

        float yaw = Location.normalizeYaw(from.yRotO);
        to.setYRot(yaw);
        to.yRotO = yaw;
        to.setYHeadRot(yaw);
    }
}