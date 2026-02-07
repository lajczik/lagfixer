package xyz.lychee.lagfixer.menu;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModulesMenu extends AbstractMenu {
    public ModulesMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, title, 20, true);

        this.surroundInventory();
        this.fillInventory();

        List<AbstractModule> modules = new ArrayList<>(ModuleManager.getInstance().getModules().values());
        modules.sort(Comparator.comparing(AbstractModule::getName));

        int index = 0;
        for (AbstractModule module : modules) {
            int i = index++;
            int slot = i + 10 + i / 7 * 2;

            ItemStack is = module.getBaseSkull().clone();
            this.itemClickEvent(slot, () -> {
                ItemMeta meta = is.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(MessageUtils.fixColors(null, "&6&l⭐ &f&l模块: &e&l" + module.getName()));
                    ArrayList<String> lore = new ArrayList<>();

                    // 翻译状态标签
                    lore.add(MessageUtils.fixColors(null, " &8{*} &7状态: " + (module.isLoaded() ? "&a&l已启用" : "&c&l已禁用")));
                    // 翻译可自定义值标签
                    lore.add(MessageUtils.fixColors(null, " &8{*} &7可自定义值: &e" + module.getSection().getValues(true).values().stream().filter(obj -> !(obj instanceof ConfigurationSection)).count()));
                    // 翻译性能影响标签
                    lore.add(MessageUtils.fixColors(null, " &8{*} &7性能影响: " + Language.getSerializer().serialize(module.getImpact().getComponent())));
                    lore.add("");
                    // 翻译点击提示
                    lore.add(MessageUtils.fixColors(null, "&b&n点击修改配置！"));
                    lore.add("");
                    // 翻译描述标签
                    lore.add(MessageUtils.fixColors(null, "&e描述:"));

                    for (String line : module.getDescription()) {
                        StringBuilder lineBuilder = new StringBuilder(" &8{*} &7");
                        int wordCount = 0;
                        for (String word : line.split("\\s+")) {
                            if (wordCount < 5 && lineBuilder.length() < 40) {
                                lineBuilder.append(word).append(' ');
                                ++wordCount;
                                continue;
                            }
                            lore.add(MessageUtils.fixColors(null, lineBuilder.toString().trim()));
                            lineBuilder = new StringBuilder("&7").append(word).append(' ');
                            wordCount = 1;
                        }
                        if (lineBuilder.length() == 0) continue;
                        lore.add(MessageUtils.fixColors(null, lineBuilder.toString().trim()));
                    }
                    meta.setLore(lore);
                    is.setItemMeta(meta);
                }
                return is;
            }, e -> e.getWhoClicked().openInventory(module.getMenu().getInv()));
        }
    }

    @Override
    public void update() {}

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {}

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getMainMenu();
    }
}