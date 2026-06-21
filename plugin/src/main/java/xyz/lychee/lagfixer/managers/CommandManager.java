package xyz.lychee.lagfixer.managers;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.commands.*;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class CommandManager extends AbstractManager {
    private static @Getter CommandManager instance;
    private final HashMap<String, Subcommand> subcommands = new HashMap<>();
    private final HashMap<String, Subcommand> subcommandsWithAliases = new HashMap<>();
    private LagFixerCommand command;

    public CommandManager(LagFixer plugin) {
        super(plugin);
        instance = this;

        this.registerSubcommands(
                new BenchmarkCommand(this),
                new ClearCommand(this),
                new MapCommand(this),
                new MenuCommand(this),
                new MonitorCommand(this),
                new PingCommand(this),
                new ReloadCommand(this),
                new FreeCommand(this)
        );
    }

    public void registerSubcommands(Subcommand... subcommands) {
        for (Subcommand subcommand : subcommands) {
            this.subcommands.put(subcommand.getName(), subcommand);
            this.subcommandsWithAliases.put(subcommand.getName(), subcommand);
            if (subcommand.getAliases() != null) {
                for (String alias : subcommand.getAliases()) {
                    this.subcommandsWithAliases.put(alias, subcommand);
                }
            }
        }
    }

    @Override
    public void load() {
        for (Subcommand subcommand : this.subcommands.values()) {
            subcommand.load();
        }

        this.command = new LagFixerCommand(this.getPlugin().getConfig().getStringList("main.command_aliases"));
        Bukkit.getCommandMap().register(this.command.getName(), this.command);
    }

    @Override
    public void disable() {
        for (Subcommand subcommand : this.subcommands.values()) {
            subcommand.unload();
        }

        if (this.command != null) {
            this.command.unregister(Bukkit.getCommandMap());
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Getter
    public abstract static class Subcommand {
        private final CommandManager commandManager;
        private final String name;
        private final String description;
        private final String[] aliases;

        public Subcommand(CommandManager commandManager, String name, String description, String... aliases) {
            this.commandManager = commandManager;
            this.name = name;
            this.description = description;
            this.aliases = aliases;
        }

        public abstract void load();

        public abstract void unload();

        public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);

        public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
            return Collections.emptyList();
        }
    }

    public class LagFixerCommand extends BukkitCommand {
        public LagFixerCommand(List<String> aliases) {
            super("lagfixer", "Main lagfixer command", "/lagfixer <" + String.join("|", subcommands.keySet()) + ">", aliases);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            if (args.length > 0) {
                Subcommand cmd = subcommandsWithAliases.get(args[0].toLowerCase());
                if (cmd != null) {
                    if (!sender.hasPermission("lagfixer.command."+cmd.getName())) {
                        Component text = Language.getMainValue("no_access", true, Placeholder.unparsed("permission", "lagfixer.command."+cmd.getName()));
                        if (text != null) {
                            sender.sendMessage(text);
                        }
                        return false;
                    }
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    return cmd.execute(sender, subArgs);
                }
            }

            if (!sender.hasPermission("lagfixer.command")) {
                Component text = Language.getMainValue("no_access", true, Placeholder.unparsed("permission", "lagfixer.command"));
                if (text != null) {
                    sender.sendMessage(text);
                }
                return false;
            }

            if (args.length > 0) {
                Subcommand cmd = subcommandsWithAliases.get(args[0].toLowerCase());
                if (cmd != null) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    cmd.execute(sender, subArgs);
                    return false;
                }
            }

            StringBuilder help = new StringBuilder("Subcommands list:\n");
            for (Subcommand subCommand : subcommands.values()) {
                help.append("&8{*} &f/lagfixer &e")
                        .append(subCommand.getName())
                        .append(" &8- &7")
                        .append(subCommand.getDescription())
                        .append("\n");
            }
            return MessageUtils.sendMessage(true, sender, help.toString());
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>(subcommandsWithAliases.keySet());
                if (!args[0].isEmpty()) {
                    completions.removeIf(str -> !str.startsWith(args[0]));
                }
                Collections.sort(completions);
                return completions;
            } else if (args.length >= 2) {
                String subCommandName = args[0].toLowerCase();
                Subcommand subCommand = subcommandsWithAliases.get(subCommandName);

                if (subCommand != null) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    List<String> tabComplete = subCommand.tabComplete(sender, subArgs);
                    if (tabComplete != null && !tabComplete.isEmpty()) {
                        return tabComplete;
                    }
                }
                return Bukkit.getOnlinePlayers().stream()
                        .map(HumanEntity::getName)
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }

            return Collections.emptyList();
        }
    }
}