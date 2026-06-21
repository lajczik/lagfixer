package xyz.lychee.lagfixer.nms.v26_1;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
<<<<<<<< HEAD:nms/v26_1/src/main/java/xyz/lychee/lagfixer/nms/v26_1/VehicleWrapper.java
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.*;
========
import net.minecraft.world.entity.vehicle.*;
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_21_R6/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R6/VehicleWrapper.java
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface VehicleWrapper {
    class OBoat extends Boat implements VehicleWrapper {
        @SuppressWarnings("unchecked")
        OBoat(Boat b) {
            super((EntityType<Boat>) b.getType(), b.level(), () -> b.getPickResult().getItem());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return true;
        }
    }

    class OChestBoat extends ChestBoat implements VehicleWrapper {
        @SuppressWarnings("unchecked")
        OChestBoat(ChestBoat cb) {
            super((EntityType<ChestBoat>) cb.getType(), cb.level(), () -> cb.getPickResult().getItem());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return true;
        }
    }

    class ORaft extends Raft implements VehicleWrapper {
        ORaft(Raft r) {
            super(EntityType.BAMBOO_RAFT, r.level(), () -> r.getPickResult().getItem());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OChestRaft extends ChestRaft implements VehicleWrapper {
        OChestRaft(ChestRaft cr) {
            super(EntityType.BAMBOO_CHEST_RAFT, cr.level(), () -> cr.getPickResult().getItem());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecart extends Minecart implements VehicleWrapper {
        OMinecart(Minecart m) {
            super(EntityType.MINECART, m.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartChest extends MinecartChest implements VehicleWrapper {
        OMinecartChest(MinecartChest mc) {
            super(EntityType.CHEST_MINECART, mc.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartHopper extends MinecartHopper implements VehicleWrapper {
        OMinecartHopper(MinecartHopper mh) {
            super(EntityType.HOPPER_MINECART, mh.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartFurnace extends MinecartFurnace implements VehicleWrapper {
        OMinecartFurnace(MinecartFurnace mf) {
            super(EntityType.FURNACE_MINECART, mf.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartSpawner extends MinecartSpawner implements VehicleWrapper {
        OMinecartSpawner(MinecartSpawner other) {
            super(EntityType.SPAWNER_MINECART, other.level());

            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> sd.entityToSpawn().read("id", EntityType.CODEC))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.getRandom(), this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartTNT extends MinecartTNT implements VehicleWrapper {
        OMinecartTNT(MinecartTNT mt) {
            super(EntityType.TNT_MINECART, mt.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }
}