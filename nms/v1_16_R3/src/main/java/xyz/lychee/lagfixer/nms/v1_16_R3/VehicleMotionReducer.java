package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftBoat;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.IdentityHashMap;
import java.util.function.Function;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS {
    private final IdentityHashMap<Class<? extends Entity>, Function<Entity, Entity>> vehicles = new IdentityHashMap<>(10);

    public VehicleMotionReducer(VehicleMotionReducerModule module) {
        super(module);

        vehicles.put(EntityBoat.class, e -> new VehicleWrapper.OBoat(module, (EntityBoat) e));

        vehicles.put(EntityMinecartChest.class, e -> new VehicleWrapper.OMinecartChest(module, (EntityMinecartChest) e));
        vehicles.put(EntityMinecartHopper.class, e -> new VehicleWrapper.OMinecartHopper(module, (EntityMinecartHopper) e));
        vehicles.put(EntityMinecartFurnace.class, e -> new VehicleWrapper.OMinecartFurnace(module, (EntityMinecartFurnace) e));
        vehicles.put(EntityMinecartTNT.class, e -> new VehicleWrapper.OMinecartTNT(module, (EntityMinecartTNT) e));
        vehicles.put(EntityMinecartRideable.class, e -> new VehicleWrapper.OMinecart(module, (EntityMinecartRideable) e));
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
    public void onLoad(ChunkLoadEvent e) {
        if (!this.getModule().canContinue(e.getWorld())) return;

        for (org.bukkit.entity.Entity entity : e.getChunk().getEntities()) {
            this.optimize(entity);
        }
    }

    private boolean processEntity(Entity original, boolean silent) {
        if (original instanceof VehicleWrapper) return false;

        Function<Entity, Entity> factory = this.vehicles.get(original.getClass());
        if (factory == null) return false;

        Entity newVehicle = factory.apply(original);
        newVehicle.setSilent(silent);

        this.copyLocation(original, newVehicle);
        this.copyItems(original, newVehicle);

        original.getWorld().addEntity(newVehicle);
        original.die();
        return true;
    }

    private void copyItems(Entity from, Entity to) {
        if (from instanceof EntityMinecartContainer fromContainer && to instanceof EntityMinecartContainer toContainer) {
            for (int i = 0; i < fromContainer.getSize(); i++) {
                ItemStack is = fromContainer.getItem(i);
                if (!is.isEmpty()) {
                    toContainer.setItem(i, is.cloneItemStack());
                }
            }
            fromContainer.clear();
        }
    }

    private void copyLocation(Entity from, Entity to) {
        to.setPosition(from.lastX, from.lastY, from.lastZ);
        to.lastX = from.lastX;
        to.lastY = from.lastY;
        to.lastZ = from.lastZ;

        float yaw = Location.normalizeYaw(from.yaw);
        to.yaw = yaw;
        to.setHeadRotation(yaw);
    }
}