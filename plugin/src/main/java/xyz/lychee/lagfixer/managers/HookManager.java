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

@Getter
public class HookManager extends AbstractManager {
    private static @Getter HookManager instance;
    private final Map<String, AbstractHook> hooks = new HashMap<>();
    private final Map<Class<? extends AbstractHook>, AbstractHook> loadedHooks = new HashMap<>();
    private StackerContainer stackerHook;
    private ModelContainer modelHook;

    public HookManager(LagFixer plugin) {
        super(plugin);
        instance = this;

        this.add(
                // Models
                new ModelEngineHook(plugin, this),
                new MythicMobsHook(plugin, this),
                new BetterModelHook(plugin, this),

                // Stackers
                new WildStackerHook(plugin, this),
                new RoseStackerHook(plugin, this),
                new UltimateStackerHook(plugin, this),
                new StackMobHook(plugin, this),

                // Mics
                new PlaceholderAPIHook(plugin, this),
                new SparkHook(plugin, this),
                new PacketEventsHook(plugin, this)
        );
    }

    protected void add(AbstractHook... hooks) {
        for (AbstractHook hook : hooks) {
            this.hooks.put(hook.getName(), hook);
        }
    }

    public @Nullable <T extends AbstractHook> T getHookIfLoaded(Class<T> clazz) {
        return this.loadedHooks.containsKey(clazz) ? clazz.cast(this.loadedHooks.get(clazz)) : null;
    }

    public boolean hasModel(Entity entity) {
        ModelContainer model = this.getModelHook();
        return model != null && model.hasModel(entity);
    }

    public void loadHook(AbstractHook hook) throws Exception {
        hook.load();
        if (hook instanceof ModelContainer model) {
            this.modelHook = model;
        }
        else if (hook instanceof StackerContainer stacker) {
            this.stackerHook = stacker;
        }
        else {
            this.loadedHooks.put(hook.getClass(), hook);
        }
    }

    public void unloadHook(AbstractHook hook) {
        hook.disable();
        if (hook instanceof ModelContainer) {
            this.modelHook = null;
        }
        else if (hook instanceof StackerContainer) {
            this.stackerHook = null;
        }
        else {
            this.loadedHooks.remove(hook.getClass());
        }
    }

    @Override
    public void load() {
        for (AbstractHook hook : this.hooks.values()) {
            if (!hook.isSupported()) continue;

            try {
                TimingUtil t = TimingUtil.startNew();
                this.loadHook(hook);
                this.getPlugin().getLogger().info(" &8• &rSuccessfully loaded hook " + hook.getName() + " in " + t.stop() + "!");
            } catch (Throwable ex) {
                this.unloadHook(hook);
                this.getPlugin().getLogger().info(" &8• &cError with enabling hook " + hook.getName() + ", reason: " + ex.getMessage());
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public void disable() {
        for (AbstractHook hook : this.hooks.values()) {
            if (!hook.isSupported()) continue;

            TimingUtil t = TimingUtil.startNew();
            this.unloadHook(hook);
            this.getPlugin().getLogger().info(" &8• &rSuccessfully disabled hook " + hook.getName() + " in " + t.stop() + "!");
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

