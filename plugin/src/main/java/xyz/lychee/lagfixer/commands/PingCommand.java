package xyz.lychee.lagfixer.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
<<<<<<< HEAD
=======
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.ISupportNms;
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
import xyz.lychee.lagfixer.utils.MessageUtils;

public class PingCommand extends CommandManager.Subcommand {
    public PingCommand(CommandManager commandManager) {
        super(commandManager, "ping", "calculate average players ping");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
<<<<<<< HEAD
=======
        ISupportNms nms = SupportManager.getInstance().getNms();
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        if (args.length > 0) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                return MessageUtils.sendMessage(true, sender, "&7Player not found on the server");
            }

            return MessageUtils.sendMessage(true, sender, "&7" + player.getDisplayName() + "'s ping is &e" + player.getPing() + "&7ms");
        }

        double averagePing = Bukkit.getOnlinePlayers()
                .stream()
                .mapToInt(Player::getPing)
                .average()
                .orElse(-1D);
        return MessageUtils.sendMessage(true, sender, "&7Average players ping: &e" + averagePing);
    }
}