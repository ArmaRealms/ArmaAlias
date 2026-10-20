package com.darkblade12.simplealias.plugin.hook;

import com.darkblade12.simplealias.util.ViewerPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Optional PlaceholderAPI integration. External API calls remain isolated here.
 * PAPI may be absent at runtime; no method on an action calls its API directly.
 */
public final class PlaceholderHook extends Hook {
    public PlaceholderHook() {
        super("PlaceholderAPI");
    }

    @Override
    protected boolean initialize() {
        return true;
    }

    public boolean isAvailable() {
        return isEnabled() && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /** Normal placeholders always use the player who invoked the alias. */
    public String parseActor(CommandSender sender, String text) {
        if (!isAvailable() || !(sender instanceof Player actor) || text.indexOf('%') < 0) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(actor, text);
    }

    /** Relational placeholders compare the actor with each viewer. */
    public String parseViewer(CommandSender actor, Player viewer, String text) {
        if (!isAvailable()) {
            return text;
        }

        String result = text;
        if (actor instanceof Player source && result.toLowerCase(Locale.ROOT).contains("%rel_")) {
            result = PlaceholderAPI.setRelationalPlaceholders(source, viewer, result);
        }

        if (ViewerPlaceholders.hasViewerTokens(result)) {
            result = ViewerPlaceholders.replace(result,
                    identifier -> PlaceholderAPI.setPlaceholders(viewer, "%" + identifier + "%"));
        }
        return result;
    }

    public boolean needsViewerContext(String text) {
        return isAvailable() && (text.toLowerCase(Locale.ROOT).contains("%rel_")
                || ViewerPlaceholders.hasViewerTokens(text));
    }
}
