package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicHelpCommandExecutor extends MagicTabExecutor {
    private final MagicController controller;

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
        Mage mage = controller.getMage(sender);
        if (args.length == 0) {
            mage.sendMessage(controller.getMessages().get("commands.magic.help_header"));

            // TODO: wand instructions
            Wand wand = mage.getActiveWand();
            if (wand != null) {
                // Click action for wand
            }
            mage.sendMessage(controller.getMessages().get("help.main"));
            return;
        }

        String exactMessage = controller.getMessages().get("help." + args[0], "");
        if (!exactMessage.isEmpty()) {
            mage.sendMessage(exactMessage);
            return;
        }

        // TODO: Search topics


        // TODO: Example instructions
        if (args[0].equals("examples")) {
            controller.showExampleInstructions(sender);
            return;
        }

        // TODO: wand instructions

        if (args[0].equals("wand")) {
            Wand wand = mage.getActiveWand();
            if (wand != null) {
                wand.showInstructions();
            }
            return;
        }

        String topic = StringUtils.join(args, " ");
        String unknownMessage = controller.getMessages().get("commands.magic.help.unknown");
        mage.sendMessage(unknownMessage.replace("$topic", topic));
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
