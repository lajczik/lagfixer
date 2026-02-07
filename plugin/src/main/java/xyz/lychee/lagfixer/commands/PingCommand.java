package xyz.lychee.lagfixer.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractSupportNms;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class PingCommand extends CommandManager.Subcommand {
    public PingCommand(CommandManager commandManager) {
        // 翻译：计算玩家平均网络延迟
        super(commandManager, "ping", "计算玩家平均网络延迟");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        AbstractSupportNms nms = SupportManager.getInstance().getNms();
        if (args.length > 0) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                // 翻译：服务器上未找到该玩家
                return MessageUtils.sendMessage(true, sender, "&7服务器上未找到该玩家");
            }

            // 翻译：玩家的ping值为
            return MessageUtils.sendMessage(true, sender, "&7" + player.getDisplayName() + "的网络延迟为 &e" + nms.getPlayerPing(player) + "&7ms");
        }

        double averagePing = Bukkit.getOnlinePlayers()
                .stream()
                .mapToInt(nms::getPlayerPing)
                .average()
                .orElse(-1D);
        // 翻译：玩家平均网络延迟
        return MessageUtils.sendMessage(true, sender, "&7玩家平均网络延迟: &e" + averagePing);
    }
}