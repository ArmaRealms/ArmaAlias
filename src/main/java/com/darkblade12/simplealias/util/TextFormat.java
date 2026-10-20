package com.darkblade12.simplealias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.regex.Pattern;

/** MiniMessage is the only supported formatting syntax; dynamic values remain literal. */
public final class TextFormat {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Pattern SECTION_CODES = Pattern.compile(
            "(?i)(?:§x(?:[&§][0-9a-f]){6}|§#[0-9a-f]{6}|§[0-9a-fk-orx])");
    private static final Pattern AMPERSAND_CODES = Pattern.compile(
            "(?i)(?:(?:&x(?:[&§][0-9a-f]){6}|&#[0-9a-f]{6})(?:&[0-9a-fk-or])*"
            + "|(?<![\\p{L}\\p{N}])(?:&[0-9a-fk-or])+)");


    private TextFormat() {
    }

    /** Discard recognized legacy formatting from external values while preserving ordinary text. */
    public static String stripLegacyCodes(String input) {
        if (input == null) {
            return "";
        }
        String withoutSection = SECTION_CODES.matcher(input).replaceAll("").replace("§", "");
        return AMPERSAND_CODES.matcher(withoutSection).replaceAll("");
    }

    public static Component render(String template) {
        return render(template, Map.of());
    }

    public static Component render(String template, Map<String, Component> variables) {
        TagResolver.Builder resolvers = TagResolver.builder();
        variables.forEach((name, value) -> resolvers.resolver(Placeholder.component(name, value)));
        return MINI.deserialize(template, resolvers.build());
    }

    /** PlaceholderAPI and arguments can never inject MiniMessage events or styling. */
    public static Component external(String value) {
        return Component.text(stripLegacyCodes(value));
    }
}
