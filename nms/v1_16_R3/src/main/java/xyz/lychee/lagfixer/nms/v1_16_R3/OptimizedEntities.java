package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;

public interface OptimizedEntities {

    class OBoat extends EntityBoat implements OptimizedEntities {
        OBoat(EntityBoat b) {
            super(b.getWorld(), b.locX(), b.locY(), b.locZ());
        }

        @Override
        public boolean j(Entity entity) {
            return false;
        }

        @Override
        public boolean aZ() {
            return false;
        }

        @Override
        public boolean isCollidable() {
            return true;
        }
    }

    class OMinecart extends EntityMinecartRideable implements OptimizedEntities {
        OMinecart(EntityMinecartRideable m) {
            super(m.getWorld(), m.locX(), m.locY(), m.locZ());
        }

        @Override
        public boolean j(Entity entity) {
            return false;
        }

        @Override
        public boolean aZ() {
            return false;
        }

        @Override
        public boolean isCollidable() {
            return false;
        }
    }

    class OMinecartChest extends EntityMinecartChest implements OptimizedEntities {
        OMinecartChest(EntityMinecartChest mc) {
            super(mc.getWorld(), mc.locX(), mc.locY(), mc.locZ());
        }

        @Override
        public boolean j(Entity entity) {
            return false;
        }

        @Override
        public boolean aZ() {
            return false;
        }

        @Override
        public boolean isCollidable() {
            return false;
        }
    }

    class OMinecartHopper extends EntityMinecartHopper implements OptimizedEntities {
        OMinecartHopper(EntityMinecartHopper mh) {
            super(mh.getWorld(), mh.locX(), mh.locY(), mh.locZ());
        }

        @Override
        public boolean j(Entity entity) {
            return false;
        }

        @Override
        public boolean aZ() {
            return false;
        }

        @Override
        public boolean isCollidable() {
            return false;
        }
    }

    class OMinecartFurnace extends EntityMinecartFurnace implements OptimizedEntities {
        OMinecartFurnace(EntityMinecartFurnace mf) {
            super(mf.getWorld(), mf.locX(), mf.locY(), mf.locZ());
        }

        @Override
        public boolean j(Entity entity) {
            return false;
        }

        @Override
        public boolean aZ() {
            return false;
        }

        @Override
        public boolean isCollidable() {
            return false;
        }
    }

    class OMinecartTNT extends EntityMinecartTNT implements OptimizedEntities {
        OMinecartTNT(EntityMinecartTNT mt) {
            super(mt.getWorld(), mt.locX(), mt.locY(), mt.locZ());
        }

        @Override
        public boolean j(Entity entity) {
            return false;
        }

        @Override
        public boolean aZ() {
            return false;
        }

        @Override
        public boolean isCollidable() {
            return false;
        }
    }
}