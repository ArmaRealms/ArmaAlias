package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.SimpleAlias;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.darkblade12.simplealias.plugin.hook.PlaceholderHook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MessageAction extends Action {
    private String message;
    private boolean broadcast;

    public MessageAction(String name, Set<String> enabledWorlds, Set<String> enabledPermissionNodes, Set<String> enabledPermissionGroups,
                         Map<Integer, String> enabledParams, int priority, boolean translateColorCodes, String message, boolean broadcast) {
        super(name, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams, priority, translateColorCodes);
        this.message = message;
        this.broadcast = broadcast;
    }

    public MessageAction(String name, String message) {
        this(name, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashMap<>(), 0, false, message, false);
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
        PlaceholderHook placeholders = plugin.getPlaceholderHook();
        // Resolve configured placeholders before <params> so player input is never parsed as PAPI markup.
        String template = placeholders.parseActor(sender, message);
        if (broadcast) {
            if (!placeholders.needsViewerContext(template)) {
                Bukkit.broadcastMessage(replaceVariables(template, plugin, sender, params));
                return;
            }

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                String personalized = placeholders.parseViewer(sender, viewer, template);
                viewer.sendMessage(replaceVariables(personalized, plugin, sender, params));
            }
            // Console has no viewer context: log the actor-only version.
            Bukkit.getConsoleSender().sendMessage(replaceVariables(template, plugin, sender, params));
        } else if (sender instanceof Player viewer) {
            String personalized = placeholders.parseViewer(sender, viewer, template);
            sender.sendMessage(replaceVariables(personalized, plugin, sender, params));
        } else {
            sender.sendMessage(replaceVariables(template, plugin, sender, params));
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    @Override
    public ActionType getType() {
        return ActionType.MESSAGE;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isBroadcast() {
        return this.broadcast;
    }
}
