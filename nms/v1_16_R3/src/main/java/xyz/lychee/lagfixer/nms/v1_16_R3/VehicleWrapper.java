package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

public interface VehicleWrapper {

    class OBoat extends EntityBoat implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OBoat(VehicleMotionReducerModule module, EntityBoat b) {
            super(b.getWorld(), b.locX(), b.locY(), b.locZ());

            this.module = module;
        }

        @Override
        public boolean j(Entity entity) {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean aZ() {
            return this.module.isBoat_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.module.isBoat_pushable();
        }
    }

    class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecart(VehicleMotionReducerModule module, EntityMinecartRideable m) {
            super(m.getWorld(), m.locX(), m.locY(), m.locZ());

            this.module = module;
        }

        @Override
        public boolean j(Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartChest(VehicleMotionReducerModule module, EntityMinecartChest mc) {
            super(mc.getWorld(), mc.locX(), mc.locY(), mc.locZ());

            this.module = module;
        }

        @Override
        public boolean j(Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartHopper(VehicleMotionReducerModule module, EntityMinecartHopper mh) {
            super(mh.getWorld(), mh.locX(), mh.locY(), mh.locZ());

            this.module = module;
        }

        @Override
        public boolean j(Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartFurnace(VehicleMotionReducerModule module, EntityMinecartFurnace mf) {
            super(mf.getWorld(), mf.locX(), mf.locY(), mf.locZ());

            this.module = module;
        }

        @Override
        public boolean j(Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.module.isMinecart_pushable();
        }
    }

    class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
        private final VehicleMotionReducerModule module;

        OMinecartTNT(VehicleMotionReducerModule module, EntityMinecartTNT mt) {
            super(mt.getWorld(), mt.locX(), mt.locY(), mt.locZ());

            this.module = module;
        }

        @Override
        public boolean j(Entity entity) {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.module.isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.module.isMinecart_pushable();
        }
    }
}