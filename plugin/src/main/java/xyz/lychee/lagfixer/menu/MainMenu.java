package xyz.lychee.lagfixer.menu;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.Collections;

public class MainMenu extends AbstractMenu {

    private final ItemBuilder i1 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMyZmYyNDRkZmM5ZGQzYTJjZWY2MzExMmU3NTAyZGM2MzY3YjBkMDIxMzI5NTAzNDdiMmI0NzlhNzIzNjZkZCJ9fX0=", "&f&lConfiguration:");
    private final ItemBuilder i2 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNjNzg5ZjIzMDc5NGY5MGUzM2M0ZjlhZDAwNjk0YmMyYTJmZjVlOGI5YjM3NWRjMzUzMjQwMWIyODFmM2U1OCJ9fX0=", "&f&lServer informations:");
    private final ItemBuilder i3 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0=", "&f&lWorlds informations:");
    private final ItemBuilder i4 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ5ZjE4YzlkODVmOTJmNzJmODY0ZDY3YzEzNjdlOWE0NWRjMTBmMzcxNTQ5YzQ2YTRkNGRkOWU0ZjEzZmY0In19fQ==", "&f&lServer fork optimizer:");

    public MainMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, title, 1, true);
        this.surroundInventory();
        this.fillButtons();
        this.fillInventory();
    }

    private void fillButtons() {
        this.getInv().setItem(10, i1.build());
        this.getInv().setItem(12, i2.build());
        this.getInv().setItem(14, i3.build());
        this.getInv().setItem(16, i4.build());
    }

    private ItemBuilder skull(String textureHash, String name) {
        return ItemBuilder.createSkull(textureHash).setName(name).setLore(" &8{*} &7Loading lore...");
    }

    @Override
    public void update() {
        SupportManager support = SupportManager.getInstance();
        ModuleManager moduleManager = ModuleManager.getInstance();

        i1.setLore(
                " &8{*} &7Loaded modules: &e" + moduleManager.getModules().values().stream().filter(AbstractModule::isLoaded).count() + "&8/&e" + moduleManager.getModules().size(),
                " &8{*} &7Version: &e" + this.getPlugin().getDescription().getVersion(),
                "",
                "&eClick to modify configuration!"
        );

        AbstractMonitor monitor = support.getMonitor();
        i2.setLore(
                " &8{*} &7Tps: &e" + monitor.getTps(),
                " &8{*} &7Mspt: &e" + monitor.getMspt(),
                " &8{*} &7Memory: &e" + monitor.getRamUsed() + "&8/&e" + monitor.getRamTotal() + "&8/&e" + monitor.getRamMax() + " MB",
                " &8{*} &7Cpu process: &e" + monitor.getCpuProcess() + "&f%",
                " &8{*} &7Cpu system: &e" + monitor.getCpuSystem() + "&f%",
                "",
                "&eClick to open hardware menu!"
        );

        int chunks = 0, tiles = 0;
        AbstractSupportNms nms = support.getNms();
        for (World w : Bukkit.getWorlds()) {
            Chunk[] loaded = w.getLoadedChunks();
            chunks += loaded.length;
            for (Chunk chunk : loaded) {
                tiles += nms.getTileEntitiesCount(chunk);
            }
        }

        i3.setLore(
                " &8{*} &7Chunks: &e" + chunks,
                " &8{*} &7Entities: &e" + support.getEntities(),
                " &8{*} &7Creatures: &e" + support.getCreatures(),
                " &8{*} &7Items: &e" + support.getItems(),
                " &8{*} &7Projectiles: &e" + support.getProjectiles(),
                " &8{*} &7Vehicles: &e" + support.getVehicles(),
                " &8{*} &7Tile entities: &e" + tiles,
                " &8{*} &7Players: &e" + Bukkit.getOnlinePlayers().size() + "&8/&e" + Bukkit.getMaxPlayers(),
                "",
                "&eClick to open cleaner menu!"
        );

        i4.setLore(Collections.singletonList("&eClick to open configurator menu!"));
        this.fillButtons();
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return;

        HumanEntity human = e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == 10) {
            human.openInventory(MenuCommand.getInstance().getModulesMenu().getInv());
        } else if (slot == 12) {
            HardwareMenu menu = MenuCommand.getInstance().getHardwareMenu();
            if (menu == null) {
                MessageUtils.sendMessage(true, human, "Hardware menu is not supported. :/");
            } else {
                human.openInventory(menu.getInv());
            }
        } else {
            MessageUtils.sendMessage(true, human, "Click event will be added soon.");
        }
    }

    @Override
    public AbstractMenu previousMenu() {
        return null;
    }
}
