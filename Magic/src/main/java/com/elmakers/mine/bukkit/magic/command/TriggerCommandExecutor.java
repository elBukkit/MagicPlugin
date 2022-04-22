package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.Messages;

public class TriggerCommandExecutor extends MagicTabExecutor {

    public TriggerCommandExecutor(MagicAPI api) {
        super(api, "mtrigger");
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }
        Messages messages = controller.getMessages();
        Mage mage = controller.getMage(sender);
        if (args.length != 2) {
            mage.sendMessage(messages.get("commands.mtrigger.invalid_parameters"));
            return true;
        }
        String mageId = args[0];
        String triggerId = args[1];

        if (!api.hasPermission(sender, "magic.mtrigger")) {
            try {
                UUID.fromString(triggerId);
            } catch (IllegalArgumentException invalidUUID) {
                mage.sendMessage(messages.get("commands.mtrigger.invalid_target"));
                return true;
            }
        }

        Mage targetMage = api.getController().getRegisteredMage(mageId);
        if (targetMage == null) {
            mage.sendMessage(messages.get("commands.mtrigger.no_mage"));
            return true;
        }

        targetMage.trigger(triggerId);
        return true;
    }
}
