package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.effect.NPCTargetingContext;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.Targeting;

public class MagicNPCCommandExecutor extends MagicTabExecutor {
    private static final int npcsPerPage = 10;
    private static final int selectRange = 32;
    private final Targeting targeting;

    public MagicNPCCommandExecutor(MagicAPI api) {
        super(api, "mnpc");
        targeting = new Targeting();
        ConfigurationSection targetingParameters = new MemoryConfiguration();
        targetingParameters.set("range", selectRange);
        targetingParameters.set("target", "other_entity");
        targetingParameters.set("ignore_blocks", true);
        targeting.processParameters(targetingParameters);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: mnpc [add|configure|type|name|list|remove|tp|tphere|import] <name|type>");
            return true;
        }

        if (args[0].equalsIgnoreCase("import")) {
            onImportNPCs(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            int pageNumber = 1;
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

        Mage mage = controller.getMage(sender);
        String[] parameters = Arrays.copyOfRange(args, 1, args.length);
        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("create")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc add <name>");
                return true;
            }
            if (!mage.isPlayer()) {
                sender.sendMessage(ChatColor.RED + "This command may only be used in-game");
                return true;
            }
            onAddNPC(mage, StringUtils.join(parameters, " "));
            return true;
        }

        if (args[0].equalsIgnoreCase("select")) {
            String targetName = null;
            if (parameters.length > 0) {
                targetName = StringUtils.join(parameters, " ");
            }
            onSelectNPC(mage, targetName);
            return true;
        }

        // Requires a selection
        MagicNPC npc = mage == null ? null : mage.getSelectedNPC();

        if (npc == null) {
            sender.sendMessage(ChatColor.RED + "Select an NPC first using /mnpc select");
            return true;
        }

