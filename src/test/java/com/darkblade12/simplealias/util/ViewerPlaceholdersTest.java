package com.darkblade12.simplealias.util;

/** Pure regression coverage without needing a running Paper server or PlaceholderAPI plugin. */
public final class ViewerPlaceholdersTest {
    public static void main(String[] args) {
        check("Hello Alex!", ViewerPlaceholders.replace(
                "Hello {viewer:player_name}!", key -> "Alex"), "viewer-specific resolution");
        check("$5\\escaped", ViewerPlaceholders.replace(
                "{viewer:player_name}", key -> "$5\\escaped"), "literal regex replacement");
        check("{viewer:player_name}", ViewerPlaceholders.replace(
                "{viewer:player_name}", key -> null), "unknown value preserved");
        check("Alex and Alex", ViewerPlaceholders.replace(
                "{viewer:player_name} and {viewer:player_name}", key -> "Alex"), "multiple tokens");
        check("%player_name% + Alex + %rel_example_value%",
                ViewerPlaceholders.replace("%player_name% + {viewer:player_name} + %rel_example_value%",
                        key -> "Alex"), "actor and relational formats preserved");
        if (ViewerPlaceholders.hasViewerTokens("%player_name% only")) {
            throw new AssertionError("normal actor placeholders must not trigger viewer parsing");
        }
        if (!ViewerPlaceholders.hasViewerTokens("Hi {viewer:player_name}")) {
            throw new AssertionError("viewer token not detected");
        }
    }

    private static void check(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> got <" + actual + ">");
        }
    }
}
