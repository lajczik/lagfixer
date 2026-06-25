package xyz.lychee.lagfixer.nms.v26_2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.boat.*;
import net.minecraft.world.entity.vehicle.minecart.*;
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
            super(EntityTypes.BAMBOO_RAFT, r.level(), () -> r.getPickResult().getItem());
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
            super(EntityTypes.BAMBOO_CHEST_RAFT, cr.level(), () -> cr.getPickResult().getItem());
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
            super(EntityTypes.MINECART, m.level());
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
            super(EntityTypes.CHEST_MINECART, mc.level());
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
            super(EntityTypes.HOPPER_MINECART, mh.level());
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
            super(EntityTypes.FURNACE_MINECART, mf.level());
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
            super(EntityTypes.SPAWNER_MINECART, other.level());

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
            super(EntityTypes.TNT_MINECART, mt.level());
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