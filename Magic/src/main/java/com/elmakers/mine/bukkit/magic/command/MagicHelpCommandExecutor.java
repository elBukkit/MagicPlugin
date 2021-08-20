package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicHelpCommandExecutor extends MagicTabExecutor {
    protected final MagicController controller;

    public MagicHelpCommandExecutor(MagicAPI api) {
        super(api, "mhelp");
        controller = ((MagicController)api.getController());
    }

    protected MagicHelpCommandExecutor(MagicAPI api, String command) {
        super(api, command);
        controller = ((MagicController)api.getController());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.magic.help")) {
            sendNoPermission(sender);
            return true;
        }

        onMagicHelp(sender, args);
        return true;
    }

    protected void onMagicHelp(CommandSender sender, String[] args) {
        Messages messages = controller.getMessages();
        Mage mage = controller.getMage(sender);
        if (!CompatibilityLib.hasChatComponents()) {
            mage.sendMessage(messages.get("commands.mhelp.header"));
            mage.sendMessage(messages.get("commands.mhelp.unavailable"));
            return;
        }

        if (args.length == 0) {
            mage.sendMessage(messages.get("commands.mhelp.header"));
            mage.sendMessage(messages.get("help.main"));
            mage.sendMessage(messages.get("commands.mhelp.separator"));
            return;
        }

        // Some special cases:
        if (args[0].startsWith("instructions")) {
            String[] pieces = StringUtils.split(args[0], ".");
            if (pieces.length > 1) {
                if (pieces[1].equals("wand")) {
                    Wand wand = mage.getActiveWand();
                    if (wand != null) {
                        wand.showInstructions();
                    } else {
                        mage.sendMessage(messages.get("commands.mhelp.no_wand"));
                    }
                    return;
                } else if (pieces[1].equals("example")) {
                    if (pieces.length > 2) {
                        controller.showExampleInstructions(sender, pieces[2]);
                    } else {
                        controller.showExampleInstructions(sender);
                    }
                    return;
                }
            }
        }

        String exactMessage = messages.get("help." + args[0], "");
        if (!exactMessage.isEmpty()) {
            mage.sendMessage(exactMessage);
            mage.sendMessage(messages.get("commands.mhelp.separator"));
            return;
        }

        // TODO: Search topics

        String topic = StringUtils.join(args, " ");
        String unknownMessage = messages.get("commands.mhelp.unknown");
        mage.sendMessage(unknownMessage.replace("$topic", topic));
        mage.sendMessage(messages.get("commands.mhelp.separator"));
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.magic.help")) return options;

        for (String messageKey : controller.getMessages().getAllKeys()) {
            if (messageKey.startsWith("help.")) {
                options.add(messageKey.substring(5));
            }
        }

        return options;
    }
}
