package xyz.lychee.lagfixer.nms.v1_20_R2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.*;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.Optional;

public interface VehicleWrapper {
    class OBoat extends Boat implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OBoat(VehicleMotionReducerModule module, Boat b) {
            super(EntityType.BOAT, b.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isBoat_pushable();
        }
    }

    class OChestBoat extends ChestBoat implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OChestBoat(VehicleMotionReducerModule module, ChestBoat cb) {
            super(EntityType.CHEST_BOAT, cb.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isBoat_pushable();
        }
    }

    class OMinecart extends Minecart implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecart(VehicleMotionReducerModule module, Minecart m) {
            super(EntityType.MINECART, m.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartChest extends MinecartChest implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartChest(VehicleMotionReducerModule module, MinecartChest mc) {
            super(EntityType.CHEST_MINECART, mc.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartHopper extends MinecartHopper implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartHopper(VehicleMotionReducerModule module, MinecartHopper mh) {
            super(EntityType.HOPPER_MINECART, mh.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartFurnace extends MinecartFurnace implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartFurnace(VehicleMotionReducerModule module, MinecartFurnace mf) {
            super(EntityType.FURNACE_MINECART, mf.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartSpawner extends MinecartSpawner implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartSpawner(VehicleMotionReducerModule module, MinecartSpawner other) {
            super(EntityType.SPAWNER_MINECART, other.level());

            this.module = module;
            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> EntityType.by(sd.entityToSpawn()))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.random, this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartTNT extends MinecartTNT implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartTNT(VehicleMotionReducerModule module, MinecartTNT mt) {
            super(EntityType.TNT_MINECART, mt.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isMinecart_pushable();
        }
    }
}