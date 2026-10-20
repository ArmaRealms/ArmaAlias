package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
            list = list.append(Component.newline())
                    .append(Component.text(" ▪ ", NamedTextColor.GRAY))
                    .append(Component.text(alias.getName(), NamedTextColor.GREEN));
        }

        plugin.sendMessage(sender, "command.alias.list.message", list);
    }
}
