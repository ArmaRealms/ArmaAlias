package com.darkblade12.simplealias.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/** Regression tests: MiniMessage-only templates and sanitized dynamic values. */
public final class TextFormatTest {
    public static void main(String[] args) {
        check("Hello", TextFormat.stripLegacyCodes("&aHello"), "ampersand code stripped");
        check("Alert", TextFormat.stripLegacyCodes("§cAlert"), "section code stripped");
        check("Hello", TextFormat.stripLegacyCodes("&#aabbccHello"), "hex code stripped");
        check("Hello", TextFormat.stripLegacyCodes("§x§a§a§b§b§c§cHello"), "RGB code stripped");
        check("Hello", TextFormat.stripLegacyCodes("&x&a&a&b&b&c&cHello"), "RGB ampersand stripped");
        check("A plain & separator", TextFormat.stripLegacyCodes("A plain & separator"), "ordinary ampersand");
        check("R&D", TextFormat.stripLegacyCodes("R&D"), "literal ampersand inside words");
        check("Alice", TextFormat.stripLegacyCodes("§a&lAlice"), "mixed section and ampersand codes");
        check("Alice", TextFormat.stripLegacyCodes("&a&lAlice"), "consecutive ampersand codes");
        check("Alice", TextFormat.stripLegacyCodes("&a§l&nAlice"), "mixed consecutive code chain");
        check("Alice", TextFormat.stripLegacyCodes("&#aabbcc&aAlice"), "hex and ampersand chain");
        check("R&D", PlainTextComponentSerializer.plainText().serialize(
                TextFormat.render("R&D")), "MiniMessage template leaves literal ampersand alone");
        check("R&D", PlainTextComponentSerializer.plainText().serialize(
                TextFormat.external("R&D")), "external values keep ordinary text");
        check("", TextFormat.stripLegacyCodes(null), "null value");
        assertNoLegacyConfig("src/main/resources/messages.properties");
        assertNoLegacyConfig("src/main/resources/messages_en.properties");
        assertNoLegacyConfig("src/main/resources/template.yml");
        assertNoLegacyConfig("src/main/resources/template_uncommented.yml");
        check("A&aB", PlainTextComponentSerializer.plainText().serialize(
                TextFormat.render("<red>A&aB")), "legacy code is literal in MiniMessage template");

        Component normal = TextFormat.render("<green>Hi <bold>world</bold>");
        check("Hi world", PlainTextComponentSerializer.plainText().serialize(normal), "MiniMessage rendering");

        Component output = TextFormat.render("<gold>Hello <armavalue0>",
                Map.of("armavalue0", TextFormat.external(
                        "&a§c&#aabbcc§x§a§a§b§b§c§c<click:run_command:/op>unsafe")));
        check("Hello <click:run_command:/op>unsafe",
                PlainTextComponentSerializer.plainText().serialize(output), "safe PAPI input");
        if (hasClickEvent(output) || hasHoverEvent(output)) {
            throw new AssertionError("external values must not produce MiniMessage events");
        }
        check("Alice", PlainTextComponentSerializer.plainText().serialize(
                TextFormat.external("§l&aAlice")), "PAPI legacy styles removed");
        if (MiniMessage.miniMessage().serialize(TextFormat.external("§l&aAlice")).contains("<bold>")) {
            throw new AssertionError("external legacy style was rendered instead of stripped");
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

    private static void assertNoLegacyConfig(String path) {
        try {
            String content = Files.readString(Path.of(path));
            if (content.matches("(?s).*[&§][0-9a-fk-orx].*")
                    || content.contains("Translate_Color_Codes")) {
                throw new AssertionError("legacy formatting is still present in " + path);
            }
        } catch (IOException e) {
            throw new AssertionError("unable to read resource: " + path, e);
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
