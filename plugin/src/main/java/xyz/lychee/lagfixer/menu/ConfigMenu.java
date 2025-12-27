package xyz.lychee.lagfixer.menu;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.io.File;
import java.util.*;

public class ConfigMenu extends AbstractMenu {
    private final Map<UUID, ConfigChange> playerChanges = new HashMap<>();
    private final AbstractModule module;
    private final File configFile;

    public ConfigMenu(LagFixer plugin, ConfigurationSection defSection, int size, AbstractModule module) {
        super(plugin, size, MessageUtils.fixColors(null, "&8[&e&l⚡&8] &fConfig! &8| &eLagFixer"), -1, true);
        this.module = module;
        this.configFile = new File(this.getPlugin().getDataFolder(), "modules/" + module.getName() + ".yml");

        for (int i = size - 9; i < size - 1; ++i) {
            if (this.getInv().getItem(i) != null) continue;
            this.getInv().setItem(i, getBorder());
        }
        this.getInv().setItem(size - 1, getBack());

        this.itemClickEvent(size - 5, () -> this.module.isLoaded() ? getEnabled() : getDisabled(), null);

        int slot = 0;
        for (Map.Entry<String, Object> entry : defSection.getValues(true).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection) continue;

            String key = entry.getKey();
            Object currentValue = this.module.getSection().get(key);

            ConfigChange change = new ConfigChange(this.module, key, currentValue);
            this.itemClickEvent(slot++, () -> {
                List<String> lore = new ArrayList<>();
                lore.add(MessageUtils.fixColors(null, "&7Current value:"));

                if (currentValue instanceof Collection) {
                    for (Object obj : (Collection<?>) currentValue) {
                        lore.add(MessageUtils.fixColors(null, " &8{*} &e" + obj));
                    }
                } else {
                    lore.add(MessageUtils.fixColors(null, " &8{*} &e" + currentValue));
                }

                lore.add("");
                lore.add(MessageUtils.fixColors(null, "&bRight click for default value!"));
                lore.add(MessageUtils.fixColors(null, "&aLeft click to change value!"));

                ItemStack item = this.module.getBaseSkull().clone();

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.fixColors(null, "&f&lKey: &e&l" + key));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                return item;
            }, e -> {
                HumanEntity human = e.getWhoClicked();
                if (e.isRightClick()) {
                    Object defaultValue = change.getModule().getSection().getDefaultSection().get(change.getKey());
                    MessageUtils.sendMessage(true, human,
                            "&fDefault value of &e" + change.getKey() + " &fis:\n &8{*} &e" + defaultValue);
                } else {
                    human.closeInventory();
                    playerChanges.put(human.getUniqueId(), change);
                    MessageUtils.sendMessage(true, human, "Enter new value (-cancel to cancel):");
                    if (change.getValue() instanceof Collection) {
                        MessageUtils.sendMessage(false, human, "&fExisting values will be toggled.");
                    }
                }
            });
        }
    }

    @Override
    public void update() {}

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        ConfigChange change = this.playerChanges.remove(player.getUniqueId());
        if (change == null) return;

        e.setCancelled(true);

        if (e.getMessage().equalsIgnoreCase("-cancel") || e.getMessage().equalsIgnoreCase("cancel")) {
            openModuleMenu(player, change.getModule());
            MessageUtils.sendMessage(true, player, "Config editor cancelled!");
            return;
        }

        try {
            updateConfigValue(change, e.getMessage());
            change.getModule().getConfig().save(configFile);
            change.getModule().loadAllConfig();

            Object newValue = change.getModule().getSection().get(change.getKey());
            MessageUtils.sendMessage(true, player,
                    "&fConfiguration saved!\n &8{*} &e" + change.getValue() + " &8→ &e" + newValue);

            change.getModule().getMenu().updateAll();
            openModuleMenu(player, change.getModule());

        } catch (Exception ex) {
            MessageUtils.sendMessage(true, player, "&cError saving configuration!");
        }
    }

    private void updateConfigValue(ConfigChange change, String message) {
        ConfigurationSection section = change.getModule().getSection();
        String key = change.getKey();

        if (section.isString(key)) {
            section.set(key, message);
        } else if (section.isInt(key)) {
            section.set(key, Integer.parseInt(message));
        } else if (section.isDouble(key)) {
            section.set(key, Double.parseDouble(message));
        } else if (section.isLong(key)) {
            section.set(key, Long.parseLong(message));
        } else if (section.isBoolean(key)) {
            section.set(key, Boolean.parseBoolean(message));
        } else if (section.isList(key)) {
            List<String> list = section.getStringList(key);
            if (list.contains(message)) {
                list.remove(message);
            } else {
                list.add(message);
            }
            section.set(key, list);
        }
    }

    private void openModuleMenu(Player player, AbstractModule module) {
        SupportManager.getInstance().getFork().runNow(false, player.getLocation(), () -> player.openInventory(module.getMenu().getInv()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerChanges.remove(e.getPlayer().getUniqueId());
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return;

        HumanEntity human = e.getWhoClicked();
        int slot = e.getSlot();
        int topSize = e.getView().getTopInventory().getSize();

        if (slot == topSize - 5) {
            try {
                boolean newState = !this.module.isLoaded();
                String path = this.module.getName() + ".enabled";

                if (newState) {
                    this.module.load();
                    this.module.loadAllConfig();
                    MessageUtils.sendMessage(true, human, "Enabled module &e" + this.module.getName());
                } else {
                    this.module.disable();
                    MessageUtils.sendMessage(true, human, "Disabled module &e" + this.module.getName());
                }

                this.module.setLoaded(newState);
                this.module.getConfig().set(path, newState);
                this.module.getConfig().save(this.configFile);

                this.updateAll();
            } catch (Exception ex) {
                MessageUtils.sendMessage(true, human, "Error toggling module!");
                this.getPlugin().printError(ex);
            }
        }
    }

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getModulesMenu();
    }

    @Data
    public static class ConfigChange {
        private final AbstractModule module;
        private final String key;
        private final Object value;
    }
}