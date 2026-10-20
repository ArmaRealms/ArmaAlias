package com.darkblade12.simplealias.util;

import com.darkblade12.simplealias.util.JavaEscapes;
import com.darkblade12.simplealias.util.ConvertUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MessageUtils {
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§([0-9a-fk-orx])", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARGUMENT_COLOR_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");

    private MessageUtils() {
    }

    public static String formatName(Enum<?> enumObj, boolean capitalize, String delimiter) {
        String[] split = enumObj.name().toLowerCase().split("_");
        return Arrays.stream(split).map(s -> capitalize && !s.isEmpty() ? Character.toTitleCase(s.charAt(0)) + s.substring(1) : s).collect(Collectors.joining(delimiter));
    }

    public static String formatName(Enum<?> enumObj, boolean capitalize) {
        return formatName(enumObj, capitalize, " ");
    }

    public static String formatName(Enum<?> enumObj) {
        return formatName(enumObj, true);
    }

    public static boolean isBlank(String message) {
        return message == null || message.isEmpty() || message.matches("\\s+");
    }

    public static String translateMessage(String message, char colorChar) {
        return isBlank(message) ? message : JavaEscapes.unescapeJava(message);
    }

    public static String translateMessage(String message) {
        return translateMessage(message, '&');
    }

    public static String reverseTranslateMessage(String message, char colorChar) {
        return isBlank(message) ? message : JavaEscapes.escapeJava(COLOR_CODE_PATTERN.matcher(message).replaceAll(colorChar + "$1"));
    }

    public static String reverseTranslateMessage(String message) {
        return reverseTranslateMessage(message, '&');
    }

    public static String[] translateArguments(String[] args, char colorChar) {
        return Arrays.stream(args)
                .map(s -> colorChar == '&'
                        ? ARGUMENT_COLOR_PATTERN.matcher(s).replaceAll("§$1")
                        : Pattern.compile("(?i)" + Pattern.quote(String.valueOf(colorChar)) + "([0-9a-fk-or])")
                                .matcher(s).replaceAll("§$1"))
                .toArray(String[]::new);
    }

    public static String[] translateArguments(String[] args) {
        return translateArguments(args, '&');
    }

    public static String formatDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration cannot be lower than 0.");
        }

        TimeUnit[] units = { TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS };
        StringBuilder builder = new StringBuilder();

        for (TimeUnit unit : units) {
            long duration = unit.convert(millis, TimeUnit.MILLISECONDS);
            if (duration <= 0) {
                continue;
            }
            millis -= TimeUnit.MILLISECONDS.convert(duration, unit);

            if (builder.length() > 0) {
                builder.append(", ");
            }

            String name = unit.name().toLowerCase();
            if (duration == 1) {
                name = name.substring(0, name.length() - 1);
            }
            builder.append(duration).append(" ").append(name);
        }

        int index = builder.lastIndexOf(", ");
        if (index != -1) {
            builder.replace(index, index + 2, " and ");
        }

        return builder.length() > 0 ? builder.toString() : "<1 second";
    }
}
