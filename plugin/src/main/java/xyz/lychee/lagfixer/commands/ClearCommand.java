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
import java.util.stream.Collectors;

public class ClearCommand extends CommandManager.Subcommand {
    public ClearCommand(CommandManager commandManager) {
        // 翻译：使用WorldCleaner中的规则清理实体
        super(commandManager, "clear", "使用WorldCleaner中的规则清理实体");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) {
            // 翻译：用法提示
            MessageUtils.sendMessage(true, sender, "&7用法: &f/lagfixer clear <items|creatures|projectiles>");
            return true;
        }

        WorldCleanerModule module = ModuleManager.getInstance().get(WorldCleanerModule.class);
        if (module == null || !module.isLoaded()) {
            // 翻译：WorldCleaner模块已禁用
            MessageUtils.sendMessage(true, sender, "&7WorldCleaner模块已禁用！");
            return true;
        }

        AtomicInteger ai = new AtomicInteger();

        String type = args[0].toLowerCase();
        switch (type) {
            case "items":
                module.getAllowedWorldsStream()
                        .flatMap(w -> w.getEntitiesByClass(Item.class).stream())
                        .filter(module::clearItem)
                        .forEach(ent -> {
                            ent.remove();
                            ai.incrementAndGet();
                        });

                // 翻译：成功移除物品
                return MessageUtils.sendMessage(true, sender, "&7成功移除 &e" + ai.get() + " &7个物品。");
            case "creatures":
                module.getAllowedWorldsStream()
                        .flatMap(w -> w.getEntitiesByClass(Mob.class).stream())
                        .filter(module::clearCreature)
                        .forEach(ent -> {
                            ent.remove();
                            ai.incrementAndGet();
                        });

                // 翻译：成功移除生物
                return MessageUtils.sendMessage(true, sender, "&7成功移除 &e" + ai.get() + " &7个生物。");
            case "projectiles":
                module.getAllowedWorldsStream()
                        .flatMap(w -> w.getEntitiesByClass(Projectile.class).stream())
                        .filter(module::clearProjectile)
                        .forEach(ent -> {
                            ent.remove();
                            ai.incrementAndGet();
                        });

                // 翻译：成功移除抛射物
                return MessageUtils.sendMessage(true, sender, "&7成功移除 &e" + ai.get() + " &7个抛射物。");
            default:
                // 翻译：无效的清理类型
                return MessageUtils.sendMessage(true, sender, "&7无效的清理类型: &f" + type);
        }
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            return Streams.of("items", "creatures", "projectiles").filter(str -> str.startsWith(args[0])).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}