package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicExampleCommandExecutor extends MagicConfigCommandExecutor {
    public MagicExampleCommandExecutor(MagicAPI api, MagicController controller) {
        super(api, controller, "mexample");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!api.hasPermission(sender, "magic.commands.mconfig.example")) {
            sendNoPermission(sender);
            return true;
        }

        onExample(sender, args);
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("magic.commands.mconfig.example")) return options;

        addExampleTabComplete(args, options);
        return options;
    }
}
