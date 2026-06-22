package xyz.lychee.lagfixer.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.hooks.PlaceholderAPIHook;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.HookManager;

import java.util.Map;

public class MessageUtils {
    private static final Map<String, String> REPLACEMENTS;

    static {
        REPLACEMENTS = Map.of(
                "{*}", "•",
                "{>>}", "»",
                "{<<}", "«"
        );
    }

    public static String fixColors(@Nullable CommandSender sender, String message) {
        if (message == null) return "";

        String processed = applyPapi(sender, applyReplacements(message));
        return ChatColor.translateAlternateColorCodes('&', processed);
    }

    public static TextComponent colors(@Nullable CommandSender sender, String message) {
        if (message == null || message.isEmpty()) return Component.empty();

        String processed = applyPapi(sender, applyReplacements(message));
        return Language.getSerializer().deserialize(processed);
    }

    private static String applyReplacements(String text) {
        for (Map.Entry<String, String> entry : REPLACEMENTS.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

    private static String applyPapi(@Nullable CommandSender sender, String text) {
        if (sender instanceof Player) {
            PlaceholderAPIHook papi = HookManager.getInstance().getHook(PlaceholderAPIHook.class);
            if (papi != null) {
                return papi.applyPlaceholders((Player) sender, text);
            }
        }
        return text;
    }

    public static boolean sendMessage(boolean prefix, CommandSender sender, String message) {
        Component text = colors(sender, "&f" + message);

        LagFixer.getInstance()
                .getAudiences()
                .sender(sender)
                .sendMessage(prefix ?
                        Component.empty().append(ConfigManager.getInstance().getPrefix()).append(text) : text
                );

        return false;
    }
}

