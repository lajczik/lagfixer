package xyz.lychee.lagfixer.modules;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class InstantLeafDecayModule extends AbstractModule implements Listener {
    private static final EnumSet<BlockFace> FACES = EnumSet.of(BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.DOWN, BlockFace.UP);
    private boolean dropItems;
    private boolean leavesDecay;
    private int treeDistance;

    public InstantLeafDecayModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, AbstractModule.Impact.LOW, "InstantLeafDecay",
                new String[]{
                        "Ensures instant leaf removal, reducing leaf blocks for ticking.",
                        "Vital for server performance by eliminating gradual leaf block processing.",
                        "Optimizes server resources for smoother gameplay without decay overhead.",
                        "Ideal for servers with forestry, managing leaf block accumulation."
                }, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWYyZDE0NjkyZDhiMGUzNTI2YTZmYWY0MjY2NzI3YmQwMmFhYTdiMDUyN2IxODVhY2Y3ZjBhYTY2NzkzZmZkYyJ9fX0=");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (!this.canContinue(block.getWorld())) return;

        if (this.isValidLeaf(block)) {
            this.breakLeaves(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent e) {
        Block block = e.getBlock();
        if (!this.canContinue(block.getWorld())) return;

        e.setCancelled(true);
        if (this.leavesDecay && this.isValidLeaf(block)) {
            this.breakLeaves(block);
        }
    }

    private void breakLeaves(Block block) {
        ArrayDeque<Block> stack = new ArrayDeque<>();
        Set<Block> scheduled = new HashSet<>();
        stack.push(block);
        scheduled.add(block);

        while (!stack.isEmpty()) {
            Block currentBlock = stack.pop();

            if (this.dropItems) {
                currentBlock.breakNaturally();
            } else {
                currentBlock.setType(Material.AIR, false);
            }

            for (BlockFace face : FACES) {
                Block neighbor = currentBlock.getRelative(face);
                if (this.isValidLeaf(neighbor) && scheduled.add(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
    }

    private boolean isValidLeaf(Block block) {
        BlockData blockData = block.getBlockData();
        if (blockData instanceof Leaves) {
            Leaves leaves = (Leaves) blockData;
            return leaves.getDistance() >= this.treeDistance && !leaves.isPersistent();
        }
        return false;
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
    }

    @Override
    public boolean loadConfig() {
        this.dropItems = this.getSection().getBoolean("drop_items");
        this.treeDistance = this.getSection().getInt("tree_distance");
        this.leavesDecay = this.getSection().getBoolean("leaves_decay");
        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }
}