package xyz.lychee.lagfixer.hooks;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.modules.WorldCleanerModule;
import xyz.lychee.lagfixer.objects.AbstractHook;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
import xyz.lychee.lagfixer.objects.WorldsMonitor;

import java.lang.management.ManagementFactory;

@Getter
public class PlaceholderAPIHook extends AbstractHook {
    private PapiImplementation papi;

    public PlaceholderAPIHook(LagFixer plugin, HookManager manager) {
        super(plugin, "PlaceholderAPI", manager);
    }

    public String applyPlaceholders(Player p, String text) {
        return PlaceholderAPI.setPlaceholders(p, text);
    }

    @Override
    public void load() {
        this.papi = new PapiImplementation(this.getPlugin());
        this.papi.register();
    }

    @Override
    public void disable() {
        this.papi.unregister();
    }

    public static class PapiImplementation
            extends PlaceholderExpansion {
        private final LagFixer plugin;

        public PapiImplementation(LagFixer plugin) {
            this.plugin = plugin;
        }

        @NotNull
        public String getIdentifier() {
            return "lagfixer";
        }

        @NotNull
        public String getAuthor() {
            return "lajczik, Syncara Host";
        }

        @NotNull
        public String getVersion() {
            return this.plugin.getDescription().getVersion();
        }

        public String onPlaceholderRequest(Player p, @NotNull String id) {
            return this.response(id);
        }

        public String onRequest(OfflinePlayer p, @NotNull String id) {
            return this.response(id);
        }

        public boolean persist() {
            return true;
        }

        public boolean canRegister() {
            return true;
        }

        public String response(String id) {
            SupportManager support = SupportManager.getInstance();
            ResourceMonitor resourceMonitor = support.getResourceMonitor();
            WorldsMonitor worldsMonitor = support.getWorldsMonitor();
            WorldCleanerModule worldCleaner = ModuleManager.getInstance().get(WorldCleanerModule.class);
            Runtime runtime = Runtime.getRuntime();

            switch (id.toLowerCase()) {
                // ==================== Performance Metrics ====================
                case "tps": {
                    return Double.toString(resourceMonitor.getTps());
                }
                case "tps_color": {
                    // Returns TPS with color code based on value
                    double tps = resourceMonitor.getTps();
                    String color = tps >= 18 ? "&a" : (tps >= 15 ? "&e" : "&c");
                    return color + String.format("%.1f", tps);
                }
                case "mspt": {
                    return Double.toString(resourceMonitor.getMspt());
                }
                case "mspt_color": {
                    double mspt = resourceMonitor.getMspt();
                    String color = mspt <= 40 ? "&a" : (mspt <= 50 ? "&e" : "&c");
                    return color + String.format("%.1f", mspt);
                }
                case "cpu":
                case "cpuprocess": {
                    return Double.toString(resourceMonitor.getCpuProcess());
                }
                case "cpusystem": {
                    return Double.toString(resourceMonitor.getCpuSystem());
                }

                // ==================== Entity Counts ====================
                case "entities":
                case "entities_total": {
                    return Integer.toString(worldsMonitor.getEntities());
                }
                case "entities_mobs":
                case "mobs": {
                    return Integer.toString(worldsMonitor.getCreatures());
                }
                case "entities_items":
                case "items": {
                    return Integer.toString(worldsMonitor.getItems());
                }
                case "entities_projectiles":
                case "projectiles": {
                    return Integer.toString(worldsMonitor.getProjectiles());
                }
                case "entities_vehicles":
                case "vehicles": {
                    return Integer.toString(worldsMonitor.getVehicles());
                }

                // ==================== Memory Stats ====================
                case "memory_used":
                case "ram_used": {
                    return ((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024) + "MB";
                }
                case "memory_max":
                case "ram_max": {
                    return (runtime.maxMemory() / 1024 / 1024) + "MB";
                }
                case "memory_free":
                case "ram_free": {
                    return (runtime.freeMemory() / 1024 / 1024) + "MB";
                }
                case "memory_percent":
                case "ram_percent": {
                    long used = runtime.totalMemory() - runtime.freeMemory();
                    long max = runtime.maxMemory();
                    int percent = (int) (used * 100 / max);
                    return percent + "%";
                }
                case "memory_bar": {
                    // Returns a visual progress bar for memory
                    long used = runtime.totalMemory() - runtime.freeMemory();
                    long max = runtime.maxMemory();
                    int percent = (int) (used * 100 / max);
                    int filled = percent / 10;
                    StringBuilder bar = new StringBuilder("&8[");
                    for (int i = 0; i < 10; i++) {
                        bar.append(i < filled ? "&a■" : "&7■");
                    }
                    bar.append("&8]");
                    return bar.toString();
                }

                // ==================== Server Stats ====================
                case "players":
                case "online": {
                    return Integer.toString(Bukkit.getOnlinePlayers().size());
                }
                case "players_max":
                case "max_players": {
                    return Integer.toString(Bukkit.getMaxPlayers());
                }
                case "worlds": {
                    return Integer.toString(Bukkit.getWorlds().size());
                }
                case "chunks":
                case "loaded_chunks": {
                    int chunks = 0;
                    for (World world : org.bukkit.Bukkit.getWorlds()) {
                        chunks += world.getLoadedChunks().length;
                    }
                    return Integer.toString(chunks);
                }
                case "uptime": {
                    long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
                    long hours = uptime / 3600;
                    long minutes = (uptime % 3600) / 60;
                    long seconds = uptime % 60;
                    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
                }
                case "uptime_hours": {
                    return Long.toString(ManagementFactory.getRuntimeMXBean().getUptime() / 1000 / 3600);
                }
                case "uptime_minutes": {
                    return Long.toString(ManagementFactory.getRuntimeMXBean().getUptime() / 1000 / 60);
                }
                case "uptime_seconds": {
                    return Long.toString(ManagementFactory.getRuntimeMXBean().getUptime() / 1000);
                }

                // ==================== Worldcleaner Timer ====================
                case "worldcleaner_timer":
                case "worldcleaner": {
                    return worldCleaner == null || !worldCleaner.isLoaded() ? "0s" : worldCleaner.getSecond() + "s";
                }
                case "worldcleaner_seconds": {
                    return worldCleaner == null || !worldCleaner.isLoaded() ? "0" : Integer.toString(worldCleaner.getSecond());
                }
                case "worldcleaner_formatted": {
                    if (worldCleaner == null || !worldCleaner.isLoaded()) return "00:00";
                    int seconds = worldCleaner.getSecond();
                    int mins = seconds / 60;
                    int secs = seconds % 60;
                    return String.format("%02d:%02d", mins, secs);
                }
                case "worldcleaner_interval": {
                    return worldCleaner == null || !worldCleaner.isLoaded() ? "0" : Integer.toString(worldCleaner.getInterval());
                }
                case "worldcleaner_enabled": {
                    return Boolean.toString(worldCleaner != null && worldCleaner.isLoaded());
                }
                case "worldcleaner_progress": {
                    if (worldCleaner == null || !worldCleaner.isLoaded()) return "0%";
                    int interval = worldCleaner.getInterval();
                    int remaining = worldCleaner.getSecond();
                    int elapsed = interval - remaining;
                    int percent = interval > 0 ? (elapsed * 100 / interval) : 0;
                    return percent + "%";
                }
                case "worldcleaner_bar": {
                    // Returns a visual progress bar for worldcleaner timer
                    if (worldCleaner == null || !worldCleaner.isLoaded()) return "&8[&7■■■■■■■■■■&8]";
                    int interval = worldCleaner.getInterval();
                    int remaining = worldCleaner.getSecond();
                    int elapsed = interval - remaining;
                    int percent = interval > 0 ? (elapsed * 100 / interval) : 0;
                    int filled = percent / 10;
                    StringBuilder bar = new StringBuilder("&8[");
                    for (int i = 0; i < 10; i++) {
                        bar.append(i < filled ? "&c■" : "&a■");
                    }
                    bar.append("&8]");
                    return bar.toString();
                }
            }
            return null;
        }
    }
}
