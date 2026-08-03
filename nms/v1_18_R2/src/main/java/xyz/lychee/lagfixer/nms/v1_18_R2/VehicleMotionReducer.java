package xyz.lychee.lagfixer.nms.v1_18_R2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.EntitiesLoadEvent;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.IdentityHashMap;
import java.util.function.Function;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS {
    private final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> vehicles = new IdentityHashMap<>(10);

    public VehicleMotionReducer(VehicleMotionReducerModule module) {
        super(module);

        vehicles.put(Boat.class, e -> new VehicleWrapper.OBoat(module, (Boat) e));

        vehicles.put(MinecartChest.class, e -> new VehicleWrapper.OMinecartChest(module, (MinecartChest) e));
        vehicles.put(MinecartHopper.class, e -> new VehicleWrapper.OMinecartHopper(module, (MinecartHopper) e));
        vehicles.put(MinecartFurnace.class, e -> new VehicleWrapper.OMinecartFurnace(module, (MinecartFurnace) e));
        vehicles.put(MinecartSpawner.class, e -> new VehicleWrapper.OMinecartSpawner(module, (MinecartSpawner) e));
        vehicles.put(MinecartTNT.class, e -> new VehicleWrapper.OMinecartTNT(module, (MinecartTNT) e));
        vehicles.put(Minecart.class, e -> new VehicleWrapper.OMinecart(module, (Minecart) e));
    }

    @Override
    public boolean optimize(org.bukkit.entity.Entity vehicle) {
        if (vehicle instanceof CraftBoat boat) {
            if (!this.getModule().isBoat()) return false;

            return this.processEntity(boat.getHandle(), this.getModule().isBoat_silent());
        } else if (vehicle instanceof CraftMinecart minecart) {
            if (!this.getModule().isMinecart()) return false;

            return this.processEntity(minecart.getHandle(), this.getModule().isMinecart_silent());
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(EntitiesLoadEvent e) {
        if (!this.getModule().canContinue(e.getWorld())) return;

        e.getEntities().forEach(this::optimize);
    }

    private boolean processEntity(Entity original, boolean silent) {
        if (original instanceof VehicleWrapper) return false;

        Function<Entity, Entity> factory = this.vehicles.get(original.getClass());
        if (factory == null) return false;

        Entity newVehicle = factory.apply(original);
        newVehicle.setSilent(silent);

        this.copyLocation(original, newVehicle);
        this.copyItems(original, newVehicle);

        original.level.addFreshEntity(newVehicle);
        original.remove(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        return true;
    }

    private void copyItems(Entity from, Entity to) {
        if (from instanceof AbstractMinecartContainer fromContainer && to instanceof AbstractMinecartContainer toContainer) {
            for (int i = 0; i < fromContainer.getContainerSize(); i++) {
                ItemStack is = fromContainer.getItem(i);
                if (!is.isEmpty()) {
                    toContainer.setItem(i, is.copy());
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