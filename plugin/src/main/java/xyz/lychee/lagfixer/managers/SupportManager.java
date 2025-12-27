package xyz.lychee.lagfixer.managers;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.sun.management.OperatingSystemMXBean;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractFork;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;
import xyz.lychee.lagfixer.support.PaperSupport;
import xyz.lychee.lagfixer.support.SpigotSupport;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Getter
@Setter
public class SupportManager extends AbstractManager implements Listener {
    private static @Getter SupportManager instance;

    private final Map<String, String> versions;
    private ScheduledExecutorService executor;
    private BukkitTask task = null;
    private String nmsVersion = null;
    private AbstractFork fork = null;
    private AbstractSupportNms nms = null;
    private AbstractMonitor monitor = new StandardMonitor();
    private int entities = 0, creatures = 0, items = 0, projectiles = 0, vehicles = 0;

    public SupportManager(LagFixer plugin) {
        super(plugin);

        instance = this;

        this.versions = new HashMap<>();
        this.versions.put("1.20.5", "v1_20_R4");
        this.versions.put("1.20.6", "v1_20_R4");
        this.versions.put("1.21", "v1_21_R1");
        this.versions.put("1.21.1", "v1_21_R1");
        this.versions.put("1.21.2", "v1_21_R2");
        this.versions.put("1.21.3", "v1_21_R2");
        this.versions.put("1.21.4", "v1_21_R3");
        this.versions.put("1.21.5", "v1_21_R4");
        this.versions.put("1.21.6", "v1_21_R5");
        this.versions.put("1.21.7", "v1_21_R5");
        this.versions.put("1.21.8", "v1_21_R5");
        this.versions.put("1.21.9", "v1_21_R6");
        this.versions.put("1.21.10", "v1_21_R6");
        this.versions.put("1.21.11", "v1_21_R7");
    }

