package xyz.lychee.lagfixer.nms.v1_20_R4;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.*;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public interface OptimizedEntities {
    class OBoat extends Boat implements OptimizedEntities {
        OBoat(Boat b) {
            super(EntityType.BOAT, b.level());
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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

    class OChestBoat extends ChestBoat implements OptimizedEntities {
        OChestBoat(ChestBoat cb) {
            super((EntityType<? extends ChestBoat>) cb.getType(), cb.level());
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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

    class OMinecart extends Minecart implements OptimizedEntities {
        OMinecart(Minecart m) {
            super(m.getType(), m.level());
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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

    class OMinecartChest extends MinecartChest implements OptimizedEntities {
        OMinecartChest(MinecartChest mc) {
            super(EntityType.CHEST_MINECART, mc.level());
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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

    class OMinecartHopper extends MinecartHopper implements OptimizedEntities {
        OMinecartHopper(MinecartHopper mh) {
            super(EntityType.HOPPER_MINECART, mh.level());
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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

    class OMinecartFurnace extends MinecartFurnace implements OptimizedEntities {
        OMinecartFurnace(MinecartFurnace mf) {
            super(EntityType.FURNACE_MINECART, mf.level());
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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

    class OMinecartSpawner extends MinecartSpawner implements OptimizedEntities {
        OMinecartSpawner(MinecartSpawner other) {
            super(EntityType.SPAWNER_MINECART, other.level());

            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> EntityType.by(sd.getEntityToSpawn()))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.random, this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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

    class OMinecartTNT extends MinecartTNT implements OptimizedEntities {
        OMinecartTNT(MinecartTNT mt) {
            super(EntityType.TNT_MINECART, mt.level());
        }

        @Override
        public boolean canCollideWith(@NonNull Entity entity) {
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