        if (args[0].equalsIgnoreCase("rename") || args[0].equalsIgnoreCase("name")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc rename <name>");
                return true;
            }
            onRenameNPC(mage, npc, StringUtils.join(parameters, " "));
            return true;
        }

        if (args[0].equalsIgnoreCase("type")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc type <mob type>");
                return true;
            }
            onChangeNPCType(mage, npc, StringUtils.join(parameters, " "));
            return true;
        }

        if (args[0].equalsIgnoreCase("tp")) {
            onTPNPC(mage, npc);
            return true;
        }

        if (args[0].equalsIgnoreCase("tphere")) {
            onTPNPCHere(mage, npc);
            return true;
        }

        if (args[0].equalsIgnoreCase("configure")) {
            onConfigureNPC(mage, npc, parameters);
            return true;
        }

        if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete")) {
            onRemoveNPC(mage, npc);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand: mnpc " + args[0]);
        return true;
    }

    protected void onImportNPCs(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Not yet implemented");
    }

    protected List<MagicNPC> getNPCList() {
        Collection<MagicNPC> npcs = controller.getNPCs();
        if (npcs instanceof List) {
            return (List<MagicNPC>)npcs;
        }
        List<MagicNPC> list = new ArrayList<>(npcs);
        return list;
    }

    protected void onListNPCs(CommandSender sender, int pageNumber) {
        List<MagicNPC> npcs = getNPCList();
        int startIndex = (pageNumber - 1) * npcsPerPage;
        for (int i = startIndex; i < startIndex + npcsPerPage && i < npcs.size(); i++) {
            MagicNPC npc = npcs.get(i);
            sender.sendMessage(ChatColor.YELLOW + Integer.toString(i) + ChatColor.GRAY + ": " + ChatColor.GOLD + npc.getName());
        }
        if (npcs.size() > npcsPerPage) {
            int pages = (npcs.size() / npcsPerPage) + 1;
            sender.sendMessage("  " + ChatColor.GRAY + "Page " + ChatColor.YELLOW
                + pageNumber + ChatColor.GRAY + "/" + ChatColor.GOLD + pages);
        }
    }

    protected void onAddNPC(Mage mage, String name) {
        MagicNPC npc = controller.addNPC(mage, name);
        mage.setSelectedNPC(npc);
        mage.sendMessage(ChatColor.GREEN + " Created npc: " + ChatColor.GOLD + npc.getName());
    }

    protected void onRenameNPC(Mage mage, MagicNPC npc, String name) {
        mage.sendMessage(ChatColor.GREEN + " Changed name of npc " + ChatColor.GOLD + npc.getName()
                + ChatColor.GREEN + " to " + ChatColor.YELLOW + name);
        npc.setName(name);
    }

    protected void onChangeNPCType(Mage mage, MagicNPC npc, String typeKey) {
        if (npc.setType(typeKey)) {
            mage.sendMessage(ChatColor.GREEN + " Changed type of npc " + ChatColor.GOLD + npc.getName()
                + ChatColor.GREEN + " to " + ChatColor.YELLOW + typeKey);
        } else {
            mage.sendMessage(ChatColor.RED + "Unknown mob type: " + ChatColor.YELLOW + typeKey);
        }
    }

    @Nullable
    protected MagicNPC findNPC(@Nullable String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        List<MagicNPC> npcs = getNPCList();
        for (MagicNPC npc : npcs) {
            if (npc.getName().equalsIgnoreCase(name)) {
                return npc;
            }
        }

        try {
            int index = Integer.parseInt(name);
            if (index >= 0 && index < npcs.size()) {
                return npcs.get(index);
            }
        } catch (NumberFormatException ignore) {

        }
        return null;
    }

    protected void onSelectNPC(Mage mage, String name) {
        MagicNPC npc = findNPC(name);
        if (npc == null && mage.isPlayer()) {
            NPCTargetingContext context = new NPCTargetingContext(mage);
            targeting.reset();
            Target target = targeting.target(context, selectRange);
            if (target != null && target.hasEntity()) {
                npc = controller.getNPC(target.getEntity());
            }
        }
        if (npc == null) {
            if (name == null || name.isEmpty()) {
                if (!mage.isPlayer()) {
                    mage.sendMessage(ChatColor.RED + "When using from console, must provide NPC name");
                } else {
                    mage.sendMessage(ChatColor.RED + "There is no NPC in front of you");
                }
            } else {
                mage.sendMessage(ChatColor.RED + "Could not find NPC: " + ChatColor.GOLD + name);
            }
        } else {
            mage.setSelectedNPC(npc);
            mage.sendMessage(ChatColor.GRAY + "Selected NPC: " + ChatColor.GOLD + npc.getName());
        }
    }

    protected void onRemoveNPC(Mage mage, MagicNPC npc) {
        controller.removeNPC(npc);
        mage.setSelectedNPC(null);
        mage.sendMessage(ChatColor.GREEN + " Removed npc: " + ChatColor.GOLD + npc.getName());
    }

    protected void onTPNPC(Mage mage, MagicNPC npc) {
        if (!mage.isPlayer()) {
            mage.sendMessage(ChatColor.RED + "This command may only be used in-game");
        }
        npc.teleport(mage.getEntity().getLocation());
    }

    protected void onTPNPCHere(Mage mage, MagicNPC npc) {
        if (!mage.isPlayer()) {
            mage.sendMessage(ChatColor.RED + "This command may only be used in-game");
        }
        mage.getEntity().teleport(npc.getLocation());
    }

    protected void onConfigureNPC(Mage mage, MagicNPC npc, String[] parameters) {
        if (parameters.length == 0 || parameters[0].length() == 0) {
            mage.sendMessage(ChatColor.RED + "Missing parameter name");
            return;
        }
        String key = parameters[0];
        String value = null;
        if (parameters.length > 1) {
            value = parameters[1];
        }
        npc.configure(key, value);
        if (value == null) {
            mage.sendMessage(ChatColor.GREEN + " Configured npc " + ChatColor.GOLD + npc.getName()
                    + ChatColor.GREEN + ", cleared " + ChatColor.AQUA + key);
        } else {
            mage.sendMessage(ChatColor.GREEN + " Configured npc " + ChatColor.GOLD + npc.getName()
                    + ChatColor.GREEN + ", set " + ChatColor.AQUA + key + ChatColor.GREEN + " to " + ChatColor.YELLOW + value);
        }
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
            options.add("type");
            options.add("remove");
            options.add("tp");
            options.add("tphere");
            options.add("import");
        }
        return options;
    }
}
