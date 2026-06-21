package xyz.lychee.lagfixer.commands;

import org.apache.commons.lang3.stream.Streams;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.modules.WorldCleanerModule;
import xyz.lychee.lagfixer.objects.RegionsEntityReport;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class ClearCommand extends CommandManager.Subcommand {
    public ClearCommand(CommandManager commandManager) {
        super(commandManager, "clear", "clear entities using rules in WorldCleaner");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            MessageUtils.sendMessage(true, sender, "&7Usage: &f/lagfixer clear <items|creatures|projectiles>");
            return true;
        }

        WorldCleanerModule module = ModuleManager.getInstance().get(WorldCleanerModule.class);
        if (module == null || !module.isLoaded()) {
            MessageUtils.sendMessage(true, sender, "&7WorldCleaner module is disabled!");
            return true;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        RegionsEntityReport report = new RegionsEntityReport();
        LongAdder size = report.getEntities();

        String type = args[0].toLowerCase();
        switch (type) {
            case "items" -> {
                for (World w : module.getAllowedWorlds()) {
                    module.purgeItems(w, futures, size);
                }
            }
            case "creatures" -> {
                for (World w : module.getAllowedWorlds()) {
                    module.purgeCreatures(w, futures, size);
                }
            }
            case "projectiles" -> {
                for (World w : module.getAllowedWorlds()) {
                    module.purgeProjectiles(w, futures, size);
                }
            }
            case "all" -> {
                for (World w : module.getAllowedWorlds()) {
                    module.purgeAll(w, futures, report);
                }
            }
            default -> {
                return MessageUtils.sendMessage(true, sender, "&7Invalid clear type: &f" + type);
            }
        }
        ;

        MessageUtils.sendMessage(true, sender, "&7Asynchronous entity removal in progress...");

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(5, TimeUnit.SECONDS)
                .whenComplete((v, t) ->
                        MessageUtils.sendMessage(true, sender, "&7Successfully removed &e" + size + " &7entities!")
                );

        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return Streams.of("items", "creatures", "projectiles", "all").filter(str -> str.startsWith(args[0])).toList();
        }
        return Collections.emptyList();
    }
}