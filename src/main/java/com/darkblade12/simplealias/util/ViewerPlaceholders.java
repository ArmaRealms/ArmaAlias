package com.darkblade12.simplealias.util;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Explicit viewer-scoped PlaceholderAPI tokens: {viewer:player_name}.
 * Keeping these separate from %player_name% prevents actor-context parsing
 * from consuming placeholders intended for a recipient.
 */
public final class ViewerPlaceholders {
    private static final Pattern TOKEN = Pattern.compile("\\{viewer:([a-zA-Z0-9_]+)\\}");

    private ViewerPlaceholders() {
    }

    public static boolean hasViewerTokens(String text) {
        return TOKEN.matcher(text).find();
    }

    public static String replace(String text, Function<String, String> resolver) {
        Matcher matcher = TOKEN.matcher(text);
        StringBuilder output = new StringBuilder(text.length());
        while (matcher.find()) {
            String value = resolver.apply(matcher.group(1));
            matcher.appendReplacement(output, Matcher.quoteReplacement(
                    value == null ? matcher.group() : value));
        }
        matcher.appendTail(output);
        return output.toString();
    }
}
