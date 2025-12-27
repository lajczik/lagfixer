package xyz.lychee.lagfixer.hooks;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import uk.antiperson.stackmob.StackMob;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

import java.util.Collection;

public class StackMobHook extends AbstractHook implements HookManager.StackerContainer {
    private StackMobHolder holder;

    public StackMobHook(LagFixer plugin, HookManager manager) {
        super(plugin, "StackMob", manager);
    }

    @Override
    public void addItemsToList(Item bItem, Collection<ItemStack> items) {
        items.add(bItem.getItemStack());
    }

    @Override
    public boolean isStacked(LivingEntity entity) {
        return this.holder.isStacked(entity);
    }

    @Override
    public void load() {
        this.holder = new StackMobHolder();
    }

    @Override
    public void disable() {
    }

    static class StackMobHolder {
        private final StackMob stackMob;

        public StackMobHolder() {
            this.stackMob = JavaPlugin.getPlugin(StackMob.class);
        }

        public boolean isStacked(LivingEntity entity) {
            return this.stackMob.getEntityManager().isStackedEntity(entity);
        }
    }
}

