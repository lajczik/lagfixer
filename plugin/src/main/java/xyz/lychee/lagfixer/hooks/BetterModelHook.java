package xyz.lychee.lagfixer.hooks;

import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import org.bukkit.entity.Entity;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class BetterModelHook extends AbstractHook implements HookManager.ModelContainer {
    public BetterModelHook(LagFixer plugin, HookManager manager) {
        super(plugin, "BetterModel", manager);
    }

    public boolean hasModel(Entity entity) {
        return EntityTrackerRegistry.registry(entity.getUniqueId()) != null;
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
    }
}