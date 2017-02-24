package com.elmakers.mine.bukkit.magic;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;

public class MageIdentifier {
    public String fromEntity(Entity entity) {
        return entity.getUniqueId().toString();
    }

    public String fromCommandSender(CommandSender commandSender) {
        if (commandSender instanceof ConsoleCommandSender) {
            return "CONSOLE";
        } else if (commandSender instanceof BlockCommandSender) {
            BlockCommandSender commandBlock = (BlockCommandSender) commandSender;
            String commandName = commandBlock.getName();
            if (commandName != null && commandName.length() > 0) {
                return "COMMAND-" + commandBlock.getName();
            }
        }

        return "COMMAND";
    }
}
