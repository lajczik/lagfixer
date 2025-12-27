package xyz.lychee.lagfixer.hooks;

import com.songoda.ultimatestacker.api.UltimateStackerApi;
import com.songoda.ultimatestacker.api.stack.item.StackedItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

import java.util.Collection;

public class UltimateStackerHook extends AbstractHook implements HookManager.StackerContainer {
    public UltimateStackerHook(LagFixer plugin, HookManager manager) {
        super(plugin, "UltimateStacker", manager);
    }

    @Override
    public void addItemsToList(Item bItem, Collection<ItemStack> items) {
        ItemStack is = bItem.getItemStack();
        StackedItem stackedItem = UltimateStackerApi.getStackedItemManager().getStackedItem(bItem);

        int amount = stackedItem.getAmount();
        int maxStack = is.getMaxStackSize();

        while (amount > 0) {
            ItemStack clone = is.clone();
            clone.setAmount(Math.min(amount, maxStack));
            items.add(clone);
            amount -= maxStack;
        }
    }

    @Override
    public boolean isStacked(LivingEntity entity) {
        return UltimateStackerApi.getEntityStackManager().isStackedEntity(entity);
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
    }
}

