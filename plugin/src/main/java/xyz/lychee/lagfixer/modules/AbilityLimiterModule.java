package xyz.lychee.lagfixer.modules;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.FastRandom;

public class AbilityLimiterModule extends AbstractModule implements Listener {
    private final FastRandom random = new FastRandom();
    private int trident_cooldown;
    private int elytra_cooldown;
    private int trident_durability;
    private int elytra_durability;

    public AbilityLimiterModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.MEDIUM, "AbilityLimiter",
                new String[]{
                        "限制三叉戟和鞘翅的快速使用，以防止过度加载区块。",
                        "频繁的高速移动可能导致服务器卡顿和不稳定。",
                        "AbilityLimiter允许调整减速幅度，以平衡性能和玩家体验。",
                        "启用AbilityLimiter可确保更流畅的世界加载、稳定的服务器性能以及可控的移动能力。"
                },
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZmM2YwMzM0Yzk0MzhlOGM3NGMwZjIxNjdiMDkxN2QwZDQ2ZDk3MzYzNjk2NGY5MDI3NDJlZDU1NmZiMDc4MiJ9fX0=");
    }

    @EventHandler
    public void onPlayerRiptide(PlayerRiptideEvent e) {
        if (!this.canContinue(e.getPlayer().getWorld())) return;

        e.getPlayer().setCooldown(Material.TRIDENT, this.trident_cooldown);
        this.damageItem(e.getPlayer(), e.getItem(), this.trident_durability);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!this.canContinue(e.getPlayer().getWorld())) return;

        ItemStack firework;
        Player player = e.getPlayer();
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate != null
                && chestplate.getType() == Material.ELYTRA
                && (firework = e.getItem()) != null
                && firework.getType() == Material.FIREWORK_ROCKET) {
            Action action = e.getAction();
            if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                    && player.isGliding()
                    && !player.hasCooldown(Material.FIREWORK_ROCKET)) {
                SupportManager.getInstance().getFork().runLater(false, () -> player.setCooldown(Material.FIREWORK_ROCKET, this.elytra_cooldown), 50L);

                this.damageItem(player, chestplate, this.elytra_durability);
            }
        }
    }

    public void damageItem(Player player, ItemStack is, int defaultDuraLoss) {
        if (defaultDuraLoss < 1 || player.getGameMode() == GameMode.CREATIVE) return;

        ItemMeta meta = is.getItemMeta();
        if (meta == null) return;

        int duraLoss = defaultDuraLoss;
        if (meta.hasEnchant(Enchantment.DURABILITY)) {
            float lossChance = 100F / (is.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            for (int i = 0; i < defaultDuraLoss; i++) {
                if (this.random.nextFloat() * 100F < lossChance) {
                    duraLoss++;
                }
            }
        }

        if (!meta.isUnbreakable()) {
            int newDurability = is.getDurability() + duraLoss;
            int maxDurability = is.getType().getMaxDurability();
            is.setDurability((short) Math.min(newDurability, maxDurability));
        }
    }

    @Override
    public void load() {
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    @Override
    public boolean loadConfig() {
        this.elytra_cooldown = this.getSection().getInt("elytra_boost.cooldown") * 20;
        this.elytra_durability = this.getSection().getInt("elytra_boost.additional_durability_loss");

        this.trident_cooldown = this.getSection().getInt("trident_riptide.cooldown") * 20;
        this.trident_durability = this.getSection().getInt("trident_riptide.additional_durability_loss");

        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }
}