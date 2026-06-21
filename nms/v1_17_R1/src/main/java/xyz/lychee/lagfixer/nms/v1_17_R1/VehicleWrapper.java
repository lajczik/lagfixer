package xyz.lychee.lagfixer.nms.v1_21_R1;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface VehicleWrapper {
    class OBoat extends Boat implements VehicleWrapper {
        OBoat(Boat b) {
<<<<<<<< HEAD:nms/v1_21_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R1/VehicleWrapper.java
            super(EntityType.BOAT, b.level());
========
            super(EntityType.BOAT, b.level);
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_17_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_17_R1/VehicleWrapper.java
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return true;
        }
    }

<<<<<<<< HEAD:nms/v1_21_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R1/VehicleWrapper.java
    class OChestBoat extends ChestBoat implements VehicleWrapper {
        OChestBoat(ChestBoat cb) {
            super(EntityType.CHEST_BOAT, cb.level());
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return true;
        }
    }

    class OMinecart extends Minecart implements VehicleWrapper {
        OMinecart(Minecart m) {
            super(EntityType.MINECART, m.level());
========
    class OMinecart extends Minecart implements VehicleWrapper {
        OMinecart(Minecart m) {
            super(EntityType.MINECART, m.level);
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_17_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_17_R1/VehicleWrapper.java
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartChest extends MinecartChest implements VehicleWrapper {
        OMinecartChest(MinecartChest mc) {
<<<<<<<< HEAD:nms/v1_21_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R1/VehicleWrapper.java
            super(EntityType.CHEST_MINECART, mc.level());
========
            super(EntityType.CHEST_MINECART, mc.level);
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_17_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_17_R1/VehicleWrapper.java
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartHopper extends MinecartHopper implements VehicleWrapper {
        OMinecartHopper(MinecartHopper mh) {
<<<<<<<< HEAD:nms/v1_21_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R1/VehicleWrapper.java
            super(EntityType.HOPPER_MINECART, mh.level());
========
            super(EntityType.HOPPER_MINECART, mh.level);
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_17_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_17_R1/VehicleWrapper.java
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartFurnace extends MinecartFurnace implements VehicleWrapper {
        OMinecartFurnace(MinecartFurnace mf) {
<<<<<<<< HEAD:nms/v1_21_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R1/VehicleWrapper.java
            super(EntityType.FURNACE_MINECART, mf.level());
========
            super(EntityType.FURNACE_MINECART, mf.level);
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_17_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_17_R1/VehicleWrapper.java
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartSpawner extends MinecartSpawner implements VehicleWrapper {
        OMinecartSpawner(MinecartSpawner other) {
<<<<<<<< HEAD:nms/v1_21_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R1/VehicleWrapper.java
            super(EntityType.SPAWNER_MINECART, other.level());
========
            super(EntityType.SPAWNER_MINECART, other.level);
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_17_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_17_R1/VehicleWrapper.java

            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> EntityType.by(sd.entityToSpawn()))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.getRandom(), this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }

    class OMinecartTNT extends MinecartTNT implements VehicleWrapper {
        OMinecartTNT(MinecartTNT mt) {
<<<<<<<< HEAD:nms/v1_21_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_21_R1/VehicleWrapper.java
            super(EntityType.TNT_MINECART, mt.level());
========
            super(EntityType.TNT_MINECART, mt.level);
>>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90:nms/v1_17_R1/src/main/java/xyz/lychee/lagfixer/nms/v1_17_R1/VehicleWrapper.java
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return false;
        }

        @Override
        public boolean canBeCollidedWith() {
            return false;
        }

        @Override
        public boolean isPushable() {
            return false;
        }
    }
}