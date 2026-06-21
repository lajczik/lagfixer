package xyz.lychee.lagfixer.modules;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.hooks.LevelledMobsHook;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.objects.RegionsEntityReport;
import xyz.lychee.lagfixer.objects.WorldsMonitor;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@Getter
public class WorldCleanerModule extends AbstractModule implements Listener {
    private final HashSet<ItemStack> items = new HashSet<>();
    private final HashMap<Integer, String> messages = new HashMap<>();
    private final EnumSet<EntityType> creatures_list = EnumSet.noneOf(EntityType.class);
    private final EnumSet<EntityType> projectiles_list = EnumSet.noneOf(EntityType.class);
    private final ArrayList<Inventory> inventories = new ArrayList<>();
    private final EnumSet<Material> items_abyss_blacklist = EnumSet.noneOf(Material.class);
    private final ItemBuilder items_abyss_previous;
    private final ItemBuilder items_abyss_next;
    private final ItemBuilder items_abyss_filler;
    private final EnumSet<Material> items_blacklist = EnumSet.noneOf(Material.class);
    private Command command;
    private ScheduledTask task;
    private boolean opened = false;
    private int second;
    private int interval;
    private boolean alerts_enabled;
    private String alerts_permission;
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
    private boolean items_abyss_alerts;
    private Sound items_abyss_open_sound;
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

        this.items_abyss_previous = ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg0ZjU5NzEzMWJiZTI1ZGMwNThhZjg4OGNiMjk4MzFmNzk1OTliYzY3Yzk1YzgwMjkyNWNlNGFmYmEzMzJmYyJ9fX0=");
        this.items_abyss_next = ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYzMzlmZjJlNTM0MmJhMThiZGM0OGE5OWNjYTY1ZDEyM2NlNzgxZDg3ODI3MmY5ZDk2NGVhZDNiOGFkMzcwIn19fQ==");
        this.items_abyss_filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("&8#");
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

