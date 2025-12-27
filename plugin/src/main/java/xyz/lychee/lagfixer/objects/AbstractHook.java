package xyz.lychee.lagfixer.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;

@Getter
@Setter
public abstract class AbstractHook {
    private final LagFixer plugin;
    private final HookManager manager;
    private final String name;
    private boolean loaded = false;

    public AbstractHook(LagFixer plugin, String name, HookManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.name = name;
    }

    public boolean isSupported() {
        return Bukkit.getPluginManager().getPlugin(this.name) != null;
    }

    public abstract void load() throws Exception;

    public abstract void disable() throws Exception;
}

