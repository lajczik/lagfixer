package xyz.lychee.lagfixer.nms.v1_21_R7;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.*;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftBoat;
import org.bukkit.craftbukkit.entity.CraftMinecart;
import org.bukkit.craftbukkit.entity.CraftMinecartChest;
import org.bukkit.entity.Vehicle;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.IdentityHashMap;
import java.util.function.Function;

public class VehicleMotionReducer extends VehicleMotionReducerModule.NMS {
    private static final IdentityHashMap<Class<? extends VehicleEntity>, Function<VehicleEntity, VehicleEntity>> VEHICLES = new IdentityHashMap<>(10);

    static {
        VEHICLES.put(Raft.class, e -> new VehicleWrapper.ORaft((Raft) e));
        VEHICLES.put(ChestRaft.class, e -> new VehicleWrapper.OChestRaft((ChestRaft) e));
        VEHICLES.put(Boat.class, e -> new VehicleWrapper.OBoat((Boat) e));
        VEHICLES.put(ChestBoat.class, e -> new VehicleWrapper.OChestBoat((ChestBoat) e));

        VEHICLES.put(MinecartChest.class, e -> new VehicleWrapper.OMinecartChest((MinecartChest) e));
        VEHICLES.put(MinecartHopper.class, e -> new VehicleWrapper.OMinecartHopper((MinecartHopper) e));
        VEHICLES.put(MinecartFurnace.class, e -> new VehicleWrapper.OMinecartFurnace((MinecartFurnace) e));
        VEHICLES.put(MinecartSpawner.class, e -> new VehicleWrapper.OMinecartSpawner((MinecartSpawner) e));
        VEHICLES.put(MinecartTNT.class, e -> new VehicleWrapper.OMinecartTNT((MinecartTNT) e));
        VEHICLES.put(Minecart.class, e -> new VehicleWrapper.OMinecart((Minecart) e));
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

    private boolean processEntity(VehicleEntity original) {
        if (original instanceof VehicleWrapper) return false;

        Function<VehicleEntity, ? extends VehicleEntity> factory = VEHICLES.get(original.getClass());
        if (factory == null) return false;

        VehicleEntity newVehicle = factory.apply(original);
        newVehicle.setSilent(true);
        copyLocation(original, newVehicle);
        copyItems(original, newVehicle);

        original.removeVehicle();
        original.level().addFreshEntity(newVehicle);
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

    private void copyLocation(VehicleEntity from, VehicleEntity to) {
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