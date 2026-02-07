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
        for (AbstractModule module : this.modules.values()) {
            try {
                TimingUtil t = TimingUtil.startNew();
                boolean success = module.loadAllConfig();
                boolean enabled = module.getConfig().getBoolean(module.getName() + ".enabled");

                if (enabled) {
                    if (success) {
                        module.load();
                        // 翻译：成功加载模块
                        this.getPlugin().getLogger().info(" &8• &r成功加载模块 " + module.getName() + "，耗时 " + t.stop().getExecutingTime() + "ms。");
                    } else {
                        // 翻译：跳过不支持的模块
                        this.getPlugin().getLogger().info(" &8• &r跳过不支持的模块 " + module.getName() + "（当前服务端版本：" + Bukkit.getServer().getBukkitVersion() + "）。");
                    }
                }

                module.setLoaded(success && enabled);
            } catch (Exception ex) {
                module.setLoaded(false);
                // 翻译：跳过模块
                this.getPlugin().getLogger().info(" &8• &c跳过模块 " + module.getName() + "，原因：" + ex.getMessage());
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
        for (AbstractModule module : this.modules.values()) {
            if (!module.isLoaded()) continue;

            try {
                TimingUtil t = TimingUtil.startNew();
                module.disable();
                // 翻译：成功禁用模块
                this.getPlugin().getLogger().info(" • 成功禁用模块 " + module.getName() + "，耗时 " + t.stop().getExecutingTime() + "ms。");
            } catch (Exception ex) {
                // 翻译：禁用模块时出错
                this.getPlugin().getLogger().info(" • 禁用模块 " + module.getName() + " 时出错，原因：" + ex.getMessage());
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}