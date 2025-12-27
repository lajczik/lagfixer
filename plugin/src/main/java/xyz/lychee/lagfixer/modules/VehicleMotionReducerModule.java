package xyz.lychee.lagfixer.modules;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.PlayerInventory;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

@Getter
public class VehicleMotionReducerModule extends AbstractModule implements Listener {
    private NMS vehicleMotionReducer;
    private boolean forceLoad;
    private boolean minecart_remove_chest;
    private boolean minecart;
    private boolean boat;

    public VehicleMotionReducerModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, AbstractModule.Impact.LOW, "VehicleMotionReducer",
                new String[]{
                        "Optimizes all vehicles such as Boats and Minecarts.",
                        "Removes minecarts with chests spawned in mineshafts.",
                        "Particularly useful when minecarts are frequently used on the server.",
                        "Enhances server performance by optimizing vehicle mechanics and removing unnecessary entities."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJjMjA1MGVjYTBlZmRkMDMxZTY1OGI5OTZjMjM5YmY3ZGEzYWVmODY1NjEyMzY3ZWQ5ZDg5NWFlN2EwZGE5ZiJ9fX0=");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityPlace(EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (!event.isCancelled() && this.canContinue(entity.getWorld()) && entity instanceof Vehicle) {
            Vehicle vehicle = (Vehicle) entity;
            boolean cancel = this.vehicleMotionReducer.optimizeVehicle(vehicle);

            if (cancel) {
                event.setCancelled(true);

                Player player = event.getPlayer();
                if (player != null && player.getGameMode() != GameMode.CREATIVE) {
                    PlayerInventory inventory = player.getInventory();

                    String mainHand = inventory.getItemInMainHand().getType().name().toLowerCase();
                    String offHand = inventory.getItemInOffHand().getType().name().toLowerCase();
                    if (mainHand.contains("boat") || mainHand.contains("minecart") || mainHand.contains("raft")) {
                        inventory.setItemInMainHand(null);
                    } else if (offHand.contains("boat") || offHand.contains("minecart") || offHand.contains("raft")) {
                        inventory.setItemInOffHand(null);
                    }
                }
            }
        }
    }

    public boolean isEnabled(Entity ent) {
        return ent instanceof Minecart && this.minecart || ent instanceof Boat && this.boat;
    }

    @Override
    public void load() {
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
        this.getPlugin().getServer().getPluginManager().registerEvents(this.vehicleMotionReducer, this.getPlugin());
    }

    @Override
    public boolean loadConfig() {
        this.vehicleMotionReducer = ReflectionUtils.createInstance("VehicleMotionReducer", this);
        this.forceLoad = this.getSection().getBoolean("force_load");
        this.minecart = this.getSection().getBoolean("minecart.enabled");
        this.boat = this.getSection().getBoolean("boat.enabled");
        this.minecart_remove_chest = this.getSection().getBoolean("minecart.remove_chest");

        return this.vehicleMotionReducer != null;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Getter
    public static abstract class NMS implements Listener {
        private final VehicleMotionReducerModule module;

        public NMS(VehicleMotionReducerModule module) {
            this.module = module;
        }

        public abstract boolean optimizeVehicle(Entity vehicle);
    }
}

