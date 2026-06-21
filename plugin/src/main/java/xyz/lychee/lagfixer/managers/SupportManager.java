package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
<<<<<<< HEAD
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.*;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
=======
import org.bukkit.Server;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.*;
import xyz.lychee.lagfixer.support.PaperSupport;
import xyz.lychee.lagfixer.support.SpigotSupport;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90

@Getter
@Setter
public class SupportManager extends AbstractManager {
    private static @Getter SupportManager instance;

    private final Map<String, String> versions = new HashMap<>();
<<<<<<< HEAD
    private String nmsVersion = null;
    private ISupportNms nms = null;
    private ResourceMonitor resourceMonitor;
    private WorldsMonitor worldsMonitor;
=======
    private BukkitTask task = null;
    private String nmsVersion = null;
    private AbstractFork fork = null;
    private ISupportNms nms = null;
    private ResourceMonitor resourceMonitor = new ResourceMonitor();
    private WorldsMonitor worldsMonitor = new WorldsMonitor();
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    private HttpClient client = HttpClient.newBuilder()
            .executor(Executors.newSingleThreadExecutor())
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public SupportManager(LagFixer plugin) {
        super(plugin);

        instance = this;

<<<<<<< HEAD
        this.resourceMonitor = new ResourceMonitor(plugin);
        this.worldsMonitor = new WorldsMonitor(plugin);

=======
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
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

        this.versions.put("26.1", "v26_1");
        this.versions.put("26.2", "v26_1");
<<<<<<< HEAD
        this.versions.put("26.3", "v26_1");
        this.versions.put("26.4", "v26_1");
        this.versions.put("26.5", "v26_1");
        this.versions.put("27.1", "v26_1");
        this.versions.put("27.2", "v26_1");
        this.versions.put("27.3", "v26_1");
=======
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    }

    @Override
    public void load() {
<<<<<<< HEAD
=======
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

        Stream.of(new SpigotSupport(this.getPlugin()), new PaperSupport(this.getPlugin()))
                .filter(AbstractFork::isSupported)
                .max(Comparator.comparingInt(AbstractFork::getPriority))
                .ifPresent(fork -> {
                    this.fork = fork;
                    this.getPlugin().getLogger().info(" &8• &rLoaded fork support ~ " + this.fork.getClass().getCanonicalName());
                });

>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        Server server = Bukkit.getServer();
        String version = server.getBukkitVersion().split("-")[0];

        if (!version.startsWith("1.")) {
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                version = parts[0] + "." + parts[1];
            }
        }

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
<<<<<<< HEAD
                this.nms = new DeprecatedSupportNms();
            }
        }

        if (this.nms == null) {
            try {
                Class<?> clazz = Class.forName("xyz.lychee.lagfixer.nms." + this.nmsVersion + ".SupportNms");
                this.nms = (ISupportNms) clazz.getConstructor().newInstance();
                this.getPlugin().getLogger().info(" &8• &rLoaded nms support ~ " + this.nms.getClass().getCanonicalName());
            } catch (Throwable ex) {
                this.getPlugin().getLogger().info("   &cOptimal support for "+this.nmsVersion+" not found!");
                this.getPlugin().getLogger().info("   &7Supported versions: &e1.20 - 26.2");
                this.nms = new DeprecatedSupportNms();
            }
=======
                this.nms = new ReflectionSupportNms();
                return;
            }
        }

        try {
            Class<?> clazz = Class.forName("xyz.lychee.lagfixer.nms." + this.nmsVersion + ".SupportNms");
            this.nms = (ReflectionSupportNms) clazz.getConstructor().newInstance();
            //Bukkit.getPluginManager().registerEvents(this.nms, this.getPlugin());

            this.getPlugin().getLogger().info(" &8• &rLoaded nms support ~ " + this.nms.getClass().getCanonicalName());
        } catch (Throwable ex) {
            this.getPlugin().getLogger().info("   &cOptimal support for " + this.nmsVersion + " not found, the plugin will use reflection methods!");
            this.getPlugin().getLogger().info("   &7Supported versions: &e1.16.5, 1.17.1, 1.18.2, 1.19.4, 1.20 - 26.1.2");
            this.nms = new ReflectionSupportNms();
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        }

        this.resourceMonitor.start();
        this.worldsMonitor.start();
<<<<<<< HEAD
    }

    public static Map<RegionPos, List<Chunk>> createRegionMap(World world) {
        Map<RegionPos, List<Chunk>> regions = new HashMap<>();

        for (Chunk chunk : world.getLoadedChunks()) {
            int regionX = chunk.getX() >> 3;
            int regionZ = chunk.getZ() >> 3;

            RegionPos pos = new RegionPos(regionX, regionZ);
            regions.computeIfAbsent(pos, k -> new ArrayList<>()).add(chunk);
        }
        return regions;
    }

    @Override
    public void disable() {}
=======
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90

    @Override
    public boolean isEnabled() {
        return true;
    }
<<<<<<< HEAD

    @Getter
    public static final class RegionPos {
        private final int x;
        private final int z;
        private final int hash;

        public RegionPos(int x, int z) {
            this.x = x;
            this.z = z;
            this.hash = (x * 7340033) ^ z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof RegionPos other)) return false;

            return this.x == other.x && this.z == other.z;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}

=======
}
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
