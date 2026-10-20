package com.darkblade12.simplealias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

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

        String commented = readTemplateMessage("src/main/resources/template.yml");
        String uncommented = readTemplateMessage("src/main/resources/template_uncommented.yml");
        check(commented, uncommented, "templates use the same MiniMessage example");

        Component template = TextFormat.render(commented.replace("<sender_name>", "<armavalue0>"),
                Map.of("armavalue0", Component.text("Alex")));
        check("Hello, Alex! [Help]",
                PlainTextComponentSerializer.plainText().serialize(template), "template example rendering");
        if (!hasClickEvent(template) || !hasHoverEvent(template)) {
            throw new AssertionError("template MiniMessage example must expose click and hover events");
        }
    }

    private static String readTemplateMessage(String path) {
        try {
            return Files.readAllLines(Path.of(path)).stream()
                    .filter(line -> line.startsWith("      Message: '") && line.endsWith("'"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("missing action Message example: " + path))
                    .substring("      Message: '".length()).replaceFirst("'$", "");
        } catch (IOException e) {
            throw new AssertionError("unable to read template: " + path, e);
        }
    }

    private static boolean hasHoverEvent(Component component) {
        return component.hoverEvent() != null
                || component.children().stream().anyMatch(TextFormatTest::hasHoverEvent);
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
