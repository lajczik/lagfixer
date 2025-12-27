package xyz.lychee.lagfixer.utils;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import xyz.lychee.lagfixer.managers.SupportManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ItemBuilder {
    private final ItemStack item;

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

    public ItemBuilder clone() {
        try {
            return (ItemBuilder) super.clone();
        } catch (CloneNotSupportedException ignored) {
            return new ItemBuilder(this.item.clone());
        }
    }

    public ItemMeta getMeta() {
        return this.item.getItemMeta();
    }

    public ItemBuilder setName(String name) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        meta.setDisplayName(MessageUtils.fixColors(null, name));
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setAmount(int i) {
        this.item.setAmount(i);
        return this;
    }

    public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
        this.item.addUnsafeEnchantment(ench, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment ench) {
        this.item.removeEnchantment(ench);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        SkullMeta meta = (SkullMeta) this.item.getItemMeta();
        meta.setOwner(owner);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment ench, int level) {
        ItemMeta meta = this.item.getItemMeta();
        meta.addEnchant(ench, level, true);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        this.item.addEnchantments(enchantments);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        meta.setLore(Arrays.stream(lore).map(str -> MessageUtils.fixColors(null, str)).collect(Collectors.toList()));
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        meta.setLore(lore.stream().map(str -> MessageUtils.fixColors(null, str)).collect(Collectors.toList()));
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeLoreLine(String line) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        ArrayList lore = new ArrayList(meta.getLore());
        if (!lore.contains(line)) {
            return this;
        }
        lore.remove(line);
        meta.setLore(lore);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        ArrayList lore = new ArrayList(meta.getLore());
        if (index < 0 || index > lore.size()) {
            return this;
        }
        lore.remove(index);
        meta.setLore(lore);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        if (meta.hasLore()) {
            lore.addAll(meta.getLore());
        }
        lore.add(line);
        meta.setLore(lore);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLoreLine(String line, int pos) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>(meta.getLore());
        lore.set(pos, line);
        meta.setLore(lore);
        this.item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        this.item.setType(material);
        return this;
    }

    public ItemStack build() {
        return this.item;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (this.item.getType() == Material.AIR) {
            return this;
        }
        ItemMeta meta = this.item.getItemMeta();
        if (glow) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        } else {
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.removeEnchant(Enchantment.DURABILITY);
        }
        this.item.setItemMeta(meta);
        return this;
    }

}