    @Override
    public void load() {
        FileConfiguration cfg = this.getPlugin().getConfig();
        int threads = cfg.getInt("main.threads", 1);
        if (threads > 1) {
            this.executor = Executors.newScheduledThreadPool(threads);
        } else {
            this.executor = Executors.newSingleThreadScheduledExecutor();
        }

        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

        Stream.of(new SpigotSupport(this.getPlugin()), new PaperSupport(this.getPlugin()))
                .filter(AbstractFork::isSupported)
                .max(Comparator.comparingInt(AbstractFork::getPriority))
                .ifPresent(fork -> {
                    this.fork = fork;
                    this.getPlugin().getLogger().info(" &8• &rLoaded fork support ~ " + this.fork.getClass().getCanonicalName());
                });

        Server server = Bukkit.getServer();
        String version = server.getBukkitVersion().split("-")[0];
        if (this.versions.containsKey(version)) {
            this.nmsVersion = this.versions.get(version);
        } else {
            String[] parts = server.getClass().getPackage().getName().split("\\.");
            String lastPart = parts[parts.length - 1];
            if (lastPart.matches("v\\d+_\\d+_R\\d+")) {
                this.nmsVersion = lastPart;
            } else {
                this.getPlugin().getLogger().info("   &cPlugin is outdated, update from:");
                this.getPlugin().getLogger().info("   &chttps://modrinth.com/plugin/lagfixer");
                this.nms = new DeprecatedBukkitSupport(this.getPlugin());
                return;
            }
        }

        try {
            Class<?> clazz = Class.forName("xyz.lychee.lagfixer.nms." + this.nmsVersion + ".SupportNms");
            Constructor<?> constructor = clazz.getConstructor(Plugin.class);
            this.nms = (AbstractSupportNms) constructor.newInstance(this.getPlugin());
            Bukkit.getPluginManager().registerEvents(this.nms, this.getPlugin());

            this.getPlugin().getLogger().info(" &8• &rLoaded nms support ~ " + this.nms.getClass().getCanonicalName());
        } catch (Throwable ex) {
            this.getPlugin().getLogger().info("   &cOptimal support not found, the plugin will use reflection methods!");
            this.getPlugin().getLogger().info("   &7Supported versions: &e1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20 - 1.21.10");
            this.nms = new DeprecatedBukkitSupport(this.getPlugin());
        }

        this.monitor.start(cfg.getInt("main.monitor_interval"));
        this.task = this.getFork().runTimer(false, () -> {
            int entities = 0, creatures = 0, items = 0, projectiles = 0, vehicles = 0;

            for (World world : Bukkit.getWorlds()) {
                List<Entity> list = world.getEntities();
                entities += list.size();

                for (Entity entity : list) {
                    if (entity instanceof Mob) {
                        creatures++;
                    } else if (entity instanceof Vehicle) {
                        vehicles++;
                    } else if (entity instanceof Item) {
                        items++;
                    } else if (entity instanceof Projectile) {
                        projectiles++;
                    }
                }
            }

            this.entities = entities;
            this.creatures = creatures;
            this.items = items;
            this.projectiles = projectiles;
            this.vehicles = vehicles;
        }, 15, 30, TimeUnit.SECONDS);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
        try {
            if (this.executor != null) {
                this.executor.close();
            }
        }
        catch (Throwable ignored) {}
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    static class DeprecatedBukkitSupport extends AbstractSupportNms {
        private Method getServerMethod;
        private Field recentTpsField;
        private Method setProfileMethod;
        private Field profileField;
        private Method getHandleMethod;
        private Field pingField;

        public DeprecatedBukkitSupport(Plugin plugin) {
            super(plugin);
        }

        private void initializeReflection() {
            try {
                Object craftServer = Bukkit.getServer();
                getServerMethod = craftServer.getClass().getMethod("getServer");
                Object minecraftServer = getServerMethod.invoke(craftServer);
                recentTpsField = minecraftServer.getClass().getField("recentTps");
            } catch (Exception ignored) {}

            ItemMeta tempMeta = Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
            if (tempMeta != null) {
                try {
                    setProfileMethod = tempMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                    setProfileMethod.setAccessible(true);
                } catch (NoSuchMethodException ignored) {}

                try {
                    profileField = tempMeta.getClass().getDeclaredField("profile");
                    profileField.setAccessible(true);
                } catch (NoSuchFieldException ignored) {}
            }
        }

        @Override
        public double getTps() {
            initializeReflection();
            if (getServerMethod == null || recentTpsField == null) return -1;

            int index = 2;
            try {
                Object craftServer = Bukkit.getServer();
                Object minecraftServer = getServerMethod.invoke(craftServer);
                if (minecraftServer == null) return -1;

                double[] tps = (double[]) recentTpsField.get(minecraftServer);
                if (index >= tps.length) index = 0;
                return Math.min(20.0, tps[index]);
            } catch (Exception e) {
                return -1;
            }
        }

        @Override
        public ItemStack createSkull(String base64) {
            ItemStack is = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = is.getItemMeta();
            if (meta == null) return is;

            initializeReflection();

            UUID uuid = UUID.randomUUID();
            GameProfile profile = new GameProfile(uuid, uuid.toString().substring(0, 8));
            profile.getProperties().put("textures", new Property("textures", base64));

            if (setProfileMethod != null) {
                try {
                    setProfileMethod.invoke(meta, profile);
                    is.setItemMeta(meta);
                    return is;
                } catch (Exception ignored) {}
            }

            if (profileField != null) {
                try {
                    profileField.set(meta, profile);
                    is.setItemMeta(meta);
                    return is;
                } catch (Exception ignored) {}
            }

            return is;
        }

        @Override
        public int getTileEntitiesCount(Chunk chunk) {
            return 0;
        }

        @Override
        public int getPlayerPing(Player player) {
            initializeReflection();
            if (getHandleMethod == null || pingField == null) {
                try {
                    getHandleMethod = player.getClass().getMethod("getHandle");
                    Object entityPlayer = getHandleMethod.invoke(player);
                    pingField = entityPlayer.getClass().getField("ping");
                    pingField.setAccessible(true);
                } catch (Exception e) {
                    return -1;
                }
            }

            try {
                Object handle = getHandleMethod.invoke(player);
                return pingField.getInt(handle);
            } catch (Exception e) {
                return -1;
            }
        }
    }

    class StandardMonitor extends AbstractMonitor {
        private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        @Override
        protected double cpuProcess() {
            return this.osBean.getProcessCpuLoad() * 100.0;
        }

        @Override
        protected double cpuSystem() {
            return this.osBean.getSystemCpuLoad() * 100.0;
        }

        @Override
        protected double tps() {
            return SupportManager.this.getNms().getTps();
        }

        @Override
        protected double mspt() {
            return SupportManager.this.getFork().getMspt();
        }
    }
}