package xyz.lychee.lagfixer.nms.v1_16_R3;

import net.minecraft.server.v1_16_R3.*;
import xyz.lychee.lagfixer.modules.VehicleMotionReducerModule;

public interface VehicleWrapper {

    class OBoat extends EntityBoat implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OBoat(VehicleMotionReducer provider, EntityBoat b) {
            super(b.getWorld(), b.locX(), b.locY(), b.locZ());

            this.provider = provider;
        }

        @Override
        public boolean j(Entity entity) {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean aZ() {
            return this.provider.getModule().isBoat_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.provider.getModule().isBoat_pushable();
        }
    }

    class OMinecart extends EntityMinecartRideable implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecart(VehicleMotionReducer provider, EntityMinecartRideable m) {
            super(m.getWorld(), m.locX(), m.locY(), m.locZ());

            this.provider = provider;
        }

        @Override
        public boolean j(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartChest extends EntityMinecartChest implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartChest(VehicleMotionReducer provider, EntityMinecartChest mc) {
            super(mc.getWorld(), mc.locX(), mc.locY(), mc.locZ());

            this.provider = provider;
        }

        @Override
        public boolean j(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartHopper extends EntityMinecartHopper implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartHopper(VehicleMotionReducer provider, EntityMinecartHopper mh) {
            super(mh.getWorld(), mh.locX(), mh.locY(), mh.locZ());

            this.provider = provider;
        }

        @Override
        public boolean j(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartFurnace extends EntityMinecartFurnace implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartFurnace(VehicleMotionReducer provider, EntityMinecartFurnace mf) {
            super(mf.getWorld(), mf.locX(), mf.locY(), mf.locZ());

            this.provider = provider;
        }

        @Override
        public boolean j(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }

    class OMinecartTNT extends EntityMinecartTNT implements VehicleWrapper {
        private final VehicleMotionReducer provider;

        OMinecartTNT(VehicleMotionReducer provider, EntityMinecartTNT mt) {
            super(mt.getWorld(), mt.locX(), mt.locY(), mt.locZ());

            this.provider = provider;
        }

        @Override
        public boolean j(Entity entity) {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean aZ() {
            return this.provider.getModule().isMinecart_collides();
        }

        @Override
        public boolean isCollidable() {
            return this.provider.getModule().isMinecart_pushable();
        }
    }
}