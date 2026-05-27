package xyz.lychee.lagfixer.utils;

import java.util.regex.Pattern;

public class MathUtils {
    public static final Pattern PATTERN = Pattern.compile("-?[0-9]+");

    public static boolean isInteger(String string) {
        return string != null && !string.isEmpty() && PATTERN.matcher(string).matches();
    }

    public static double round(double value, int decimals) {
        double p = Math.pow(10, decimals);
        return Math.round(value * p) / p;
    }
}