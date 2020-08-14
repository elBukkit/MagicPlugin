package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;

public class MagicNPCCommandExecutor extends MagicTabExecutor {
    public MagicNPCCommandExecutor(MagicAPI api) {
        super(api, "mnpc");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: mnpc [add|configure|name|list|remove|tp|tphere] <name>");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            int pageNumber = 0;
            if (args.length > 1) {
                try {
                    pageNumber = Integer.parseInt(args[1]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(ChatColor.RED + "Invalid page number: " + pageNumber);
                    return true;
                }
            }
            onListNPCs(sender, pageNumber);
            return true;
        }

        // Player-only commands
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return true;
        }

        Player player = (Player)sender;
        String[] parameters = Arrays.copyOfRange(args, 1, args.length);
        if (args[0].equalsIgnoreCase("add")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc add <name>");
                return true;
            }
            onAddNPC(player, StringUtils.join(parameters, " "));
            return true;
        }

        if (args[0].equalsIgnoreCase("select")) {
            String targetName = null;
            if (parameters.length > 0) {
                targetName = StringUtils.join(parameters, " ");
            }
            onSelectNPC(player, targetName);
            return true;
        }

        // Requires a selection
        Mage mage = controller.getRegisteredMage(player);
        MagicNPC npc = mage == null ? null : mage.getSelectedNPC();

        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "Select an NPC first using /mnpc select");
            return true;
        }

        if (args[0].equalsIgnoreCase("rename")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc rename <name>");
                return true;
            }
            onRenameNPC(player, npc, StringUtils.join(parameters, " "));
            return true;
        }

        if (args[0].equalsIgnoreCase("tp")) {
            onTPNPC(player, npc);
            return true;
        }

        if (args[0].equalsIgnoreCase("tphere")) {
            onTPNPCHere(player, npc);
            return true;
        }

        if (args[0].equalsIgnoreCase("configure")) {
            onConfigureNPC(player, npc, parameters);
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            onRemoveNPC(player, npc);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand: mnpc " + args[0]);
        return true;
    }

    protected void onListNPCs(CommandSender sender, int pageNumber) {
    }

    protected void onAddNPC(Player player, String name) {

    }

    protected void onRenameNPC(Player player, MagicNPC npc, String name) {

    }

    protected void onSelectNPC(Player player, String name) {

    }

    protected void onRemoveNPC(Player player, MagicNPC npc) {

    }

    protected void onTPNPC(Player player, MagicNPC npc) {

    }

    protected void onTPNPCHere(Player player, MagicNPC npc) {

    }

    protected void onConfigureNPC(Player player, MagicNPC npc, String[] parameters) {

    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (!sender.hasPermission("Magic.commands.mnpc")) return options;

        if (args.length == 1) {
            options.add("add");
            options.add("configure");
            options.add("list");
            options.add("name");
            options.add("remove");
            options.add("tp");
            options.add("tphere");
        }
        return options;
    }
}
