package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicController;

public class MagicHelpCommandExecutor extends MagicTabExecutor {
    public MagicHelpCommandExecutor(MagicAPI api) {
        super(api, "mhelp");
    }

    protected MagicHelpCommandExecutor(MagicAPI api, String command) {
        super(api, command);
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
        sender.sendMessage(controller.getMessages().get("commands.magic.help_header"));
        if (controller instanceof MagicController) {
            ((MagicController)controller).showExampleInstructions(sender);
        }
        if (sender instanceof Player) {
            Mage mage = controller.getMage(sender);
            Wand wand = mage.getActiveWand();
            if (wand != null) {
                wand.showInstructions();
            }
        }
        sender.sendMessage(controller.getMessages().get("commands.magic.help"));
        sender.sendMessage(controller.getMessages().get("commands.magic.help_footer"));
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.magic.help")) return options;

        // TODO

        return options;
    }
}
