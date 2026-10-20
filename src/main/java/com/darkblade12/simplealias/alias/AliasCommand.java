package com.darkblade12.simplealias.alias;

import com.darkblade12.simplealias.SimpleAlias;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

/**
 * Bridges a configured alias to Paper's Brigadier command system.
 * The alias is resolved by name on every invocation so that removed aliases do not execute.
 */
public final class AliasCommand implements BasicCommand {
    private final SimpleAlias plugin;
    private final String name;

    public AliasCommand(SimpleAlias plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Alias alias = plugin.getAliasManager().getAlias(name);
        if (alias != null) {
            alias.execute(source.getSender(), args);
        }
    }

    @Override
    public boolean canUse(CommandSender sender) {
        Alias alias = plugin.getAliasManager().getAlias(name);
        return alias != null && alias.testPermission(sender);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return Collections.emptyList();
    }

    /**
     * The console executor must handle vanilla commands with a console source,
     * because command dispatching with a player proxy does not support every vanilla command.
     */
    public static boolean isVanillaCommand(String commandLine) {
        if (commandLine.isBlank()) {
            return false;
        }

        String commandName = commandLine.split(" ", 2)[0].toLowerCase(Locale.ROOT);
        Command command = Bukkit.getCommandMap().getCommand(commandName);
        if (command == null || command instanceof PluginCommand) {
            return false;
        }
        if (commandName.startsWith("minecraft:")) {
            return true;
        }

        Command vanillaCommand = Bukkit.getCommandMap().getCommand("minecraft:" + commandName);
        return vanillaCommand != null && vanillaCommand == command;
    }
}
