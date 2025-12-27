package xyz.lychee.lagfixer.menu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.TimingUtil;

import java.io.*;
import java.util.List;

public class HardwareMenu extends AbstractMenu {
    private final ItemBuilder i1 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTJiMTcxMmI5MDdjZTZiMTQwMmVhYWMyOGVjMjRhNGQ5NTU2OGY0YWI4N2U1OTc5ODBjMTViMjJiYmJkN2E1In19fQ==", "&b\uD83C\uDF10 &f&lNetwork:");
    private final ItemBuilder i2 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDU2Yzk0NjE5MDMxMjMxNjhjZTY2N2VhZDdlYTU2YTUxNjEzMDk3MDQ5YmE2NDc4MzJiMzcyMmFmZmJlYjYzNiJ9fX0=", "&9\uD83D\uDCBE &f&lProcessor:");
    private final ItemBuilder i3 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYwYjAwNGYzNjBlMjg4NTVjY2YxMjM1YzJiZGVhMGEyOTk3YjBiYzAzMjU4ZTJkYzI0YWI4YTI1NzBhZWE2In19fQ==", "&a\uD83C\uDF9E &f&lMemory:");
    private final ItemBuilder i4 = this.skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg5MWFmZDM5ZTJlNjczOGJjNmE4Yzg4YzI0OWZkYmNmNGE0NWM0YTI0MjQ3ZjFkMTBiYWUwYzY0ZDk5OTFlMSJ9fX0=", "&e\uD83D\uDCC2 &f&lDisk Storage:");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final SystemInfo si;
    private final HardwareAbstractionLayer hal;
    private HardwareData hardwareData;
    private long lastNetworkUpdate = 0;
    private long prevBytesSent = 0;
    private long prevBytesRecv = 0;

