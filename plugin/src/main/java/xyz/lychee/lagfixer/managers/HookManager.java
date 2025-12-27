package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.hooks.*;
import xyz.lychee.lagfixer.objects.AbstractHook;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HookManager
        extends AbstractManager {
    private static @Getter HookManager instance;
    private final Map<String, AbstractHook> hooks = new HashMap<>();
    private final Map<Class<? extends AbstractHook>, AbstractHook> loadedHooks = new HashMap<>();
    private final Map<String, ModelContainer> modelHooks = new HashMap<>();
    private final Map<String, StackerContainer> stackerHooks = new HashMap<>();

    public HookManager(LagFixer plugin) {
        super(plugin);
        instance = this;

        this.add(new PlaceholderAPIHook(plugin, this));
        this.add(new SparkHook(plugin, this));
        this.add(new WildStackerHook(plugin, this));
        this.add(new RoseStackerHook(plugin, this));
        this.add(new UltimateStackerHook(plugin, this));
        this.add(new ModelEngineHook(plugin, this));
        this.add(new MythicMobsHook(plugin, this));
        this.add(new StackMobHook(plugin, this));
    }

    protected void add(AbstractHook hook) {
        this.hooks.put(hook.getName(), hook);
    }

    public @Nullable <T extends AbstractHook> T getHook(Class<T> clazz) {
        return this.loadedHooks.containsKey(clazz) ? clazz.cast(this.loadedHooks.get(clazz)) : null;
    }

    public @Nullable StackerContainer getStacker() {
        for (StackerContainer stacker : this.stackerHooks.values()) {
            return stacker;
        }
        return null;
    }

    public @Nullable ModelContainer getModel() {
        for (ModelContainer model : this.modelHooks.values()) {
            return model;
        }
        return null;
    }

    public boolean noneStackers() {
        return this.stackerHooks.isEmpty();
    }

    public boolean noneModels() {
        return this.modelHooks.isEmpty();
    }

    public void addLoaded(AbstractHook hook) {
        this.loadedHooks.put(hook.getClass(), hook);

        if (hook instanceof ModelContainer) {
            this.modelHooks.put(hook.getName(), (ModelContainer) hook);
        }

        if (hook instanceof StackerContainer) {
            this.stackerHooks.put(hook.getName(), (StackerContainer) hook);
        }
    }

    public void removeLoaded(AbstractHook hook) {
        hook.setLoaded(false);
        this.loadedHooks.remove(hook.getClass());
        this.modelHooks.remove(hook.getName());
        this.stackerHooks.remove(hook.getName());
    }

    @Override
    public void load() {
        for (AbstractHook hook : this.hooks.values()) {
            if (!hook.isSupported()) continue;

            try {
                TimingUtil t = TimingUtil.startNew();
                hook.load();
                this.addLoaded(hook);
                this.getPlugin().getLogger().info(" &8• &rSuccessfully loaded hook " + hook.getName() + " in " + t.stop().getExecutingTime() + "ms!");
            } catch (Exception ex) {
                this.getPlugin().getLogger().info(" &8• &cError with enabling hook " + hook.getName() + ", reason: " + ex.getMessage());
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public void disable() {
        for (AbstractHook hook : this.hooks.values()) {
            if (!hook.isSupported()) continue;

            try {
                TimingUtil t = TimingUtil.startNew();
                hook.disable();
                this.removeLoaded(hook);
                this.getPlugin().getLogger().info(" &8• &rSuccessfully disabled hook " + hook.getName() + " in " + t.stop().getExecutingTime() + "ms!");
            } catch (Exception ex) {
                this.getPlugin().getLogger().info(" &8• &cError with disabling hook " + hook.getName() + ", reason: " + ex.getMessage());
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public interface ModelContainer {
        boolean hasModel(Entity entity);
    }

    public interface StackerContainer {
        void addItemsToList(Item item, Collection<ItemStack> items);

        boolean isStacked(LivingEntity entity);
    }
}

