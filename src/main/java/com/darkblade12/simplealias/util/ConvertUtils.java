package com.darkblade12.simplealias.util;

import java.util.regex.Pattern;

public final class ConvertUtils {
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMPTY_PATTERN = Pattern.compile("<(empty|null)>|\\s+", Pattern.CASE_INSENSITIVE);
    public static final Pattern SEPARATOR_PATTERN = Pattern.compile("\\s?,\\s?");

    private ConvertUtils() {
    }

    public static boolean isEmpty(String value) {
        return value.isEmpty() || EMPTY_PATTERN.matcher(value).matches();
    }

    public static boolean convertToBoolean(String value) {
        if (!BOOLEAN_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid boolean value.");
        }

        return Boolean.parseBoolean(value);
    }

    /** Equivalent to legacy removeStart: removes a prefix once, if present. */
    public static String removeStart(String value, String prefix) {
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    public static String join(String[] values, String separator) {
        return String.join(separator, values);
    }

    public static String join(String[] values, char separator) {
        return String.join(String.valueOf(separator), values);
    }

    public static String join(String[] values, char separator, int start, int end) {
        return String.join(String.valueOf(separator), java.util.Arrays.copyOfRange(values, start, end));
    }

    public static String join(Iterable<String> values, String separator) {
        java.util.StringJoiner joiner = new java.util.StringJoiner(separator);
        for (String value : values) {
            joiner.add(value == null ? "" : value);
        }
        return joiner.toString();
    }

    public static String[] split(String value) {
        return SEPARATOR_PATTERN.split(value);
    }
}
