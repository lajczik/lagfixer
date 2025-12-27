package xyz.lychee.lagfixer;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ColoredLogger extends Logger {

    public ColoredLogger() {
        super(ColoredLogger.convertStringMessage("&e⚡"), null);
        this.setParent(Bukkit.getLogger());
        this.setLevel(Level.ALL);
    }

    private static String convertStringMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        StringBuilder result = new StringBuilder();
        Pattern pattern = Pattern.compile("&([0-9a-fk-or])(?!.*\u0001)");
        Matcher matcher = pattern.matcher(message);

        int lastEnd = 0;
        while (matcher.find()) {
            result.append(message, lastEnd, matcher.start());
            char codeChar = matcher.group(1).charAt(0);
            AnsiColor color = AnsiColor.getColorByCode(codeChar);
            if (color != null) {
                result.append(color.getAnsiColor());
            } else {
                result.append(matcher.group(0));
            }
            lastEnd = matcher.end();
        }

        result.append(message.substring(lastEnd));
        result.append(AnsiColor.RESET.getAnsiColor());

        return result.toString();
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        logRecord.setMessage(ColoredLogger.convertStringMessage(logRecord.getMessage()));
        super.log(logRecord);
    }

    @Getter
    private enum AnsiColor {
        BLACK('0', "\u001b[38;5;%dm", 0),
        DARK_GREEN('2', "\u001b[38;5;%dm", 2),
        DARK_RED('4', "\u001b[38;5;%dm", 1),
        GOLD('6', "\u001b[38;5;%dm", 172),
        DARK_GREY('8', "\u001b[38;5;%dm", 8),
        GREEN('a', "\u001b[38;5;%dm", 10),
        RED('c', "\u001b[38;5;%dm", 9),
        YELLOW('e', "\u001b[38;5;%dm", 11),
        DARK_BLUE('1', "\u001b[38;5;%dm", 4),
        DARK_AQUA('3', "\u001b[38;5;%dm", 30),
        DARK_PURPLE('5', "\u001b[38;5;%dm", 54),
        GRAY('7', "\u001b[38;5;%dm", 246),
        BLUE('9', "\u001b[38;5;%dm", 4),
        AQUA('b', "\u001b[38;5;%dm", 51),
        LIGHT_PURPLE('d', "\u001b[38;5;%dm", 13),
        WHITE('f', "\u001b[38;5;%dm", 15),
        STRIKETHROUGH('m', "\u001b[%dm", 9),
        ITALIC('o', "\u001b[%dm", 3),
        BOLD('l', "\u001b[%dm", 1),
        UNDERLINE('n', "\u001b[%dm", 4),
        RESET('r', "\u001b[%dm", 0);

        private static final Map<Character, AnsiColor> chars = Arrays.stream(values()).collect(Collectors.toMap(AnsiColor::getBukkitColor, Function.identity()));
        private final char bukkitColor;
        private final String ansiColor;

        AnsiColor(char bukkitColor, String pattern, int ansiCode) {
            this.bukkitColor = bukkitColor;
            this.ansiColor = String.format(pattern, ansiCode);
        }

        public static AnsiColor getColorByCode(char code) {
            return chars.get(code);
        }
    }
}

