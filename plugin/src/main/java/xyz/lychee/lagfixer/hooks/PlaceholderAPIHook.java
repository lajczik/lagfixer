package xyz.lychee.lagfixer.hooks;

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.modules.WorldCleanerModule;
import xyz.lychee.lagfixer.objects.AbstractHook;
import xyz.lychee.lagfixer.objects.AbstractMonitor;

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
            return "lychee";
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
            AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
            switch (id.toLowerCase()) {
                case "tps": {
                    return Double.toString(monitor.getTps());
                }
                case "mspt": {
                    return Double.toString(monitor.getMspt());
                }
                case "cpuprocess": {
                    return Double.toString(monitor.getCpuProcess());
                }
                case "cpusystem": {
                    return Double.toString(monitor.getCpuSystem());
                }
                case "worldcleaner": {
                    WorldCleanerModule module = ModuleManager.getInstance().get(WorldCleanerModule.class);
                    return module == null || !module.isLoaded() ? null : module.getSecond() + "s";
                }
            }
            return null;
        }
    }
}

