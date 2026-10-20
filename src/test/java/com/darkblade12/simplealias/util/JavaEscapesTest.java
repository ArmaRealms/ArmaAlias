package com.darkblade12.simplealias.util;

/** No external test framework required. Run with java --class-path target/classes <source file>. */
public final class JavaEscapesTest {
    public static void main(String[] args) {
        check("first\nsecond", JavaEscapes.unescapeJava("first\\nsecond"), "line break");
        check("A", JavaEscapes.unescapeJava("\\101"), "octal");
        check("água", JavaEscapes.unescapeJava("\\u00e1gua"), "Unicode");
        check("A", JavaEscapes.unescapeJava("\\uuuu0041"), "repeated Unicode prefix");
        check("unkxnown", JavaEscapes.unescapeJava("unk\\xnown"), "unknown escape");
        check("line\\nbreak", JavaEscapes.escapeJava("line\nbreak"), "escaped line");
        check("quote\\\" and \\\\ slash", JavaEscapes.escapeJava("quote\" and \\ slash"), "quote and slash");
        String original = "á § \t \r \n \" \\ ";
        check(original, JavaEscapes.unescapeJava(JavaEscapes.escapeJava(original)), "roundtrip");
    }

    private static void check(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected <" + expected + "> but was <" + actual + ">");
        }
    }
}
