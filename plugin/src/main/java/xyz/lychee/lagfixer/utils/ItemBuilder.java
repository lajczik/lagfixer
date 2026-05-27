package xyz.lychee.lagfixer.utils;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.*;

@Getter
public class ItemBuilder {
    private final ItemStack item;
    private String displayName = null;
    private List<String> lore = new ArrayList<>();
    private boolean glow = false;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();

    public ItemBuilder(Material m) {
        this(m, 1);
    }

    public ItemBuilder(ItemStack is) {
        this.item = is;
    }

    public ItemBuilder(Material m, int amount) {
        this.item = new ItemStack(m, amount);
    }

    public static ItemBuilder createSkull(String base64) {
        return new ItemBuilder(SupportManager.getInstance().getNms().createSkull(base64));
    }

    public ItemBuilder copy() {
        ItemBuilder item = new ItemBuilder(this.item.clone());

        item.displayName = this.displayName;
        item.lore = this.lore;
        item.glow = this.glow;
        item.enchantments = this.enchantments;

        return item;
    }

    public ItemMeta getMeta() {
        return this.item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        this.displayName = MessageUtils.fixColors(null, name);
        return this;
    }

    public ItemBuilder setAmount(int i) {
        this.item.setAmount(i);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        this.enchantments.remove(enchantment);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        SkullMeta meta = (SkullMeta) this.item.getItemMeta();
        meta.setOwner(owner);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments.putAll(enchantments);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        this.lore = Arrays.stream(lore).map(str -> MessageUtils.fixColors(null, str)).toList();
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore.stream().map(str -> MessageUtils.fixColors(null, str)).toList();
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        this.item.setType(material);
        return this;
    }

    public ItemStack build() {
        if (this.item == null || this.item.getType() == Material.AIR)
            return this.item;

        ItemMeta meta = this.item.getItemMeta();
        if (meta == null)
            return this.item;

        if (this.glow) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        if (this.displayName != null) {
            meta.setDisplayName(this.displayName);
        }

        if (this.lore != null) {
            meta.setLore(this.lore);
        }

        if (this.enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : this.enchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        this.item.setItemMeta(meta);
        return this.item;
    }

    public ItemBuilder setGlow(boolean glow) {
        this.glow = glow;
        return this;
    }
}

