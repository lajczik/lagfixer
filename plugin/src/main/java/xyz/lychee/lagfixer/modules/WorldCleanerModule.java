package xyz.lychee.lagfixer.modules;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.hooks.LevelledMobsHook;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class WorldCleanerModule extends AbstractModule implements Listener, CommandExecutor {
    private final HashSet<ItemStack> items = new HashSet<>();
    private final HashMap<Integer, String> messages = new HashMap<>();
    private final EnumSet<EntityType> creatures_list = EnumSet.noneOf(EntityType.class);
    private final EnumSet<EntityType> projectiles_list = EnumSet.noneOf(EntityType.class);
    private final ArrayList<Inventory> inventories = new ArrayList<>();
    private final EnumSet<Material> items_abyss_blacklist = EnumSet.noneOf(Material.class);
    private final ItemStack items_abyss_previous;
    private final ItemStack items_abyss_next;
    private final ItemStack items_abyss_filler;
    private final EnumSet<Material> items_blacklist = EnumSet.noneOf(Material.class);
    private BukkitTask task;
    private int second;
    private int interval;
    private boolean alerts_enabled;
    private Audience alerts_audience;
    private boolean alerts_actionbar;
    private boolean alerts_message;

    private boolean creatures_enabled;
    private boolean creatures_named;
    private boolean creatures_dropitems;
    private boolean creatures_stacked;
    private boolean creatures_levelled;
    private boolean creatures_ignore_models;
    private boolean creatures_listmode;

    private boolean items_enabled;
    private boolean items_disableitemdespawn;
    private int items_timelived;
    private boolean items_abyss_enabled;
    private volatile boolean items_abyss_opened = false;
    private boolean items_abyss_alerts;
    private boolean items_abyss_itemdespawn;
    private String items_abyss_permission;
    private int items_abyss_close;

    private boolean projectiles_enabled;
    private boolean projectiles_listmode;

    public WorldCleanerModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, AbstractModule.Impact.MEDIUM, "WorldCleaner",
                new String[]{
                        "Cleans up old items on the ground to accelerate server performance.",
                        "Accumulation of items over time contributes to server lag, especially in densely populated or active servers.",
                        "Kills creatures to accelerate server performance.", "Players can retrieve items from the Abyss inventory using the /abyss command."
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTlkODA2Yjc1ZWM5NTAwNmM1ZWMzODY2YzU0OGM1NTcxYWYzZTc4OGM3ZDE2MjllZGU2NGJjMWI3NDg4NTljZCJ9fX0="
        );

        SupportManager support = SupportManager.getInstance();
        this.items_abyss_previous = support.getNms().createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg0ZjU5NzEzMWJiZTI1ZGMwNThhZjg4OGNiMjk4MzFmNzk1OTliYzY3Yzk1YzgwMjkyNWNlNGFmYmEzMzJmYyJ9fX0=");
        this.items_abyss_next = support.getNms().createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYzMzlmZjJlNTM0MmJhMThiZGM0OGE5OWNjYTY1ZDEyM2NlNzgxZDg3ODI3MmY5ZDk2NGVhZDNiOGFkMzcwIn19fQ==");
        this.items_abyss_filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

        ItemMeta metaFiller = this.items_abyss_filler.getItemMeta();
        if (metaFiller != null) {
            metaFiller.setDisplayName(MessageUtils.fixColors(null, "&8#"));
            this.items_abyss_filler.setItemMeta(metaFiller);
        }
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent e) {
        if (this.items_disableitemdespawn && e.getEntity().getPickupDelay() < 10000) {
            e.setCancelled(true);
            return;
        }
        if (this.items_abyss_enabled && this.items_abyss_itemdespawn && !this.items_abyss_blacklist.contains(e.getEntity().getItemStack().getType())) {
            HookManager.StackerContainer stacker = HookManager.getInstance().getStacker();
            if (stacker != null) {
                stacker.addItemsToList(e.getEntity(), this.items);
            } else {
                this.items.add(e.getEntity().getItemStack());
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        if (inv != null && this.inventories.contains(inv)) {
            ItemStack is = e.getCurrentItem();
            if (is == null) return;

            if (is.equals(this.items_abyss_next)) {
                e.setCancelled(true);
                int next = this.inventories.indexOf(inv) + 1;
                if (next < this.inventories.size()) {
                    e.getWhoClicked().openInventory(this.inventories.get(next));
                }
            } else if (is.equals(this.items_abyss_previous)) {
                e.setCancelled(true);
                int previous = this.inventories.indexOf(inv) - 1;
                if (previous >= 0) {
                    e.getWhoClicked().openInventory(this.inventories.get(previous));
                }
            } else if (is.equals(this.items_abyss_filler)) {
                e.setCancelled(true);
            }
        }
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        Component text;
        if (!this.items_abyss_enabled) {
            text = Language.getMainValue("disabled_module", true, Placeholder.unparsed("module", this.getName()));
        } else if (this.items_abyss_permission != null && !this.items_abyss_permission.isEmpty() && !sender.hasPermission(this.items_abyss_permission)) {
            text = Language.getMainValue("no_access", true, Placeholder.unparsed("permission", this.items_abyss_permission));
        } else if (!this.items_abyss_opened) {
            text = this.getLanguage().getComponent("items.abyss.closed", true);
        } else if (sender instanceof Player) {
            if (this.inventories.isEmpty()) {
                text = this.getLanguage().getComponent("items.abyss.empty", true);
            } else {
                ((Player) sender).openInventory(this.inventories.get(0));
                text = this.getLanguage().getComponent("items.abyss.opened", true);
            }
        } else {
            text = Language.getMainValue("player_only", true);
        }

        if (text != null) {
            this.getPlugin().getAudiences().sender(sender).sendMessage(text);
        }
        return false;
    }

    @Override
    public void load() throws IOException {
        SupportManager support = SupportManager.getInstance();
        support.getFork().registerCommand(this.getPlugin(), "abyss", Collections.emptyList(), this);
        this.task = SupportManager.getInstance().getFork().runTimer(false, () -> {
            if (--this.second <= 0) {
                HookManager.StackerContainer stacker = HookManager.getInstance().getStacker();

                int creatures = 0, items = 0, projectiles = 0;

                for (World world : this.getAllowedWorlds()) {
                    for (Entity ent : world.getEntities()) {
                        if (ent instanceof LivingEntity) {
                            if (this.creatures_enabled && this.clearCreature((LivingEntity) ent)) {
                                if (this.creatures_dropitems) {
                                    ((LivingEntity) ent).damage(Double.MAX_VALUE);
                                }
                                ent.remove();
                                creatures++;
                            }
                        } else if (ent instanceof Item) {
                            if (this.items_enabled && this.clearItem((Item) ent)) {
                                if (this.items_abyss_enabled && !this.items_abyss_blacklist.contains(((Item) ent).getItemStack().getType())) {
                                    Item item = (Item) ent;
                                    if (stacker != null) {
                                        stacker.addItemsToList(item, this.items);
                                    } else {
                                        this.items.add(item.getItemStack().clone());
                                    }
                                }
                                ent.remove();
                                items++;
                            }
                        } else if (ent instanceof Projectile) {
                            if (this.projectiles_enabled && this.clearProjectile((Projectile) ent)) {
                                ent.remove();
                                projectiles++;
                            }
                        }
                    }
                }

                if (this.alerts_enabled && this.messages.containsKey(this.second)) {
                    this.sendAlert(
                            Language.createComponent(this.messages.get(this.second), true,
                                    Placeholder.unparsed("remaining", Integer.toString(this.second)),
                                    Placeholder.unparsed("items", Integer.toString(items)),
                                    Placeholder.unparsed("creatures", Integer.toString(creatures)),
                                    Placeholder.unparsed("projectiles", Integer.toString(projectiles))
                            )
                    );
                }

                if (this.items_abyss_enabled) {
                    this.items_abyss_opened = true;

                    String guiName = this.getLanguage().getString("items.abyss.gui.name", true);
                    Collection<ItemStack> toStore = new ArrayList<>(this.items);
                    this.items.clear();
                    int page = 0;
                    while (!toStore.isEmpty()) {
                        Inventory inv = Bukkit.createInventory(null, 54,
                                MessageUtils.fixColors(null, guiName.replace("<page>", Integer.toString(++page)))
                        );

                        for (int i = 45; i < 52; i++) {
                            inv.setItem(i, this.items_abyss_filler);
                        }
                        inv.setItem(52, this.items_abyss_previous);
                        inv.setItem(53, this.items_abyss_next);

                        toStore = inv.addItem(toStore.toArray(new ItemStack[0])).values();

                        this.inventories.add(inv);
                    }

                    if (this.items_abyss_alerts) {
                        this.sendAlert(this.getLanguage().getComponent("items.abyss.open", true));
                    }

                    support.getFork().runLater(false, () -> {
                        this.items_abyss_opened = false;

                        this.inventories.forEach(inv -> {
                            new HashSet<>(inv.getViewers()).forEach(HumanEntity::closeInventory);
                            inv.clear();
                        });
                        this.inventories.clear();

                        if (this.items_abyss_alerts) {
                            this.sendAlert(this.getLanguage().getComponent("items.abyss.close", true));
                        }
                    }, this.items_abyss_close, TimeUnit.SECONDS);
                }
                this.second = this.interval + 1;
            } else if (this.alerts_enabled && this.messages.containsKey(this.second)) {
                this.sendAlert(
                        Language.createComponent(this.messages.get(this.second), true,
                                Placeholder.unparsed("remaining", Integer.toString(this.second)),
                                Placeholder.unparsed("items", Long.toString(support.getItems())),
                                Placeholder.unparsed("creatures", Long.toString(support.getCreatures())),
                                Placeholder.unparsed("projectiles", Long.toString(support.getProjectiles()))
                        )
                );
            }
        }, 1L, 1L, TimeUnit.SECONDS);
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    public void sendAlert(Component text) {
        if (this.alerts_message) this.alerts_audience.sendMessage(text);
        if (this.alerts_actionbar) this.alerts_audience.sendActionBar(text);
    }

    public boolean clearCreature(LivingEntity ent) {
        if (this.creatures_list.contains(ent.getType()) != this.creatures_listmode || ent instanceof HumanEntity) {
            return false;
        }

        HookManager hm = HookManager.getInstance();
        if (this.creatures_ignore_models) {
            HookManager.ModelContainer model = hm.getModel();
            if (model != null && model.hasModel(ent)) {
                return false;
            }
        }

        LevelledMobsHook lvlHook = hm.getHook(LevelledMobsHook.class);
        if (lvlHook != null && lvlHook.isLevelled(ent)) {
            return this.creatures_levelled;
        }

        HookManager.StackerContainer stacker = hm.getStacker();
        if (stacker != null && stacker.isStacked(ent)) {
            return this.creatures_stacked;
        }

        if (ent.getCustomName() != null) {
            return this.creatures_named;
        }

        return true;
    }

    public boolean clearItem(Item ent) {
        return !ent.isInvulnerable()
                && ent.getPickupDelay() < 200
                && ent.getTicksLived() > this.items_timelived
                && !this.items_blacklist.contains(ent.getItemStack().getType());
    }

    public boolean clearProjectile(Projectile ent) {
        return this.getProjectiles_list().contains(ent.getType()) == this.isProjectiles_listmode();
    }

    @Override
    public boolean loadConfig() {
        this.interval = Math.max(this.getSection().getInt("interval"), 1);
        this.second = this.interval + 1;

        this.alerts_enabled = this.getSection().getBoolean("alerts.enabled");
        this.alerts_message = this.getSection().getBoolean("alerts.message");
        this.alerts_actionbar = this.getSection().getBoolean("alerts.actionbar");

        String permission = this.getSection().getString("alerts.permission");
        boolean permissionDisabled = permission == null || permission.isEmpty();
        this.alerts_audience = this.getPlugin().getAudiences()
                .filter(s -> s instanceof Player && (permissionDisabled || s.hasPermission(permission)));


        this.creatures_enabled = this.getSection().getBoolean("creatures.enabled");
        if (this.creatures_enabled) {
            this.creatures_named = this.getSection().getBoolean("creatures.named");
            this.creatures_dropitems = this.getSection().getBoolean("creatures.drop_items");
            this.creatures_stacked = this.getSection().getBoolean("creatures.stacked");
            this.creatures_levelled = this.getSection().getBoolean("creatures.levelled");
            this.creatures_ignore_models = HookManager.getInstance().noneModels() || this.getSection().getBoolean("creatures.ignore_models");
            this.creatures_listmode = this.getSection().getBoolean("creatures.list_mode");
            ReflectionUtils.convertEnums(EntityType.class, this.creatures_list, this.getSection().getStringList("creatures.list"));
        }

        this.items_enabled = this.getSection().getBoolean("items.enabled");
        if (this.items_enabled) {
            this.items_timelived = this.getSection().getInt("items.time_lived") / 50;
            this.items_disableitemdespawn = this.getSection().getBoolean("items.disable_item_despawn");
            ReflectionUtils.convertEnums(Material.class, this.items_blacklist, this.getSection().getStringList("items.blacklist"));

            this.items_abyss_enabled = this.getSection().getBoolean("items.abyss.enabled");
            if (this.items_abyss_enabled) {
                this.items_abyss_alerts = this.getSection().getBoolean("items.abyss.alerts");
                this.items_abyss_permission = this.getSection().getString("items.abyss.permission");
                this.items_abyss_itemdespawn = this.getSection().getBoolean("items.abyss.item_despawn");
                this.items_abyss_close = this.getSection().getInt("items.abyss.close");
                ReflectionUtils.convertEnums(Material.class, this.items_abyss_blacklist, this.getSection().getStringList("items.abyss.blacklist"));

                ItemMeta metaPrevious = this.items_abyss_previous.getItemMeta();
                if (metaPrevious != null) {
                    metaPrevious.setDisplayName(MessageUtils.fixColors(null, this.getLanguage().getString("items.abyss.gui.previous", false)));
                    this.items_abyss_previous.setItemMeta(metaPrevious);
                }

                ItemMeta metaNext = this.items_abyss_next.getItemMeta();
                if (metaNext != null) {
                    metaNext.setDisplayName(MessageUtils.fixColors(null, this.getLanguage().getString("items.abyss.gui.next", false)));
                    this.items_abyss_next.setItemMeta(metaNext);
                }
            }
        }

        this.messages.clear();
        for (String str : Language.getYaml().getStringList("messages." + this.getName() + ".countingdown")) {
            try {
                int equalSignIndex = str.indexOf('=');
                if (equalSignIndex != -1) {
                    String index = str.substring(0, equalSignIndex);
                    String message = str.substring(equalSignIndex + 1);

                    try {
                        int parsedIndex = Integer.parseInt(index);
                        this.messages.put(parsedIndex, message);
                    } catch (NumberFormatException e) {
                        this.getPlugin().getLogger().warning("Invalid index format in countingdown message: " + index + " for message \"" + message + "\"");
                    }
                } else {
                    this.getPlugin().getLogger().warning("Skipping malformed countingdown message (no \"=\" found): " + str);
                }
            } catch (Exception ex) {
                this.getPlugin().getLogger().info("Error processing countingdown message: " + str);
                this.getPlugin().printError(ex);
            }
        }

        this.projectiles_enabled = this.getSection().getBoolean("projectiles.enabled");
        if (this.projectiles_enabled) {
            this.projectiles_listmode = this.getSection().getBoolean("projectiles.list_mode");
            ReflectionUtils.convertEnums(EntityType.class, this.projectiles_list, this.getSection().getStringList("projectiles.list"));
        }

        return true;
    }

    @Override
    public void disable() throws IOException {
        HandlerList.unregisterAll(this);
        if (this.task != null && !this.task.isCancelled()) {
            this.task.cancel();
        }
    }
}

