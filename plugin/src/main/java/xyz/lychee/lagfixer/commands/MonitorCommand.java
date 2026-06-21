package xyz.lychee.lagfixer.commands;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ResourceMonitor;
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
<<<<<<< HEAD
        ResourceMonitor resourceMonitor = SupportManager.getInstance().getResourceMonitor();
=======
        ResourceMonitor monitor = SupportManager.getInstance().getResourceMonitor();
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        return MessageUtils.sendMessage(true, sender,
                "&7Command result: " +
                        "\n &8{*} &fTps: &e" + resourceMonitor.getTps() +
                        "\n &8{*} &fMspt: &e" + resourceMonitor.getMspt() +
                        "\n &8{*} &fMemory: &e" + resourceMonitor.getRamUsed() + "&8/&e" + resourceMonitor.getRamTotal() + "&8/&e" + resourceMonitor.getRamMax() + " MB" +
                        "\n &8{*} &fCpu process: &e" + resourceMonitor.getCpuProcess() + "%" +
                        "\n &8{*} &fCpu system: &e" + resourceMonitor.getCpuSystem() + "%");
    }
}