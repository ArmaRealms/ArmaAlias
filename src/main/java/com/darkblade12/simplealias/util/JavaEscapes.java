package com.darkblade12.simplealias.util;

/**
 * Java string literal escaping for existing alias YAML values and language resources.
 * Covers quotes, backslashes, controls, Unicode escapes, and Java octal escapes.
 */
public final class JavaEscapes {
    private JavaEscapes() {
    }

    public static String unescapeJava(String input) {
        if (input == null || input.indexOf('\\') < 0) {
            return input;
        }

        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (current != '\\' || i + 1 == input.length()) {
                result.append(current);
                continue;
            }

            char next = input.charAt(++i);
            switch (next) {
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case '\\' -> result.append('\\');
                case '"' -> result.append('"');
                case '\'' -> result.append('\'');
                case 'u' -> {
                    int endOfU = i;
                    while (endOfU + 1 < input.length() && input.charAt(endOfU + 1) == 'u') {
                        endOfU++;
                    }
                    int startOfHex = endOfU + 1;
                    if (startOfHex + 4 <= input.length() && isHex(input, startOfHex)) {
                        result.append((char) Integer.parseInt(input.substring(startOfHex, startOfHex + 4), 16));
                        i = startOfHex + 3;
                    } else {
                        result.append('\\').append('u');
                    }
                }
                default -> {
                    if (next >= '0' && next <= '7') {
                        int value = next - '0';
                        int allowedDigits = next <= '3' ? 3 : 2;
                        for (int digits = 1; digits < allowedDigits && i + 1 < input.length(); digits++) {
                            char digit = input.charAt(i + 1);
                            if (digit < '0' || digit > '7') {
                                break;
                            }
                            value = value * 8 + (digit - '0');
                            i++;
                        }
                        result.append((char) value);
                    } else {
                        // Commons Lang unescapeJava removed the slash for unknown escapes.
                        result.append(next);
                    }
                }
            }
        }
        return result.toString();
    }

    public static String escapeJava(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\\' -> result.append("\\\\");
                case '"' -> result.append("\\\"");
                case '\b' -> result.append("\\b");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                case '\f' -> result.append("\\f");
                default -> {
                    if (c < 0x20 || c > 0x7e) {
                        result.append("\\u");
                        for (int shift = 12; shift >= 0; shift -= 4) {
                            result.append(Character.forDigit((c >> shift) & 0xf, 16));
                        }
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        return result.toString();
    }

    private static boolean isHex(String input, int offset) {
        for (int i = offset; i < offset + 4; i++) {
            if (Character.digit(input.charAt(i), 16) < 0) {
                return false;
            }
        }
        return true;
    }
}
