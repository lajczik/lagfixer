package xyz.lychee.lagfixer.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.utils.ItemBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
public abstract class AbstractMenu implements Listener {
    private static final @Getter ItemStack border = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName("&8#").build();
    private static final @Getter ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&8#").build();
    private static final @Getter ItemStack disabled;
    private static final @Getter ItemStack enabled;
    private static final @Getter ItemStack back;

    static {
        enabled = ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTkwNzkzZjU2NjE2ZjEwMTUwMmRlMWQzNGViMjU0NGY2MDdkOTg5MDBlMzY5OTM2OTI5NTMxOWU2MzBkY2Y2ZCJ9fX0=").setName("&a&lENABLED!").setLore(" &8{*} &7Click to &cdisable &7this module!").build();
        disabled = ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTRiZDlhNDViOTY4MWNlYTViMjhjNzBmNzVhNjk1NmIxZjU5NGZlYzg0MGI5NjA3Nzk4ZmIxZTcwNzc2NDQzMCJ9fX0=").setName("&c&lDISABLED!").setLore(" &8{*} &7Click to &aenable &7this module!").build();
        back = ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjM0OTM5ZDI2NDQ0YTU3MzI3ZjA2NGMzOTI4ZGE2MWYzNmNhZjYyMmRlYmU3NGMzM2Y4ZjhhMzZkYTIyIn19fQ==").setName("&3&lPREVIOUS MENU!").setLore(" &8{*} &7Click to &3return &7to previous menu!").build();
    }

    private final LagFixer plugin;
    private final Inventory inv;
    private final BukkitTask task;
    private final HashMap<Integer, ItemClickEvent> clicks = new HashMap<>();
    private boolean updated = false;

    public AbstractMenu(LagFixer plugin, int size, String title, int interval, boolean async) {
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(null, size, title);
        this.task = interval > 0 ? SupportManager.getInstance().getFork().runTimer(async, () -> {
            if (!this.updated || !this.inv.getViewers().isEmpty()) {
                this.updateAll();
            }
        }, 1L, interval, TimeUnit.SECONDS) : null;
    }

    public void updateAll() {
        this.clicks.forEach((slot, event) -> {
            try {
                this.inv.setItem(slot, event.getCallableItem().call());
            } catch (Exception ignored) {}
        });
        this.update();
        this.updated = true;
    }

    public abstract void update();

    public abstract void handleClick(InventoryClickEvent e, ItemStack is);

    public abstract AbstractMenu previousMenu();

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void unload() {
        new HashSet<>(this.inv.getViewers()).forEach(HumanEntity::closeInventory);
        this.inv.clear();
        HandlerList.unregisterAll(this);
    }

    public void itemClickEvent(int slot, Callable<ItemStack> item, Consumer<InventoryClickEvent> event) {
        this.clicks.put(slot, new ItemClickEvent(item, event));
    }

    @EventHandler(ignoreCancelled = true)
    public void inventoryClickEvent(InventoryClickEvent e) {
        ItemStack click = e.getCurrentItem();
        if (click != null
                && click.getType() != Material.AIR
                && Objects.equals(e.getClickedInventory(), this.inv)
        ) {
            e.setCancelled(true);

            ItemClickEvent itemClickEvent = this.clicks.get(e.getSlot());
            if (itemClickEvent != null && itemClickEvent.getEventConsumer() != null) {
                itemClickEvent.getEventConsumer().accept(e);
                return;
            }

            if (click.equals(AbstractMenu.getBack())) {
                AbstractMenu back = this.previousMenu();
                if (back != null) {
                    e.getWhoClicked().openInventory(back.getInv());
                }
                return;
            }

            this.handleClick(e, click);
        }
    }

    public void surroundInventory() {
        int i;
        int size = this.inv.getSize();
        for (i = 0; i < 9; ++i) {
            this.inv.setItem(i, AbstractMenu.getBorder());
            this.inv.setItem(size - 9 + i, AbstractMenu.getBorder());
        }
        for (i = 9; i < size - 9; i += 9) {
            this.inv.setItem(i, AbstractMenu.getBorder());
            this.inv.setItem(i + 8, AbstractMenu.getBorder());
        }
        if (this.previousMenu() != null) {
            this.inv.setItem(size - 1, AbstractMenu.getBack());
        }
    }

    public void fillInventory() {
        for (int i = 0; i < this.inv.getSize(); ++i) {
            ItemStack invitem = this.inv.getItem(i);
            if (invitem != null && invitem.getType() != Material.AIR) continue;
            this.inv.setItem(i, AbstractMenu.getFiller());
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ItemClickEvent {
        private final Callable<ItemStack> callableItem;
        private final Consumer<InventoryClickEvent> eventConsumer;
    }
}

