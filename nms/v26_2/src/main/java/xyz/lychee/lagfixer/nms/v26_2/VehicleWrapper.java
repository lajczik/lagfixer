package xyz.lychee.lagfixer.nms.v26_2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.*;
import net.minecraft.world.entity.vehicle.minecart.*;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.Optional;

public interface VehicleWrapper {
    class OBoat extends Boat implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        @SuppressWarnings("unchecked")
        OBoat(VehicleMotionReducerModule module, Boat b) {
            super((EntityType<Boat>) b.getType(), b.level(), () -> b.getPickResult().getItem());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isBoat_pushable();
        }
    }

    class OChestBoat extends ChestBoat implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        @SuppressWarnings("unchecked")
        OChestBoat(VehicleMotionReducerModule module, ChestBoat cb) {
            super((EntityType<ChestBoat>) cb.getType(), cb.level(), () -> cb.getPickResult().getItem());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isBoat_pushable();
        }
    }

    class ORaft extends Raft implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        ORaft(VehicleMotionReducerModule module, Raft r) {
            super(EntityTypes.BAMBOO_RAFT, r.level(), () -> r.getPickResult().getItem());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isBoat_pushable();
        }
    }

    class OChestRaft extends ChestRaft implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OChestRaft(VehicleMotionReducerModule module, ChestRaft cr) {
            super(EntityTypes.BAMBOO_CHEST_RAFT, cr.level(), () -> cr.getPickResult().getItem());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
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
            super(EntityTypes.MINECART, m.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
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
            super(EntityTypes.CHEST_MINECART, mc.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
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
            super(EntityTypes.HOPPER_MINECART, mh.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
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
            super(EntityTypes.FURNACE_MINECART, mf.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
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
            super(EntityTypes.SPAWNER_MINECART, other.level());

            this.module = module;
            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> sd.entityToSpawn().read("id", EntityType.CODEC))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.getRandom(), this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
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
            super(EntityTypes.TNT_MINECART, mt.level());

            this.module = module;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.module.isMinecart_pushable();
        }
    }
}