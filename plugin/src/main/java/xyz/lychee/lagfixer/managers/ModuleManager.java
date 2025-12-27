package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.menu.ConfigMenu;
import xyz.lychee.lagfixer.modules.*;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.HashMap;

@Getter
public class ModuleManager extends AbstractManager {
    private static @Getter ModuleManager instance;
    private final HashMap<String, AbstractModule> modules = new HashMap<>();

    public ModuleManager(LagFixer plugin) {
        super(plugin);
        instance = this;
        this.addAll(
                new MobAiReducerModule(plugin, this),
                new LagShieldModule(plugin, this),
                new RedstoneLimiterModule(plugin, this),
                new EntityLimiterModule(plugin, this),
                new ConsoleFilterModule(plugin, this),
                new WorldCleanerModule(plugin, this),
                new VehicleMotionReducerModule(plugin, this),
                new InstantLeafDecayModule(plugin, this),
                new AbilityLimiterModule(plugin, this),
                new ExplosionOptimizerModule(plugin, this)
        );
    }

    public <T extends AbstractModule> T get(Class<T> clazz) {
        return clazz.cast(this.modules.get(clazz.getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractModule> T get(String name) {
        return this.modules.containsKey(name) ? (T) this.modules.get(name) : null;
    }

    private void addAll(AbstractModule... arrModules) {
        for (AbstractModule module : arrModules) {
            modules.put(module.getClass().getSimpleName(), module);
        }
    }

    @Override
    public void load() {
        //Bukkit.getPluginManager().registerEvents(this, this.getPlugin());

        for (AbstractModule module : this.modules.values()) {
            try {
                TimingUtil t = TimingUtil.startNew();
                boolean success = module.loadAllConfig();
                boolean enabled = module.getConfig().getBoolean(module.getName() + ".enabled");

                if (enabled) {
                    if (success) {
                        module.load();
                        this.getPlugin().getLogger().info(" &8• &rSuccessfully loaded module " + module.getName() + " in " + t.stop().getExecutingTime() + "ms.");
                    } else {
                        this.getPlugin().getLogger().info(" &8• &rSkipping unsupported module " + module.getName() + " for " + Bukkit.getServer().getBukkitVersion() + ".");
                    }
                }

                module.setLoaded(success && enabled);
            } catch (Exception ex) {
                module.setLoaded(false);
                this.getPlugin().getLogger().info(" &8• &cSkipping module " + module.getName() + ", reason: " + ex.getMessage());
                this.getPlugin().printError(ex);
            }

            ConfigMenu menu = module.getMenu();
            menu.load();
            menu.updateAll();
        }

        if (this.getPlugin().getConfig().isSet("modules")) {
            this.getPlugin().saveConfig();
        }
    }

    @Override
    public void disable() {
        //HandlerList.unregisterAll(this);

        for (AbstractModule module : this.modules.values()) {
            if (!module.isLoaded()) continue;

            try {
                TimingUtil t = TimingUtil.startNew();
                module.disable();
                this.getPlugin().getLogger().info(" • Successfully disabled module " + module.getName() + " in " + t.stop().getExecutingTime() + "ms.");
            } catch (Exception ex) {
                this.getPlugin().getLogger().info(" • Error with disabling module " + module.getName() + ", reason: " + ex.getMessage());
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

