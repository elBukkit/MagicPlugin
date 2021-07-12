package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicEditorCommandExecutor extends MagicConfigCommandExecutor {
    public MagicEditorCommandExecutor(MagicAPI api, MagicController controller) {
        super(api, controller, "meditor");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mconfig.editor")) {
            sendNoPermission(sender);
            return true;
        }

        onStartEditor(sender, args);
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.mconfig.editor")) return options;

        if (args.length == 1) {
            options.addAll(availableFileMap.keySet());
            options.remove("message");
            options.add("messages");
        }

        if (args.length == 2) {
            String fileType = getFileParameter(args[0]);
            if (fileType == null) {
                return options;
            }

            addConfigureOptions(fileType, options);
        }

        return options;
    }
}
