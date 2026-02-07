package xyz.lychee.lagfixer.modules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.scheduler.BukkitTask;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.ReflectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedstoneLimiterModule extends AbstractModule implements Listener {
    private final Map<String, Long> cooldown = new ConcurrentHashMap<>();
    private final Map<Chunk, TickCounter> redstone_map = new ConcurrentHashMap<>();
    private final Map<Chunk, TickCounter> piston_map = new ConcurrentHashMap<>();
    private final EnumSet<Material> push_blacklist = EnumSet.noneOf(Material.class);
    private BukkitTask task;
    private int ticks_redsone;
    private int ticks_piston;
    private int click_cooldown;
    private boolean break_redstone;
    private boolean break_piston;
    private boolean alerts;

    public RedstoneLimiterModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.MEDIUM, "RedstoneLimiter",
                new String[]{
                        "禁用高需求的红石时钟以防止服务器过载。",
                        "某些红石配置可能导致性能下降和崩溃。",
                        "激活AntiRedstone可保持服务器稳定性并确保响应性。",
                        "即使存在复杂红石装置，也能保障不间断的游戏体验。"
                }, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjExNzZjNGQ2Mzk1ZmY1NzY3YTc0YTM2OWZlMzg2ZDA2Y2M2MGEyMDk3YmM1YTUzYmQwMDVlYWRkMGE3Y2JkNCJ9fX0="
        );
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent e) {
        if (e.getOldCurrent() != 0 || !this.canContinue(e.getBlock().getWorld())) {
            return;
        }
        TickCounter counter = this.redstone_map.computeIfAbsent(e.getBlock().getChunk(), TickCounter::new);
        counter.addTick(e.getBlock(), 1);
        if (counter.ticks > this.ticks_redsone) {
            e.setNewCurrent(e.getOldCurrent());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent e) {
        if (!this.canContinue(e.getBlock().getWorld())) {
            return;
        }
        if (e.getBlocks().stream().anyMatch(b -> this.push_blacklist.contains(b.getType()))) {
            e.setCancelled(true);
            return;
        }
        TickCounter counter = this.piston_map.computeIfAbsent(e.getBlock().getChunk(), TickCounter::new);
        counter.addTick(e.getBlock(), e.getBlocks().size());
        if (counter.ticks > this.ticks_piston) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (!this.canContinue(e.getPlayer().getWorld())) {
            return;
        }

        Block b = e.getBlockPlaced();
        if (b.getType() == Material.REDSTONE_TORCH || b.getType() == Material.REDSTONE_WALL_TORCH) {
            e.setCancelled(this.hasCooldown(e.getPlayer(), b));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (!this.canContinue(e.getPlayer().getWorld())) {
            return;
        }

        Block b = e.getClickedBlock();
        if (b != null && e.getAction() == Action.RIGHT_CLICK_BLOCK && (b.getType() == Material.LEVER || Tag.BUTTONS.isTagged(b.getType()))) {
            e.setCancelled(this.hasCooldown(e.getPlayer(), b));
        }
    }

    public boolean hasCooldown(Player p, Block b) {
        String material = b.getType().name();
        String id = p.getUniqueId() + ":" + material;
        long time = this.cooldown.getOrDefault(id, -1L);
        if (time < System.currentTimeMillis()) {
            this.cooldown.put(id, System.currentTimeMillis() + (long) this.click_cooldown);
            return false;
        }
        Component text = this.getLanguage().getComponent("cooldown", true, Placeholder.unparsed("cooldown", Long.toString(time - System.currentTimeMillis())), Placeholder.unparsed("material", material));
        this.getPlugin().getAudiences().player(p).sendActionBar(text);
        return true;
    }

    @Override
    public void load() {
        this.task = SupportManager.getInstance().getFork().runTimer(true, () -> {
            this.redstone_map.values().forEach(counter -> counter.complete(this.ticks_redsone, this.break_redstone));
            this.piston_map.values().forEach(counter -> counter.complete(this.ticks_piston, this.break_piston));
        }, 1L, 2L, TimeUnit.SECONDS);
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    @Override
    public boolean loadConfig() {
        this.alerts = this.getSection().getBoolean("alerts");
        this.ticks_redsone = this.getSection().getInt("ticks_limit.redstone");
        this.ticks_piston = this.getSection().getInt("ticks_limit.piston");
        this.click_cooldown = this.getSection().getInt("click_cooldown");
        this.break_redstone = this.getSection().getBoolean("break_block.redstone");
        this.break_piston = this.getSection().getBoolean("break_block.piston");

        ReflectionUtils.convertEnums(Material.class, this.push_blacklist, this.getSection().getStringList("piston.push_blacklist"));
        return true;
    }

    @Override
    public void disable() {
        if (this.task != null) {
            this.task.cancel();
        }
        HandlerList.unregisterAll(this);
        this.redstone_map.clear();
        this.piston_map.clear();
    }

    public class TickCounter {
        private final Chunk chunk;
        private final HashSet<Block> blocks = new HashSet<>();
        private int ticks = 0;

        public TickCounter(Chunk chunk) {
            this.chunk = chunk;
        }

        public void addTick(Block block, int size) {
            this.blocks.add(block);
            this.ticks += size;
        }

        public Location getLocation(Set<Block> blocks) {
            double x = 0.0;
            double y = 0.0;
            double z = 0.0;
            double size = blocks.size();
            for (Block b : blocks) {
                x += b.getX();
                y += b.getY();
                z += b.getZ();
            }
            return new Location(this.chunk.getWorld(), x / size, y / size, z / size);
        }

        public void complete(int limit, boolean breakBlocks) {
            if (this.ticks > limit) {
                Set<Block> blockSet = this.blocks.stream().filter(Objects::nonNull).collect(Collectors.toSet());
                this.blocks.clear();
                Location loc = this.getLocation(blockSet);
                if (alerts) {
                    Component message = getLanguage().getComponent("alert", true, Placeholder.unparsed("ticks", Integer.toString(this.ticks)), Placeholder.unparsed("location", "x: " + loc.getBlockX() + ", y: " + loc.getBlockY() + ", z: " + loc.getBlockZ()));
                    getPlugin().getAudiences().filter(ServerOperator::isOp).sendMessage(message);
                }
                this.ticks = 0;
                if (breakBlocks) {
                    SupportManager.getInstance().getFork().runNow(false, loc, () -> blockSet.forEach(block -> block.setType(Material.AIR)));
                }
                return;
            }
            this.ticks = 0;
            this.blocks.clear();
        }
    }
}