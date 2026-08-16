package xyz.lychee.lagfixer.nms.v1_21_R7;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.*;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

import java.util.Optional;

public interface VehicleWrapper {
    class OBoat extends Boat implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        @SuppressWarnings("unchecked")
        OBoat(VehicleMotionReducer provider, Boat b) {
            super((EntityType<Boat>) b.getType(), b.level(), () -> b.getPickResult().getItem());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isBoat_pushable();
        }
    }

    class OChestBoat extends ChestBoat implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        @SuppressWarnings("unchecked")
        OChestBoat(VehicleMotionReducer provider, ChestBoat cb) {
            super((EntityType<ChestBoat>) cb.getType(), cb.level(), () -> cb.getPickResult().getItem());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isBoat_pushable();
        }
    }

    class ORaft extends Raft implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        ORaft(VehicleMotionReducer provider, Raft r) {
            super(EntityType.BAMBOO_RAFT, r.level(), () -> r.getPickResult().getItem());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isBoat_pushable();
        }
    }

    class OChestRaft extends ChestRaft implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OChestRaft(VehicleMotionReducer provider, ChestRaft cr) {
            super(EntityType.BAMBOO_CHEST_RAFT, cr.level(), () -> cr.getPickResult().getItem());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isBoat_pushable();
        }
    }

    class OMinecart extends Minecart implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecart(VehicleMotionReducer provider, Minecart m) {
            super(EntityType.MINECART, m.level());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartChest extends MinecartChest implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartChest(VehicleMotionReducer provider, MinecartChest mc) {
            super(EntityType.CHEST_MINECART, mc.level());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartHopper extends MinecartHopper implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartHopper(VehicleMotionReducer provider, MinecartHopper mh) {
            super(EntityType.HOPPER_MINECART, mh.level());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartFurnace extends MinecartFurnace implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartFurnace(VehicleMotionReducer provider, MinecartFurnace mf) {
            super(EntityType.FURNACE_MINECART, mf.level());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartSpawner extends MinecartSpawner implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartSpawner(VehicleMotionReducer provider, MinecartSpawner other) {
            super(EntityType.SPAWNER_MINECART, other.level());

            this.provider = provider;
            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> sd.entityToSpawn().read("id", EntityType.CODEC))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.getRandom(), this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartTNT extends MinecartTNT implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartTNT(VehicleMotionReducer provider, MinecartTNT mt) {
            super(EntityType.TNT_MINECART, mt.level());

            this.provider = provider;
        }

        @Override
        public boolean canCollideWith(@NotNull Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean canBeCollidedWith(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isPushable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }
}