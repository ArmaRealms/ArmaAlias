package com.darkblade12.simplealias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.util.Map;

/** Regression tests: trusted MiniMessage, both legacy color formats and untrusted PAPI values. */
public final class TextFormatTest {
    public static void main(String[] args) {
        check("<reset><green>Hello", TextFormat.toMiniMessage("&aHello"), "ampersand colors");
        check("<reset><red>Alert", TextFormat.toMiniMessage("§cAlert"), "section colors");
        check("<reset><#aabbcc>Hello", TextFormat.toMiniMessage("&#aabbccHello"), "hex colors");
        check("<reset><#aabbcc>Hello", TextFormat.toMiniMessage("§x§a§a§b§b§c§cHello"), "bungee rgb");
        check("<bold>OK<reset>", TextFormat.toMiniMessage("&lOK&r"), "decorations");

        Component normal = TextFormat.render("<green>Hi <bold>world</bold>");
        check("Hi world", PlainTextComponentSerializer.plainText().serialize(normal), "MiniMessage rendering");

        Component output = TextFormat.render("<gold>Hello <armavalue0>",
                Map.of("armavalue0", TextFormat.external("&a<click:run_command:/op>malicious")));
        check("Hello <click:run_command:/op>malicious",
                PlainTextComponentSerializer.plainText().serialize(output), "safe external input");
        if (hasClickEvent(output)) {
            throw new AssertionError("external MiniMessage injection turned into a click event");
        }

        Component legacy = TextFormat.external("§aGreen &cRed");
        check("Green Red", PlainTextComponentSerializer.plainText().serialize(legacy), "PAPI legacy colors");
        if (!MiniMessage.miniMessage().serialize(legacy).contains("red")) {
            throw new AssertionError("legacy red color was not preserved");
        }
    }

    private static boolean hasClickEvent(Component component) {
        if (component.clickEvent() != null) {
            return true;
        }
        return component.children().stream().anyMatch(TextFormatTest::hasClickEvent);
    }

    private static void check(String expected, String actual, String caseName) {
        if (!expected.equals(actual)) {
            throw new AssertionError(caseName + ": expected " + expected + ", actual " + actual);
        }
    }
}