    public HardwareMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, title, 3, true);
        this.si = new SystemInfo();
        this.hal = this.si.getHardware();

        this.loadOrCreateHardwareData(plugin);

        this.surroundInventory();
        this.fillButtons();
        this.getInv().setItem(11,
                ItemBuilder.createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWY0NDZhOGY5Mjg0YzYyY2Y4ZDQ5MWZiZGIzMzhmZDM5ZWJiZWJlMzVlOTU5YzJmYzRmNzg2YzY3NTIyZWZiIn19fQ==")
                        .setName("&4⚠ &c&lPERFORMANCE WARNING!")
                        .setLore(
                                " &8{*} &7Heavy impact on servers with &c<4 &7CPU threads",
                                " &8{*} &7Don't leave this GUI open indefinitely",
                                " ",
                                "&a✔ Asynchronous updates &8(&7non-blocking&8)",
                                "&a✔ Auto-closes after inactivity"
                        ).build()
        );
        this.getInv().setItem(size - 1, ConfigMenu.getBack());
        this.fillInventory();
    }

    private void loadOrCreateHardwareData(LagFixer plugin) {
        File dataFile = new File(plugin.getDataFolder(), "hardware_data.json");

        if (dataFile.exists()) {
            try (Reader reader = new FileReader(dataFile)) {
                this.hardwareData = gson.fromJson(reader, HardwareData.class);
                return;
            } catch (IOException ignored) {}
        }
        collectAndSaveHardwareData(dataFile);
    }

    private void collectAndSaveHardwareData(File dataFile) {
        SystemInfo tempSi = new SystemInfo();
        HardwareAbstractionLayer tempHal = tempSi.getHardware();
        CentralProcessor cpu = tempHal.getProcessor();
        GlobalMemory memory = tempHal.getMemory();
        List<HWDiskStore> disks = tempHal.getDiskStores();

        this.hardwareData = new HardwareData();
        this.hardwareData.setCpuName(cpu.getProcessorIdentifier().getName());
        this.hardwareData.setCpuMicroarchitecture(cpu.getProcessorIdentifier().getMicroarchitecture());
        this.hardwareData.setCpuVendorFreq(cpu.getProcessorIdentifier().getVendorFreq());
        this.hardwareData.setLogicalCores(cpu.getLogicalProcessorCount());
        this.hardwareData.setPhysicalCores(cpu.getPhysicalProcessorCount());
        this.hardwareData.setTotalMemory(memory.getTotal());
        this.hardwareData.setPageSize(memory.getPageSize());
        this.hardwareData.setMemoryType(memory.getPhysicalMemory().isEmpty() ? "Unknown" : memory.getPhysicalMemory().get(0).getMemoryType());
        this.hardwareData.setDiskCount(disks.size());
        this.hardwareData.setPartitionCount(disks.stream().mapToInt(d -> d.getPartitions().size()).sum());
        this.hardwareData.setNetworkInterfaceCount(tempHal.getNetworkIFs().size());

        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(this.hardwareData, writer);
        } catch (IOException ignored) {}
    }

    private void fillButtons() {
        this.getInv().setItem(12, this.i1.build());
        this.getInv().setItem(13, this.i2.build());
        this.getInv().setItem(14, this.i3.build());
        this.getInv().setItem(15, this.i4.build());
    }

    private ItemBuilder skull(String textureHash, String name) {
        return ItemBuilder.createSkull(textureHash).setName(name).setLore(" &8{*} &7Loading lore...");
    }

    @Override
    public void update() {
        TimingUtil timing = TimingUtil.startNew();

        try {
            List<NetworkIF> networks = hal.getNetworkIFs();
            long bytesSent = 0;
            long bytesRecv = 0;
            for (NetworkIF net : networks) {
                net.updateAttributes();
                bytesSent += net.getBytesSent();
                bytesRecv += net.getBytesRecv();
            }

            this.i1.setLore(
                    " &8{*} &7Interfaces: &f" + this.hardwareData.getNetworkInterfaceCount(),
                    " &8{*} &7Sent: &f" + FormatUtil.formatBytes(bytesSent),
                    " &8{*} &7Received: &f" + FormatUtil.formatBytes(bytesRecv),
                    " &8{*} &7Throughput: &f" + calculateNetworkSpeed(bytesSent, bytesRecv),
                    "&7Network data updated in &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        }
        catch (Throwable t) {
            this.i1.setLore(
                    " &8{*} &7An error occurred while",
                    "&7retrieving network information :("
            );
        }

        timing.start();
        try {
            this.i2.setLore(
                    " &8{*} &7Model: &f" + this.hardwareData.getCpuName(),
                    " &8{*} &7Logical cores: &f" + this.hardwareData.getLogicalCores(),
                    " &8{*} &7Physical cores: &f" + this.hardwareData.getPhysicalCores(),
                    " &8{*} &7Microarchitecture: &f" + this.hardwareData.getCpuMicroarchitecture(),
                    " &8{*} &7Frequency: &f" + FormatUtil.formatHertz(this.hardwareData.getCpuVendorFreq()),
                    "&7Cpu data updated in &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        }
        catch (Throwable t) {
            this.i2.setLore(
                    " &8{*} &7An error occurred while",
                    "&7retrieving processer information :("
            );
        }

        timing.start();
        try {
            GlobalMemory memory = hal.getMemory();
            long usedMem = this.hardwareData.getTotalMemory() - memory.getAvailable();
            double memPercent = (usedMem * 100.0) / this.hardwareData.getTotalMemory();
            VirtualMemory swap = memory.getVirtualMemory();

            this.i3.setLore(
                    " &8{*} &7Total RAM: &f" + FormatUtil.formatBytesDecimal(this.hardwareData.getTotalMemory()),
                    " &8{*} &7Used RAM: &f" + FormatUtil.formatBytesDecimal(usedMem) +
                            String.format(" (&f%.1f%%&7)", memPercent),
                    " &8{*} &7Available RAM: &f" + FormatUtil.formatBytesDecimal(memory.getAvailable()),
                    " &8{*} &7Page Size: &f" + FormatUtil.formatBytesDecimal(this.hardwareData.getPageSize()),
                    " &8{*} &7Memory Type: &f" + this.hardwareData.getMemoryType(),
                    " &8{*} &7Swap Total: &f" + FormatUtil.formatBytesDecimal(swap.getSwapTotal()),
                    " &8{*} &7Swap Used: &f" + FormatUtil.formatBytesDecimal(swap.getSwapUsed()),
                    "&7Memory data updated in &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        }
        catch (Throwable t) {
            this.i3.setLore(
                    " &8{*} &7An error occurred while",
                    "&7retrieving memory information :("
            );
        }

        timing.start();
        try {
            long totalReadBytes = 0;
            long totalWriteBytes = 0;
            long readOps = 0;
            long writeOps = 0;
            long queueLength = 0;

            for (HWDiskStore disk : hal.getDiskStores()) {
                disk.updateAttributes();
                totalReadBytes += disk.getReadBytes();
                totalWriteBytes += disk.getWriteBytes();
                readOps += disk.getReads();
                writeOps += disk.getWrites();
                queueLength += disk.getCurrentQueueLength();
            }

            long totalDiskSpace = 0;
            long usedDiskSpace = 0;
            for (OSFileStore fs : si.getOperatingSystem().getFileSystem().getFileStores()) {
                totalDiskSpace += fs.getTotalSpace();
                usedDiskSpace += fs.getTotalSpace() - fs.getUsableSpace();
            }
            double diskUsagePercent = totalDiskSpace > 0 ?
                    (usedDiskSpace * 100.0) / totalDiskSpace : 0;

            this.i4.setLore(
                    " &8{*} &7Physical Disks: &f" + this.hardwareData.getDiskCount(),
                    " &8{*} &7Partitions: &f" + this.hardwareData.getPartitionCount(),
                    " &8{*} &7Total Capacity: &f" + FormatUtil.formatBytes(totalDiskSpace),
                    " &8{*} &7Used Space: &f" + FormatUtil.formatBytes(usedDiskSpace) +
                            String.format(" (&f%.1f%%&7)", diskUsagePercent),
                    " &8{*} &7Read Data: &f" + FormatUtil.formatBytes(totalReadBytes) +
                            " (&f" + readOps + " ops&7)",
                    " &8{*} &7Written Data: &f" + FormatUtil.formatBytes(totalWriteBytes) +
                            " (&f" + writeOps + " ops&7)",
                    " &8{*} &7Disk Queues: &f" + queueLength,
                    "&7Storage data updated in &e" + timing.stop().getExecutingTime() + "&7ms"
            );
        }
        catch (Throwable t) {
            this.i4.setLore(
                    " &8{*} &7An error occurred while",
                    "&7retrieving storage information :("
            );
        }

        this.fillButtons();
    }

    private String calculateNetworkSpeed(long currentSent, long currentRecv) {
        long timeDiff = System.currentTimeMillis() - lastNetworkUpdate;
        if (lastNetworkUpdate == 0 || timeDiff == 0) {
            lastNetworkUpdate = System.currentTimeMillis();
            prevBytesSent = currentSent;
            prevBytesRecv = currentRecv;
            return "Calculating...";
        }

        double sentSpeed = (currentSent - prevBytesSent) / (timeDiff / 1000.0);
        double recvSpeed = (currentRecv - prevBytesRecv) / (timeDiff / 1000.0);

        lastNetworkUpdate = System.currentTimeMillis();
        prevBytesSent = currentSent;
        prevBytesRecv = currentRecv;

        return String.format("&a▲&f%s/s &c▼&f%s/s", FormatUtil.formatBytesDecimal((long) sentSpeed), FormatUtil.formatBytesDecimal((long) recvSpeed));
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {}

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getMainMenu();
    }

    @Getter
    @Setter
    private static class HardwareData {
        String cpuName;
        String cpuMicroarchitecture;
        long cpuVendorFreq;
        int logicalCores;
        int physicalCores;
        long totalMemory;
        long pageSize;
        String memoryType;
        int diskCount;
        int partitionCount;
        int networkInterfaceCount;
    }
}