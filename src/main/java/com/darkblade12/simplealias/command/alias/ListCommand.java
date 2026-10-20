package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import com.darkblade12.simplealias.util.MessageUtils;
import org.bukkit.command.CommandSender;

public final class ListCommand extends CommandBase<SimpleAlias> {
    public ListCommand() {
        super("list", Permission.COMMAND_LIST);
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String label, String[] args) {
        AliasManager manager = plugin.getAliasManager();
        if (manager.getAliasCount() == 0) {
            plugin.sendMessage(sender, "command.alias.list.noneAvailable");
            return;
        }

        Component list = Component.empty();
        for (Alias alias : manager.getAliases()) {
            NamedTextColor first = MessageUtils.randomColor();
            NamedTextColor second = MessageUtils.similarColor(first);
            list = list.append(Component.newline())
                    .append(Component.text(" ▪ ", first))
                    .append(Component.text(alias.getName(), second, TextDecoration.ITALIC));
        }

        plugin.sendMessage(sender, "command.alias.list.message", list);
    }
}
