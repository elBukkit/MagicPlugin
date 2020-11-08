package com.elmakers.mine.bukkit.magic;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import com.google.common.base.Preconditions;

/** This can be overridden to store players by an alternative ID system. */
// TODO: Move this to public API once it is stable enough
public class MageIdentifier {
    public String fromEntity(Entity entity) {
        return entity.getUniqueId().toString();
    }

    public String fromPreLogin(AsyncPlayerPreLoginEvent event) {
        // Make sure this aligns with what fromEntity returns for a Player
        return event.getUniqueId().toString();
    }

    public String fromCommandSender(CommandSender commandSender) {
        Preconditions.checkArgument(
                !(commandSender instanceof Player),
                "fromCommandSender does not accept a player argument: %s",
                commandSender);

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
