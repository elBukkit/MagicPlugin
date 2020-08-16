package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.effect.NPCTargetingContext;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
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
            sender.sendMessage(ChatColor.RED + "Usage: mnpc [add|configure|cast|describe|type|name|list|remove|tp|tphere|import] <name|type>");
            return true;
        }

        String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("import")) {
            onImportNPCs(sender);
            return true;
        }

        if (subCommand.equalsIgnoreCase("list")) {
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
        if (subCommand.equalsIgnoreCase("add") || subCommand.equalsIgnoreCase("create")) {
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

        if (subCommand.equalsIgnoreCase("select")) {
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

        if (subCommand.equalsIgnoreCase("rename") || subCommand.equalsIgnoreCase("name")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc rename <name>");
                return true;
            }
            onRenameNPC(mage, npc, StringUtils.join(parameters, " "));
            return true;
        }

        if (subCommand.equalsIgnoreCase("type")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc type <mob type>");
                return true;
            }
            onChangeNPCType(mage, npc, StringUtils.join(parameters, " "));
            return true;
        }

        if (subCommand.equalsIgnoreCase("cast") || subCommand.equalsIgnoreCase("spell")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc cast <spell>");
                return true;
            }
            onChangeNPCSpell(mage, npc, parameters);
            return true;
        }

        if (subCommand.equalsIgnoreCase("tp")) {
            onTPNPC(mage, npc);
            return true;
        }

        if (subCommand.equalsIgnoreCase("tphere")) {
            onTPNPCHere(mage, npc);
            return true;
        }

        if (subCommand.equalsIgnoreCase("configure")) {
            onConfigureNPC(mage, npc, parameters);
            return true;
        }

        if (subCommand.equalsIgnoreCase("describe")) {
            onDescribeNPC(mage, npc);
            return true;
        }

        if (subCommand.equalsIgnoreCase("remove") || subCommand.equalsIgnoreCase("delete")) {
            onRemoveNPC(mage, npc);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand: mnpc " + subCommand);
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
        mage.sendMessage(ChatColor.GREEN + "Created npc: " + ChatColor.GOLD + npc.getName());
    }

    protected void onRenameNPC(Mage mage, MagicNPC npc, String name) {
        mage.sendMessage(ChatColor.GREEN + "Changed name of npc " + ChatColor.GOLD + npc.getName()
                + ChatColor.GREEN + " to " + ChatColor.YELLOW + name);
        npc.setName(name);
    }

    protected void onChangeNPCType(Mage mage, MagicNPC npc, String typeKey) {
        if (npc.setType(typeKey)) {
            mage.sendMessage(ChatColor.GREEN + "Changed type of npc " + ChatColor.GOLD + npc.getName()
                + ChatColor.GREEN + " to " + ChatColor.YELLOW + typeKey);
        } else {
            mage.sendMessage(ChatColor.RED + "Unknown mob type: " + ChatColor.YELLOW + typeKey);
        }
    }

    protected void onChangeNPCSpell(Mage mage, MagicNPC npc, String[] parameters) {
        npc.configure("interact_spell", parameters[0]);
        mage.sendMessage(ChatColor.GREEN + "Changed npc " + ChatColor.GOLD + npc.getName()
            + ChatColor.GREEN + " to cast " + ChatColor.YELLOW + parameters[0]);

        if (parameters.length > 1) {
            mage.sendMessage(ChatColor.GREEN + " With parameters:");
            ConfigurationSection spellParameters = new MemoryConfiguration();
            ConfigurationUtils.addParameters(Arrays.copyOfRange(parameters, 1, parameters.length), spellParameters);
            npc.configure("interact_spell_parameters", spellParameters);
            Set<String> keys = spellParameters.getKeys(false);
            for (String key : keys) {
                Object value = spellParameters.get(key);
                mage.sendMessage(ChatColor.DARK_AQUA + key + ChatColor.GRAY + ": " + ChatColor.WHITE + InventoryUtils.describeProperty(value, InventoryUtils.MAX_PROPERTY_DISPLAY_LENGTH));
            }
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
        mage.getEntity().teleport(npc.getLocation());
    }

    protected void onTPNPCHere(Mage mage, MagicNPC npc) {
        if (!mage.isPlayer()) {
            mage.sendMessage(ChatColor.RED + "This command may only be used in-game");
        }
        npc.teleport(mage.getEntity().getLocation());
    }

    protected void onDescribeNPC(Mage mage, MagicNPC npc) {
        npc.describe(mage.getCommandSender());
    }

    protected void onConfigureNPC(Mage mage, MagicNPC npc, String[] parameters) {
        if (parameters.length == 0 || parameters[0].isEmpty()) {
            mage.sendMessage(ChatColor.RED + "Missing parameter name");
            return;
        }
        String key = parameters[0];
        String value = null;
        if (parameters.length == 2) {
            value = parameters[1];
        } else if (parameters.length > 2) {
            value = StringUtils.join(Arrays.copyOfRange(parameters, 1, parameters.length), " ");
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
            options.add("describe");
            options.add("type");
            options.add("remove");
            options.add("tp");
            options.add("tphere");
            options.add("import");
            options.add("select");
            options.add("cast");
        } else if (args.length == 2 && args[0].equals("type")) {
            options.addAll(controller.getMobKeys());
            for (EntityType entityType : EntityType.values()) {
                if (entityType.isAlive() && entityType.isSpawnable()) {
                    options.add(entityType.name().toLowerCase());
                }
            }
        } else if (args.length == 2 && args[0].equals("select")) {
            for (MagicNPC npc : controller.getNPCs()) {
                options.add(npc.getName());
            }
        } else if (args.length == 2 && args[0].equals("configure")) {
            options.add("ai");
            options.add("gravity");
            options.add("interact_spell_source");
            options.add("interact_spell_target");
            options.add("interact_commands");
        } else if ((args.length == 3 && args[0].equals("configure") && args[1].equals("interact_spell"))
               || (args.length == 2 && args[0].equals("cast"))
               || (args.length == 2 && args[0].equals("spell"))) {
            for (SpellTemplate spell : controller.getSpellTemplates()) {
                options.add(spell.getKey());
            }
        } else if (args.length == 3 && args[0].equals("configure")
               && (args[1].equals("ai") || args[1].equals("gravity") || args[1].equals("interact_spell_caster"))) {
            options.add("true");
            options.add("false");
        } else if (args.length == 3 && args[0].equals("configure") && args[1].equals("interact_spell_target")) {
            options.add("none");
            options.add("npc");
            options.add("player");
        } else if (args.length == 3 && args[0].equals("configure") && args[1].equals("interact_spell_source")) {
            options.add("npc");
            options.add("player");
        }
        return options;
    }
}
