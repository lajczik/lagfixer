package xyz.lychee.lagfixer.modules;

import org.bukkit.Bukkit;
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
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractModule;
<<<<<<< HEAD

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
=======
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90

public class AbilityLimiterModule extends AbstractModule implements Listener {
    private int trident_cooldown;
    private int elytra_cooldown;
    private double trident_speed;
    private double elytra_speed;
    private int trident_durability;
    private int elytra_durability;

    public AbilityLimiterModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.MEDIUM, "AbilityLimiter",
                new String[]{
                        "Limits rapid Trident and Elytra usage to prevent excessive chunk loading.",
                        "Frequent high-speed travel can cause server lag and instability.",
                        "AbilityLimiter allows adjusting the speed reduction to balance performance and player experience.",
                        "Activating AbilityLimiter ensures smoother world loading, stable server performance, and controlled mobility."
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
                player.setCooldown(Material.FIREWORK_ROCKET, this.elytra_cooldown);

                if (firework.getAmount() > 1) firework.setAmount(firework.getAmount() - 1);
                else player.getInventory().remove(firework);

                int duration;
                ItemMeta meta = firework.getItemMeta();
                if (meta instanceof FireworkMeta fireworkMeta) {
                    duration = switch (fireworkMeta.getPower()) {
                        case 2 -> 5;
                        case 3 -> 9;
                        default -> 3;
                    };
                } else duration = 3;

                AtomicInteger ai = new AtomicInteger();
                Bukkit.getAsyncScheduler().runAtFixedRate(this.getPlugin(), task -> {
                    if (player.isGliding()) {
                        player.setVelocity(player.getLocation().getDirection().normalize().multiply(this.elytra_speed + (ai.get() / 9D)));
                        if (ai.incrementAndGet() >= duration) {
                            task.cancel();
                        }
                        return;
                    }
                    task.cancel();
                }, 50L, 200L, TimeUnit.MILLISECONDS);

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
                if (Math.random() * 100F < lossChance) {
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
        this.elytra_speed = this.getSection().getDouble("elytra_boost.speed_multiplier") * 1.5D;
        this.elytra_durability = this.getSection().getInt("elytra_boost.additional_durability_loss");

        this.trident_cooldown = this.getSection().getInt("trident_riptide.cooldown") * 20;
        this.trident_speed = this.getSection().getDouble("trident_riptide.speed_multiplier") * 0.65D;
        this.trident_durability = this.getSection().getInt("trident_riptide.additional_durability_loss");

        return true;
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }
}

