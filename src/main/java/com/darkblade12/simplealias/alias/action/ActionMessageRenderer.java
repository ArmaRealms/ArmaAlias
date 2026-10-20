package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.plugin.hook.PlaceholderHook;
import com.darkblade12.simplealias.plugin.hook.VaultHook;
import com.darkblade12.simplealias.util.TextFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Compiles a configured alias message with opaque components for all dynamic values. */
final class ActionMessageRenderer {
    private static final Pattern TOKENS = Pattern.compile(
            "(?i)%[^%\\s]{1,200}%|\\{viewer:[a-z0-9_]+\\}|<sender_name>|<sender_uuid>|"
            + "<world_name>|<money_balance>|<group_name>|<params>|"
            + "<params@(?:\\d+|\\d*-\\d+|\\d+-\\d*)>|<alias_name>|<param_count>|"
            + "<remaining_time>|<cost_amount>");

    private ActionMessageRenderer() {
    }

    static Component render(String template, SimpleAlias plugin, CommandSender sender,
                            Player viewer, String[] params) {
        Matcher matcher = TOKENS.matcher(template);
        Map<String, Component> values = new LinkedHashMap<>();
        StringBuilder formatted = new StringBuilder(template.length());
        while (matcher.find()) {
            String token = matcher.group();
            String replacement = resolve(token, plugin, sender, viewer, params);
            Component value = TextFormat.external(replacement);
            String key = "armavalue" + values.size();
            values.put(key, value);
            matcher.appendReplacement(formatted, Matcher.quoteReplacement("<" + key + ">"));
        }
        matcher.appendTail(formatted);
        return TextFormat.render(formatted.toString(), values);
    }

    private static String resolve(String token, SimpleAlias plugin, CommandSender sender,
                                  Player viewer, String[] params) {
        PlaceholderHook papi = plugin.getPlaceholderHook();
        String lower = token.toLowerCase(java.util.Locale.ROOT);
        if (token.startsWith("%")) {
            if (lower.startsWith("%rel_")) {
                return viewer == null ? token : papi.parseViewer(sender, viewer, token);
            }
            return papi.parseActor(sender, token);
        }
        if (lower.startsWith("{viewer:")) {
            return viewer == null ? token : papi.parseViewer(sender, viewer, token);
        }
        if (lower.startsWith("<params@")) {
            return parameterRange(token.substring(8, token.length() - 1), params);
        }
        if (lower.equals("<params>")) {
            return String.join(" ", params);
        }
        if (lower.equals("<sender_name>")) {
            return sender.getName();
        }
        if (!(sender instanceof Player player)) {
            return "";
        }
        VaultHook vault = plugin.getVaultHook();
        return switch (lower) {
            case "<sender_uuid>" -> player.getUniqueId().toString();
            case "<world_name>" -> player.getWorld().getName();
            case "<money_balance>" -> vault.isEconomyEnabled()
                    ? vault.formatCurrency(vault.getBalance(player)) : "";
            case "<group_name>" -> vault.isPermissionEnabled() && vault.hasPermissionGroupSupport()
                    ? vault.getPrimaryGroup(player) : "";
            default -> "";
        };
    }

    private static String parameterRange(String range, String[] params) {
        int dash = range.indexOf('-');
        if (dash < 0) {
            int index = Integer.parseInt(range);
            return index < params.length ? params[index] : "";
        }
        int start = dash == 0 ? 0 : Integer.parseInt(range.substring(0, dash));
        int end = dash == range.length() - 1 ? params.length - 1 : Integer.parseInt(range.substring(dash + 1));
        if (start >= params.length || end < start) {
            return "";
        }
        return String.join(" ", java.util.Arrays.copyOfRange(params, start, Math.min(end, params.length - 1) + 1));
    }
}
