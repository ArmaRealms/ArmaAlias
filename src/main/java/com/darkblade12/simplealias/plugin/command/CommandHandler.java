package com.darkblade12.simplealias.plugin.command;

import com.darkblade12.simplealias.plugin.PluginBase;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dispatches existing administrative subcommands through a Paper Brigadier root command.
 */
public abstract class CommandHandler<T extends PluginBase> implements BasicCommand, Iterable<CommandBase<T>> {
    protected final T plugin;
    protected final String defaultLabel;
    protected final Map<String, CommandBase<T>> commands;
    protected final HelpCommand<T> help;

    protected CommandHandler(T plugin, String defaultLabel, int helpPageSize) {
        this.plugin = plugin;
        this.defaultLabel = defaultLabel;
        commands = new LinkedHashMap<>();
        help = new HelpCommand<>(this, helpPageSize);
    }

    protected CommandHandler(T plugin, String defaultLabel) {
        this(plugin, defaultLabel, 4);
    }

    public void enable() throws CommandRegistrationException {
        registerCommand(help);
        registerCommands();
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (args.length == 0) {
            displayUnknownCommand(sender, defaultLabel);
            return;
        }

        CommandBase<T> command = getCommand(args[0]);
        if (command == null) {
            displayUnknownCommand(sender, defaultLabel);
            return;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        if (!command.isExecutableAsConsole() && !(sender instanceof Player)) {
            plugin.sendMessage(sender, "command.noConsole");
            return;
        }

        if (!command.testPermission(sender)) {
            plugin.sendMessage(sender, "command.noPermission");
            return;
        }

        if (!command.isValid(subArgs)) {
            displayInvalidUsage(sender, command, defaultLabel);
            return;
        }

        command.execute(plugin, sender, defaultLabel, subArgs);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        CommandSender sender = source.getSender();
        if (args.length < 1) {
            return Collections.emptyList();
        }

        List<String> suggestions;
        if (args.length == 1) {
            suggestions = commands.values().stream()
                    .filter(c -> c.testPermission(sender) && (c.isExecutableAsConsole() || sender instanceof Player))
                    .map(CommandBase::getName)
                    .collect(Collectors.toList());
        } else {
            CommandBase<T> command = getCommand(args[0]);
            if (command == null || !command.testPermission(sender)) {
                return Collections.emptyList();
            }

            suggestions = command.getSuggestions(plugin, sender, Arrays.copyOfRange(args, 1, args.length));
        }

        if (suggestions == null || suggestions.isEmpty()) {
            return Collections.emptyList();
        }

        String filter = args[args.length - 1].toLowerCase(Locale.ROOT);
        return suggestions.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(filter))
                .collect(Collectors.toList());
    }

    protected void registerCommand(CommandBase<T> command) throws CommandRegistrationException {
        String name = command.getName();
        if (commands.containsKey(name)) {
            throw new CommandRegistrationException("The command '%s' cannot be registered multiple times.", name);
        }
        commands.put(name, command);
    }

    protected void registerCommand(Class<? extends CommandBase<T>> cmdClass) throws CommandRegistrationException {
        CommandBase<T> command;
        try {
            command = cmdClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CommandRegistrationException("Failed to instantiate the command of class '%s'.", cmdClass.getName(), e);
        }
        registerCommand(command);
    }

    protected abstract void registerCommands() throws CommandRegistrationException;

    @Override
    public Iterator<CommandBase<T>> iterator() {
        return commands.values().iterator();
    }

    private void displayUnknownCommand(CommandSender sender, String label) {
        plugin.sendMessage(sender, "command.unknown", help.getUsage(label));
    }

    public void displayInvalidUsage(CommandSender sender, CommandBase<T> command, String label) {
        plugin.sendMessage(sender, "command.invalidUsage", command.getUsage(label));
    }

    public T getPlugin() {
        return plugin;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    public CommandBase<T> getCommand(String name) {
        return commands.get(name.toLowerCase(Locale.ROOT));
    }
}
