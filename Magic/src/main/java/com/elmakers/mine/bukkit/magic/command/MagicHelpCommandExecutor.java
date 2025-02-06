package com.elmakers.mine.bukkit.magic.command;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.help.Help;
import com.elmakers.mine.bukkit.utility.help.SearchHelpTask;

public class MagicHelpCommandExecutor extends MagicTabExecutor {
    private static final int MAX_RESULTS = 10;
    protected final MagicController controller;
    protected final Help help;
    protected Map<String,String> helpTopics;

    protected MagicHelpCommandExecutor(MagicAPI api, String command) {
        super(api, command);
        controller = ((MagicController)api.getController());
        help = controller.getMessages().getHelp();
    }

    public MagicHelpCommandExecutor(MagicAPI api) {
        this(api, "mhelp");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        if (!api.hasPermission(sender, "magic.commands.mhelp")) {
            sendNoPermission(sender);
            return true;
        }

        onMagicHelp(sender, args);
        return true;
    }

    protected void onMagicHelp(CommandSender sender, String[] args) {
        Messages messages = controller.getMessages();
        Mage mage = controller.getMage(sender);
        mage.setShownHelp();

        if (args.length == 0) {
            mage.sendMessage(messages.get("commands.mhelp.header"));
            if (!CompatibilityLib.hasChatComponents()) {
                mage.sendMessage(messages.get("commands.mhelp.unavailable"));
            } else {
                if (sender instanceof Player) {
                    mage.sendMessage(messages.get("commands.mhelp.player_header"));
                } else {
                    mage.sendMessage(messages.get("commands.mhelp.console_header"));
                }
            }
            help.showTopic(mage, "main");
            mage.sendMessage(messages.get("commands.mhelp.separator"));
            return;
        }

        // If we only have one arg, look for an exact topic match first
        if (args.length == 1) {
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

            if (help.showTopic(mage, args[0])) {
                mage.sendMessage(messages.get("commands.mhelp.separator"));
                return;
            }
        }

        // Search through topics for text matches, do this async since we're
        // going to spin pretty hard looking through text (assuming help gets big)
        SearchHelpTask searchTask = new SearchHelpTask(help, mage, args, MAX_RESULTS);
        Plugin plugin = controller.getPlugin();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, searchTask);
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("magic.commands.mhelp")) return options;

        // Get all words in all help topics
        options.addAll(help.getWords());

        return options;
    }
}
