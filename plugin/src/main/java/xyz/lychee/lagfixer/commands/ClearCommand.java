package xyz.lychee.lagfixer.commands;

import org.apache.commons.lang3.stream.Streams;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.modules.WorldCleanerModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClearCommand extends CommandManager.Subcommand {
    public ClearCommand(CommandManager commandManager) {
        super(commandManager, "clear", "clear entities using rules in WorldCleaner");
    }

    @Override
    public void load() {
    }

    @Override
    public void unload() {
    }

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

        AtomicInteger ai = new AtomicInteger();

        String type = args[0].toLowerCase();
        return switch (type) {
            case "items" -> {
                module.getAllowedWorlds()
                        .stream()
                        .flatMap(w -> w.getEntitiesByClass(Item.class).stream())
                        .filter(module::clearItem)
                        .forEach(ent -> {
                            ent.remove();
                            ai.incrementAndGet();
                        });

                yield MessageUtils.sendMessage(true, sender, "&7Successfully removed &e" + ai.get() + " &7items.");
            }
            case "creatures" -> {
                module.getAllowedWorlds()
                        .stream()
                        .flatMap(w -> w.getEntitiesByClass(Mob.class).stream())
                        .filter(module::clearCreature)
                        .forEach(ent -> {
                            ent.remove();
                            ai.incrementAndGet();
                        });

                yield MessageUtils.sendMessage(true, sender, "&7Successfully removed &e" + ai.get() + " &7creatures.");
            }
            case "projectiles" -> {
                module.getAllowedWorlds()
                        .stream()
                        .flatMap(w -> w.getEntitiesByClass(Projectile.class).stream())
                        .filter(module::clearProjectile)
                        .forEach(ent -> {
                            ent.remove();
                            ai.incrementAndGet();
                        });

                yield MessageUtils.sendMessage(true, sender, "&7Successfully removed &e" + ai.get() + " &7projectiles.");
            }
            default -> MessageUtils.sendMessage(true, sender, "&7Invalid clear type: &f" + type);
        };
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return Streams.of("items", "creatures", "projectiles").filter(str -> str.startsWith(args[0])).toList();
        }
        return Collections.emptyList();
    }
}