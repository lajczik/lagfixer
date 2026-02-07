package xyz.lychee.lagfixer.commands;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class MonitorCommand extends CommandManager.Subcommand {
    public MonitorCommand(CommandManager commandManager) {
        super(commandManager, "monitor", "检查服务器负载统计", "tps", "mspt");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
        return MessageUtils.sendMessage(true, sender,
                "&7命令结果：" +
                        "\n &8{*} &fTPS: &e" + monitor.getTps() +
                        "\n &8{*} &fMSPT: &e" + monitor.getMspt() +
                        "\n &8{*} &f内存: &e" + monitor.getRamUsed() + "&8/&e" + monitor.getRamTotal() + "&8/&e" + monitor.getRamMax() + " MB" +
                        "\n &8{*} &fCPU进程: &e" + monitor.getCpuProcess() + "%" +
                        "\n &8{*} &fCPU系统: &e" + monitor.getCpuSystem() + "%");
    }
}