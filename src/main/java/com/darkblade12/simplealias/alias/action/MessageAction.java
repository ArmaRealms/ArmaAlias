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
                         Map<Integer, String> enabledParams, int priority, String message, boolean broadcast) {
        super(name, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams, priority);
        this.message = message;
        this.broadcast = broadcast;
    }

    public MessageAction(String name, String message) {
        this(name, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashMap<>(), 0, message, false);
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
        PlaceholderHook placeholders = plugin.getPlaceholderHook();
        if (broadcast) {
            if (!placeholders.needsViewerContext(message)) {
                Bukkit.broadcast(ActionMessageRenderer.render(message, plugin, sender, null, params));
                return;
            }

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.sendMessage(ActionMessageRenderer.render(message, plugin, sender, viewer, params));
            }
            Bukkit.getConsoleSender().sendMessage(
                    ActionMessageRenderer.render(message, plugin, sender, null, params));
        } else {
            Player viewer = sender instanceof Player player ? player : null;
            sender.sendMessage(ActionMessageRenderer.render(message, plugin, sender, viewer, params));
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
