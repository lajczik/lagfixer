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

    // 翻译：配置
    private final ItemBuilder i1 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMyZmYyNDRkZmM5ZGQzYTJjZWY2MzExMmU3NTAyZGM2MzY3YjBkMDIxMzI5NTAzNDdiMmI0NzlhNzIzNjZkZCJ9fX0=", "&f&l配置:");
    // 翻译：服务器信息
    private final ItemBuilder i2 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNjNzg5ZjIzMDc5NGY5MGUzM2M0ZjlhZDAwNjk0YmMyYTJmZjVlOGI5YjM3NWRjMzUzMjQwMWIyODFmM2U1OCJ9fX0=", "&f&l服务器信息:");
    // 翻译：世界信息
    private final ItemBuilder i3 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0=", "&f&l世界信息:");
    // 翻译：服务端分支优化器
    private final ItemBuilder i4 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ5ZjE4YzlkODVmOTJmNzJmODY0ZDY3YzEzNjdlOWE0NWRjMTBmMzcxNTQ5YzQ2YTRkNGRkOWU0ZjEzZmY0In19fQ==", "&f&l服务端分支优化器:");

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

    // 翻译：正在加载描述...
    private ItemBuilder skull(String textureHash, String name) {
        return ItemBuilder.createSkull(textureHash).setName(name).setLore(" &8{*} &7正在加载描述...");
    }

    @Override
    public void update() {
        SupportManager support = SupportManager.getInstance();
        ModuleManager moduleManager = ModuleManager.getInstance();

        // 翻译i1的描述
        i1.setLore(
                " &8{*} &7已加载模块: &e" + moduleManager.getModules().values().stream().filter(AbstractModule::isLoaded).count() + "&8/&e" + moduleManager.getModules().size(),
                " &8{*} &7版本: &e" + this.getPlugin().getDescription().getVersion(),
                "",
                "&e点击修改配置！"
        );

        AbstractMonitor monitor = support.getMonitor();
        // 翻译i2的描述（保留TPS/MSPT通用缩写）
        i2.setLore(
                " &8{*} &7TPS: &e" + monitor.getTps(),
                " &8{*} &7MSPT: &e" + monitor.getMspt(),
                " &8{*} &7内存: &e" + monitor.getRamUsed() + "&8/&e" + monitor.getRamTotal() + "&8/&e" + monitor.getRamMax() + " MB",
                " &8{*} &7CPU进程占用: &e" + monitor.getCpuProcess() + "&f%",
                " &8{*} &7CPU系统占用: &e" + monitor.getCpuSystem() + "&f%",
                "",
                "&e点击打开硬件菜单！"
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

        // 翻译i3的描述
        i3.setLore(
                " &8{*} &7区块: &e" + chunks,
                " &8{*} &7实体: &e" + support.getEntities(),
                " &8{*} &7生物: &e" + support.getCreatures(),
                " &8{*} &7物品: &e" + support.getItems(),
                " &8{*} &7抛射物: &e" + support.getProjectiles(),
                " &8{*} &7载具: &e" + support.getVehicles(),
                " &8{*} &7方块实体: &e" + tiles,
                " &8{*} &7玩家: &e" + Bukkit.getOnlinePlayers().size() + "&8/&e" + Bukkit.getMaxPlayers(),
                "",
                "&e点击打开清理菜单！"
        );

        // 翻译i4的描述
        i4.setLore(Collections.singletonList("&e点击打开配置器菜单！"));
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
                // 翻译：硬件菜单不支持。:/
                MessageUtils.sendMessage(true, human, "硬件菜单不支持。:/");
            } else {
                human.openInventory(menu.getInv());
            }
        } else {
            // 翻译：点击事件将很快添加。
            MessageUtils.sendMessage(true, human, "点击事件将很快添加。");
        }
    }

    @Override
    public AbstractMenu previousMenu() {
        return null;
    }
}