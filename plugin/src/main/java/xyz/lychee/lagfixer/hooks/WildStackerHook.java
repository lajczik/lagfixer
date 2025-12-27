package xyz.lychee.lagfixer.hooks;

import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

import java.util.Collection;

public class WildStackerHook extends AbstractHook implements HookManager.StackerContainer {
    public WildStackerHook(LagFixer plugin, HookManager manager) {
        super(plugin, "WildStacker", manager);
    }

    @Override
    public void addItemsToList(Item bItem, Collection<ItemStack> items) {
        ItemStack is = bItem.getItemStack();
        StackedItem stackedItem = WildStackerAPI.getStackedItem(bItem);

        if (stackedItem == null) {
            items.add(is);
            return;
        }

        int amount = stackedItem.getStackAmount();
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
        try {
            return WildStackerAPI.getStackedEntity(entity).getStackAmount() > 1;
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void disable() {
    }
}