            if (is.equals(this.items_abyss_next.getItem())) {
                e.setCancelled(true);
                int next = this.inventories.indexOf(inv) + 1;
                if (next < this.inventories.size()) {
                    e.getWhoClicked().openInventory(this.inventories.get(next));
                }
            } else if (is.equals(this.items_abyss_previous.getItem())) {
                e.setCancelled(true);
                int previous = this.inventories.indexOf(inv) - 1;
                if (previous >= 0) {
                    e.getWhoClicked().openInventory(this.inventories.get(previous));
                }
            } else if (is.equals(this.items_abyss_filler.getItem())) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void load() throws IOException {
        this.command = new Command(Collections.emptyList());
        Bukkit.getCommandMap().register(this.command.getName(), this.command);

        this.second = this.interval + 1;
        this.task = Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), t1 -> {
            if (--this.second <= 0) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                RegionsEntityReport report = new RegionsEntityReport();
                for (World world : this.getAllowedWorlds()) {
                    this.purgeAll(world, futures, report);
                }

                String message = this.alerts_enabled ? this.messages.get(this.second) : null;
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .orTimeout(5, TimeUnit.SECONDS)
                        .whenComplete((v, t) -> {
                            if (message != null) {
                                Component text = Language.createComponent(message, true,
                                        Placeholder.unparsed("remaining", Integer.toString(this.second)),
                                        Placeholder.unparsed("items", report.getItems().toString()),
                                        Placeholder.unparsed("creatures", report.getCreatures().toString()),
                                        Placeholder.unparsed("projectiles", report.getProjectiles().toString())
                                );

                                this.sendAlert(text);
                            }

                            if (this.items_abyss_enabled) {
                                String guiName = this.getLanguage().getString("items.abyss.gui.name", true);
                                Collection<ItemStack> toStore = new ArrayList<>(this.items);
                                this.items.clear();
                                int page = 0;
                                while (!toStore.isEmpty()) {
                                    Inventory inv = Bukkit.createInventory(null, 54,
                                            Language.getSerializer().deserialize(guiName.replace("<page>", Integer.toString(++page)))
                                    );

                                    for (int i = 45; i < 52; i++) {
                                        inv.setItem(i, this.items_abyss_filler.getItem());
                                    }
                                    inv.setItem(52, this.items_abyss_previous.getItem());
                                    inv.setItem(53, this.items_abyss_next.getItem());

                                    toStore = inv.addItem(toStore.toArray(new ItemStack[0])).values();

                                    this.inventories.add(inv);
                                }

                                this.opened = true;

                                if (this.items_abyss_alerts) {
                                    Component open = this.getLanguage().getComponent("items.abyss.open", true);
                                    this.sendAlert(open);
                                }
                            }
                        });

                if (this.items_abyss_enabled) {
                    Bukkit.getAsyncScheduler().runDelayed(this.getPlugin(), t2 -> {
                        this.opened = false;

                        Set<HumanEntity> viewers = new HashSet<>();
                        this.inventories.forEach(inv -> {
                            viewers.addAll(inv.getViewers());
                            inv.clear();
                        });
                        this.inventories.clear();

                        Bukkit.getGlobalRegionScheduler().execute(this.getPlugin(), () -> viewers.forEach(HumanEntity::closeInventory));

                        if (this.items_abyss_alerts) {
                            Component close = this.getLanguage().getComponent("items.abyss.close", true);
                            this.sendAlert(close);
                        }
                    }, this.items_abyss_close, TimeUnit.SECONDS);
                }

                this.second = this.interval + 1;
            } else if (this.alerts_enabled && this.messages.containsKey(this.second)) {
                WorldsMonitor worldsMonitor = SupportManager.getInstance().getWorldsMonitor();

                Component text = Language.createComponent(this.messages.get(this.second), true,
                        Placeholder.unparsed("remaining", Integer.toString(this.second)),
                        Placeholder.unparsed("items", Long.toString(worldsMonitor.getItems())),
                        Placeholder.unparsed("creatures", Long.toString(worldsMonitor.getCreatures())),
                        Placeholder.unparsed("projectiles", Long.toString(worldsMonitor.getProjectiles()))
                );

                this.sendAlert(text);
            }
        }, 1L, 1L, TimeUnit.SECONDS);
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    public void sendAlert(Component component) {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> this.alerts_permission == null || p.hasPermission(this.alerts_permission))
                .forEach(p -> {
                    if (this.alerts_message) {
                        p.sendMessage(component);
                    }
                    if (this.alerts_actionbar) {
                        p.sendActionBar(component);
                    }
                });
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

        if (ent.customName() != null) {
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
        this.alerts_permission = permission == null || permission.isBlank() ? null : permission;

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

                boolean open_sound = this.getSection().getBoolean("items.abyss.open_sound.enabled");
                if (open_sound) {
                    double volume = this.getSection().getDouble("items.abyss.open_sound.volume");
                    double pitch = this.getSection().getDouble("items.abyss.open_sound.pitch");
                    String sound = this.getSection().getString("items.abyss.open_sound.sound");

                    if (sound != null && !sound.isEmpty()) {
                        try {
                            this.items_abyss_open_sound = Sound.sound(
                                    Key.key(Key.MINECRAFT_NAMESPACE, sound.toLowerCase()),
                                    Sound.Source.MASTER,
                                    (float) Math.clamp(volume, 0.0, 1.0),
                                    (float) Math.clamp(pitch, 0.0, 2.0)
                            );
                        } catch (Exception e) {
                            this.items_abyss_open_sound = null;
                        }
                    }
                }
                this.items_abyss_permission = this.getSection().getString("items.abyss.permission");
                this.items_abyss_itemdespawn = this.getSection().getBoolean("items.abyss.item_despawn");
                this.items_abyss_close = this.getSection().getInt("items.abyss.close");
                ReflectionUtils.convertEnums(Material.class, this.items_abyss_blacklist, this.getSection().getStringList("items.abyss.blacklist"));

                this.items_abyss_previous.setName(this.getLanguage().getString("items.abyss.gui.previous", false));
                this.items_abyss_next.setName(this.getLanguage().getString("items.abyss.gui.next", false));
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

        if (this.command != null) {
            this.command.unregister(Bukkit.getCommandMap());
        }
    }

    public void purgeAll(World world, List<CompletableFuture<Void>> into, RegionsEntityReport report) {
        HookManager.StackerContainer stacker = HookManager.getInstance().getStacker();
        RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();

        Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
        regions.forEach((regionPos, chunks) -> {
            Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (Chunk chunk : chunks) {
                    Entity[] entities = chunk.getEntities();
                    report.getEntities().add(entities.length);
                    for (Entity ent : entities) {
                        if (ent instanceof Mob creature) {
                            if (this.isCreatures_enabled() && this.clearCreature(creature)) {
                                if (this.isCreatures_dropitems()) creature.damage(Double.MAX_VALUE);
                                else creature.remove();
                                report.getCreatures().increment();
                            }
                        } else if (ent instanceof Item item) {
                            if (this.isItems_enabled() && this.clearItem(item)) {
                                ItemStack is = item.getItemStack();
                                if (this.isItems_abyss_enabled() && !this.getItems_abyss_blacklist().contains(is.getType())) {
                                    if (stacker != null) {
                                        stacker.addItemsToList(item, items);
                                    } else {
                                        items.add(is);
                                    }
                                }
                                item.remove();
                                report.getItems().increment();
                            }
                        } else if (ent instanceof Projectile projectile) {
                            if (this.isProjectiles_enabled() && this.clearProjectile(projectile)) {
                                projectile.remove();
                                report.getProjectiles().increment();
                            }
                        }
                    }
                }
            }, executor);

            into.add(future);
        });
    }

    public void purgeCreatures(World world, List<CompletableFuture<Void>> into, LongAdder size) {
        if (!this.isCreatures_enabled()) return;

        RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();

        Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
        regions.forEach((regionPos, chunks) -> {
            Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (Chunk chunk : chunks) {
                    Entity[] entities = chunk.getEntities();
                    for (Entity ent : entities) {
                        if (ent instanceof Mob creature && this.clearCreature(creature)) {
                            if (this.isCreatures_dropitems()) creature.damage(Double.MAX_VALUE);
                            else creature.remove();
                            size.increment();
                        }
                    }
                }
            }, executor);

            into.add(future);
        });
    }

    public void purgeItems(World world, List<CompletableFuture<Void>> into, LongAdder size) {
        if (!this.isItems_enabled()) return;

        HookManager.StackerContainer stacker = HookManager.getInstance().getStacker();
        RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();

        Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
        regions.forEach((regionPos, chunks) -> {
            Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (Chunk chunk : chunks) {
                    Entity[] entities = chunk.getEntities();
                    for (Entity ent : entities) {
                        if (ent instanceof Item item && this.clearItem(item)) {
                            ItemStack is = item.getItemStack();
                            if (this.isItems_abyss_enabled() && !this.getItems_abyss_blacklist().contains(is.getType())) {
                                if (stacker != null) {
                                    stacker.addItemsToList(item, items);
                                } else {
                                    items.add(is);
                                }
                            }
                            item.remove();
                            size.increment();
                        }
                    }
                }
            }, executor);

            into.add(future);
        });
    }

    public void purgeProjectiles(World world, List<CompletableFuture<Void>> into, LongAdder size) {
        if (!this.isProjectiles_enabled()) return;

        RegionScheduler scheduler = Bukkit.getServer().getRegionScheduler();

        Map<SupportManager.RegionPos, List<Chunk>> regions = SupportManager.createRegionMap(world);
        regions.forEach((regionPos, chunks) -> {
            Executor executor = task -> scheduler.execute(this.getPlugin(), world, regionPos.getX() << 3, regionPos.getZ() << 3, task);
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (Chunk chunk : chunks) {
                    Entity[] entities = chunk.getEntities();
                    for (Entity ent : entities) {
                        if (ent instanceof Projectile projectile && this.clearProjectile(projectile)) {
                            projectile.remove();
                            size.increment();
                        }
                    }
                }
            }, executor);

            into.add(future);
        });
    }

    public class Command extends BukkitCommand {
        public Command(List<String> aliases) {
            super("abyss", "Abyss lagfixer command", "/abyss", aliases);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] args) {
            Component text;
            if (!items_abyss_enabled) {
                text = Language.getMainValue("disabled_module", true, Placeholder.unparsed("module", "Abyss (" + this.getName() + ")"));
            } else if (items_abyss_permission != null && !items_abyss_permission.isEmpty() && !sender.hasPermission(items_abyss_permission)) {
                text = Language.getMainValue("no_access", true, Placeholder.unparsed("permission", items_abyss_permission));
            } else if (!opened) {
                text = getLanguage().getComponent("items.abyss.closed", true);
            } else if (sender instanceof Player) {
                if (inventories.isEmpty()) {
                    text = getLanguage().getComponent("items.abyss.empty", true);
                } else {
                    ((Player) sender).openInventory(inventories.getFirst());
                    text = getLanguage().getComponent("items.abyss.opened", true);
                }
            } else {
                text = Language.getMainValue("player_only", true);
            }

            if (text != null) {
                sender.sendMessage(text);
            }
            return false;
        }
    }
}