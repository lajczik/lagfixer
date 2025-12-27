package xyz.lychee.lagfixer.nms.v1_21_R6;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.*;

import java.util.Optional;

public interface OptimizedEntities {

    class OBoat extends Boat implements OptimizedEntities {
        OBoat(Boat b) {
            super((EntityType<Boat>) b.getType(), b.level(), () -> b.getPickResult().getItem());

        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OChestBoat extends ChestBoat implements OptimizedEntities {
        OChestBoat(ChestBoat cb) {
            super((EntityType<ChestBoat>) cb.getType(), cb.level(), () -> cb.getPickResult().getItem());
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class ORaft extends Raft implements OptimizedEntities {
        ORaft(Raft r) {
            super(EntityType.BAMBOO_RAFT, r.level(), () -> r.getPickResult().getItem());

        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OChestRaft extends ChestRaft implements OptimizedEntities {
        OChestRaft(ChestRaft cr) {
            super(EntityType.BAMBOO_CHEST_RAFT, cr.level(), () -> cr.getPickResult().getItem());

        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OMinecart extends Minecart implements OptimizedEntities {
        OMinecart(Minecart m) {
            super(EntityType.MINECART, m.level());
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OMinecartChest extends MinecartChest implements OptimizedEntities {
        OMinecartChest(MinecartChest mc) {
            super(EntityType.CHEST_MINECART, mc.level());
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OMinecartHopper extends MinecartHopper implements OptimizedEntities {
        OMinecartHopper(MinecartHopper mh) {
            super(EntityType.HOPPER_MINECART, mh.level());
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OMinecartFurnace extends MinecartFurnace implements OptimizedEntities {
        OMinecartFurnace(MinecartFurnace mf) {
            super(EntityType.FURNACE_MINECART, mf.level());
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OMinecartSpawner extends MinecartSpawner implements OptimizedEntities {
        OMinecartSpawner(MinecartSpawner other) {
            super(EntityType.SPAWNER_MINECART, other.level());

            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> sd.entityToSpawn().read("id", EntityType.CODEC))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type, other.level(), other.getRandom(), this.blockPosition())
                    );
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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

    class OMinecartTNT extends MinecartTNT implements OptimizedEntities {
        OMinecartTNT(MinecartTNT mt) {
            super(EntityType.TNT_MINECART, mt.level());
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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