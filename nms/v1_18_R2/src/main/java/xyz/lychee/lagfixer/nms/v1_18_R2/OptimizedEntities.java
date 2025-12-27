package xyz.lychee.lagfixer.nms.v1_18_R2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.*;

import java.util.Optional;

public interface OptimizedEntities {
    class OBoat extends Boat implements OptimizedEntities {
        OBoat(Boat b) {
            super((EntityType<? extends Boat>) b.getType(), b.level);
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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
            super(m.getType(), m.level);
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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
            super((EntityType<? extends MinecartChest>) mc.getType(), mc.level);
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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
            super((EntityType<? extends MinecartHopper>) mh.getType(), mh.level);
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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
            super((EntityType<? extends MinecartFurnace>) mf.getType(), mf.level);
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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
            super((EntityType<? extends MinecartSpawner>) other.getType(), other.level);

            Optional.ofNullable(other.getSpawner().nextSpawnData)
                    .flatMap(sd -> EntityType.by(sd.getEntityToSpawn()))
                    .ifPresent(type ->
                            this.getSpawner().setEntityId(type)
                    );
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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
            super((EntityType<? extends MinecartTNT>) mt.getType(), mt.level);
        }

        @Override
        public boolean canCollideWith(Entity entity) {
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