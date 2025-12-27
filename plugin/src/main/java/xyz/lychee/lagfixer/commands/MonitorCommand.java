package xyz.lychee.lagfixer.commands;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class MonitorCommand extends CommandManager.Subcommand {
    public MonitorCommand(CommandManager commandManager) {
        super(commandManager, "monitor", "check server load statistics", "tps", "mspt");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
        return MessageUtils.sendMessage(true, sender,
                "&7Command result: " +
                        "\n &8{*} &fTps: &e" + monitor.getTps() +
                        "\n &8{*} &fMspt: &e" + monitor.getMspt() +
                        "\n &8{*} &fMemory: &e" + monitor.getRamUsed() + "&8/&e" + monitor.getRamTotal() + "&8/&e" + monitor.getRamMax() + " MB" +
                        "\n &8{*} &fCpu process: &e" + monitor.getCpuProcess() + "%" +
                        "\n &8{*} &fCpu system: &e" + monitor.getCpuSystem() + "%");
    }
}