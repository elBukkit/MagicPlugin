package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.elmakers.mine.bukkit.ChatUtils;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MagicHelpCommandExecutor extends MagicTabExecutor {
    protected final MagicController controller;
    protected Map<String,String> helpTopics;

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

        if (args.length == 0) {
            mage.sendMessage(messages.get("commands.mhelp.header"));
            if (!CompatibilityLib.hasChatComponents()) {
                mage.sendMessage(messages.get("commands.mhelp.unavailable"));
            }
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

        // Search through topics for text matches
        Map<String,String> helpTopics = getHelpTopics();
        List<String> topicMatches = new ArrayList<>();
        List<String> partialMatches = new ArrayList<>();
        List<String> lowerArgs = new ArrayList<>();
        for (String arg : args) {
            lowerArgs.add(arg.toLowerCase());
        }

        for (Map.Entry<String,String> entry : helpTopics.entrySet()) {
            boolean hasAny = false;
            boolean hasAll = true;
            for (String arg : lowerArgs) {
                if (entry.getKey().contains(arg) || entry.getValue().contains(arg)) {
                    hasAny = true;
                } else {
                    hasAll = false;
                }
            }
            if (hasAll) {
                topicMatches.add(entry.getKey());
            } else if (hasAny) {
                partialMatches.add(entry.getKey());
            }
        }

        topicMatches.addAll(partialMatches);
        if (topicMatches.isEmpty()) {
            String topic = StringUtils.join(args, " ");
            String unknownMessage = messages.get("commands.mhelp.unknown");
            mage.sendMessage(unknownMessage.replace("$topic", topic));
        } else {
            String foundMessage = messages.get("commands.mhelp.found");
            mage.sendMessage(foundMessage.replace("$count", Integer.toString(topicMatches.size())));
            String template = messages.get("commands.mhelp.match");
            for (String topicMatch : topicMatches) {
                String[] lines = StringUtils.split(helpTopics.get(topicMatch), "\n");
                String summary = null;
                for (String line : lines) {
                    for (String arg : lowerArgs) {
                        if (line.contains(arg)) {
                            summary = line;
                            break;
                        }
                    }
                    if (summary != null) break;
                }
                if (summary == null) {
                    summary = lines[0];
                }
                topicMatch = template
                    .replace("$topic", topicMatch)
                    .replace("$summary", summary);
                mage.sendMessage(topicMatch);
            }
        }
        mage.sendMessage(messages.get("commands.mhelp.separator"));
    }

    public Map<String,String> getHelpTopics() {
        if (helpTopics == null) {
            helpTopics = new HashMap<>();
            Messages messages = controller.getMessages();
            for (String messageKey : messages.getAllKeys()) {
                if (messageKey.startsWith("help.")) {
                    helpTopics.put(messageKey.substring(5), ChatUtils.getSimpleMessage(messages.get(messageKey).toLowerCase()));
                }
            }

        }
        return helpTopics;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        Set<String> options = new HashSet<>();
        if (!sender.hasPermission("Magic.commands.magic.help")) return options;

        // Get all help topics from messages
        options.addAll(getHelpTopics().keySet());

        // Add special-cases
        options.add("instructions.wand");
        options.add("instructions.example");
        for (String exampleKey : controller.getLoadedExamples()) {
            options.add("instructions.example." + exampleKey);
        }

        return options;
    }
}
