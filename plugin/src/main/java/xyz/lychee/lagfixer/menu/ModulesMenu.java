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
                    meta.setDisplayName(MessageUtils.fixColors(null, "&6&l⭐ &f&lModule: &e&l" + module.getName()));
                    ArrayList<String> lore = new ArrayList<>();

                    lore.add(MessageUtils.fixColors(null, " &8{*} &7Status: " + (module.isLoaded() ? "&a&lENABLED" : "&c&lDISABLED")));
                    lore.add(MessageUtils.fixColors(null, " &8{*} &7Customizable values: &e" + module.getSection().getValues(true).values().stream().filter(obj -> !(obj instanceof ConfigurationSection)).count()));
                    lore.add(MessageUtils.fixColors(null, " &8{*} &7Performance: " + Language.getSerializer().serialize(module.getImpact().getComponent())));
                    lore.add("");
                    lore.add(MessageUtils.fixColors(null, "&b&nClick to modify configuration!"));
                    lore.add("");
                    lore.add(MessageUtils.fixColors(null, "&eDescription:"));

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

