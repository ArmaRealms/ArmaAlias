package com.darkblade12.simplealias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Trusted configuration is MiniMessage. Legacy color codes are accepted for migration.
 * Dynamic values are inserted as components, never parsed as MiniMessage markup.
 */
public final class TextFormat {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static final Pattern COLORS = Pattern.compile(
            "(?i)(?:[&§]x(?:[&§][0-9a-f]){6}|[&§]#[0-9a-f]{6}|&#[0-9a-f]{6}|[&§][0-9a-fk-or])");
    private static final String[] NAMES = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
            "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
            "yellow", "white"
    };

    private TextFormat() {
    }

    public static Component render(String format) {
        return render(format, Map.of());
    }

    public static Component render(String format, Map<String, Component> variables) {
        TagResolver.Builder resolvers = TagResolver.builder();
        variables.forEach((key, value) -> resolvers.resolver(Placeholder.component(key, value)));
        return MINI.deserialize(toMiniMessage(format), resolvers.build());
    }

    /**
     * Formats external placeholder output using legacy colors without giving the
     * expansion control of MiniMessage click, hover or insertion tags.
     */
    public static Component external(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        Matcher matcher = COLORS.matcher(text);
        StringBuilder withSections = new StringBuilder();
        while (matcher.find()) {
            String code = matcher.group();
            if (code.length() == 2) {
                matcher.appendReplacement(withSections, Matcher.quoteReplacement("§" + code.charAt(1)));
            } else if (code.length() == 8 && code.charAt(1) == '#') {
                matcher.appendReplacement(withSections, Matcher.quoteReplacement("§#" + code.substring(2)));
            } else if (code.length() == 8 && code.charAt(0) == '&' && code.charAt(1) == '#') {
                matcher.appendReplacement(withSections, Matcher.quoteReplacement("§#" + code.substring(2)));
            } else if (code.length() == 7 && code.charAt(0) == '&') {
                matcher.appendReplacement(withSections, Matcher.quoteReplacement("§#" + code.substring(1)));
            } else if (code.length() == 14 && (code.charAt(1) == 'x' || code.charAt(1) == 'X')) {
                StringBuilder bungee = new StringBuilder("§x");
                for (int i = 3; i < code.length(); i += 2) {
                    bungee.append('§').append(code.charAt(i));
                }
                matcher.appendReplacement(withSections, Matcher.quoteReplacement(bungee.toString()));
            } else {
                matcher.appendReplacement(withSections, Matcher.quoteReplacement(code));
            }
        }
        matcher.appendTail(withSections);
        return LEGACY.deserialize(withSections.toString());
    }

    /** Only configured formatting is parsed; dynamic text must not go through here. */
    public static String toMiniMessage(String input) {
        Matcher matcher = COLORS.matcher(input);
        StringBuilder output = new StringBuilder();
        while (matcher.find()) {
            String code = matcher.group();
            String tag;
            if (code.length() == 2) {
                char color = Character.toLowerCase(code.charAt(1));
                tag = switch (color) {
                    case 'k' -> "<obfuscated>";
                    case 'l' -> "<bold>";
                    case 'm' -> "<strikethrough>";
                    case 'n' -> "<underlined>";
                    case 'o' -> "<italic>";
                    case 'r' -> "<reset>";
                    default -> "<reset><" + NAMES[Character.digit(color, 16)] + ">";
                };
            } else {
                String hex;
                if (code.length() == 14) {
                    StringBuilder rgb = new StringBuilder(6);
                    for (int i = 3; i < code.length(); i += 2) {
                        rgb.append(code.charAt(i));
                    }
                    hex = rgb.toString();
                } else {
                    hex = code.substring(code.indexOf('#') + 1);
                }
                tag = "<reset><#" + hex + ">";
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement(tag));
        }
        matcher.appendTail(output);
        return output.toString();
    }
}
