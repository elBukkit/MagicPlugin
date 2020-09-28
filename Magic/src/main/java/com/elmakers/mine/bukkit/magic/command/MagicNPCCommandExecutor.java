package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.npc.MagicNPC;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.citizens.CitizensController;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

public class MagicNPCCommandExecutor extends MagicTabExecutor {
    private final NPCSelectionManager selections;

    public MagicNPCCommandExecutor(MagicAPI api) {
        super(api, "mnpc");
        selections = new NPCSelectionManager((MagicController)api.getController());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, getPermissionNode())) {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("import")) {
            onImportNPCs(sender);
            return true;
        }

        String[] parameters = Arrays.copyOfRange(args, 1, args.length);
        if (subCommand.equalsIgnoreCase("list")) {
            onListNPCs(sender, parameters);
            return true;
        }

        Mage mage = controller.getMage(sender);
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
        MagicNPC npc = mage == null ? null : selections.getSelected(mage.getCommandSender());

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
            onChangeNPCSpell(mage, npc, parameters);
            return true;
        }

        if (subCommand.equalsIgnoreCase("tp")) {
            onTPNPC(mage, npc);
            return true;
        }

        if (subCommand.equalsIgnoreCase("move") || subCommand.equalsIgnoreCase("tphere")) {
            onMoveNPC(mage, npc);
            return true;
        }

        if (subCommand.equalsIgnoreCase("configure")) {
            onConfigureNPC(mage, npc, parameters);
            return true;
        }

        if (subCommand.equalsIgnoreCase("costs")) {
            onNPCCost(mage, npc, parameters);
            return true;
        }

        if (subCommand.equalsIgnoreCase("describe")) {
            onDescribeNPC(mage, npc);
            return true;
        }

        if (subCommand.equalsIgnoreCase("player")) {
            if (parameters.length == 0) {
                sender.sendMessage(ChatColor.RED + "Usage: mnpc player <player name>");
                return true;
            }
            onPlayerNPC(mage, npc, parameters[0]);
            return true;
        }

        if (subCommand.equalsIgnoreCase("remove") || subCommand.equalsIgnoreCase("delete")) {
            onRemoveNPC(mage, npc);
            return true;
        }

        if (subCommand.equalsIgnoreCase("dialog") || subCommand.equalsIgnoreCase("text")) {
            onNPCDialog(mage, npc);
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand: mnpc " + subCommand);
        return true;
    }

    protected void onImportNPCs(CommandSender sender) {
        MagicController magic = (MagicController)controller;
        CitizensController citizens = magic.getCitizensController();
        if (citizens == null) {
            sender.sendMessage(ChatColor.RED + "Citizens is not installed");
            return;
        }

        citizens.importAll(magic, controller.getMage(sender));
        sender.sendMessage(ChatColor.GREEN + "Finished importing! If everything looks OK now you can safely remove the Citizens plugin.");
        sender.sendMessage(ChatColor.GREEN + "Until then, all of your NPCs will be doubled up");
    }

    protected List<MagicNPC> getNPCList() {
        Collection<MagicNPC> npcs = controller.getNPCs();
        if (npcs instanceof List) {
            return (List<MagicNPC>)npcs;
        }
        List<MagicNPC> list = new ArrayList<>(npcs);
        return list;
    }

    protected void onListNPCs(CommandSender sender, String[] args) {
        selections.list(sender, args);
    }

    protected void onAddNPC(Mage mage, String name) {
        MagicNPC npc = controller.addNPC(mage, name);
        selections.setSelection(mage.getCommandSender(), npc);
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
        ConfigurationSection currentParameters = npc.getParameters();
        if (parameters.length == 0) {
            String previousSpell = currentParameters.getString("interact_spell");
            if (previousSpell == null || previousSpell.isEmpty()) {
                mage.sendMessage(ChatColor.DARK_AQUA + "NPC has no spell set: " + ChatColor.GOLD);
                return;
            }
            ConfigurationSection previousParameters = ConfigurationUtils.getConfigurationSection(currentParameters, "interact_spell_parameters");
            npc.configure("interact_spell", null);
            mage.sendMessage(ChatColor.DARK_AQUA + "Cleared spell cast for npc " + ChatColor.GOLD);
            mage.sendMessage(ChatColor.AQUA + "Was: " + ChatColor.WHITE + previousSpell);
            if (previousParameters != null) {
                mage.sendMessage(InventoryUtils.describeProperty(previousParameters));
            }
        } else {
            currentParameters.set("interact_spell", parameters[0]);
            mage.sendMessage(ChatColor.GREEN + "Changed npc " + ChatColor.GOLD + npc.getName()
                + ChatColor.GREEN + " to cast " + ChatColor.YELLOW + parameters[0]);

            if (parameters.length > 1) {
                mage.sendMessage(ChatColor.GREEN + " With parameters:");
                ConfigurationSection spellParameters = new MemoryConfiguration();
                ConfigurationUtils.addParameters(Arrays.copyOfRange(parameters, 1, parameters.length), spellParameters);
                currentParameters.set("interact_spell_parameters", spellParameters);
                Set<String> keys = spellParameters.getKeys(false);
                for (String key : keys) {
                    Object value = spellParameters.get(key);
                    mage.sendMessage(ChatColor.DARK_AQUA + key + ChatColor.GRAY + ": " + ChatColor.WHITE + InventoryUtils.describeProperty(value, InventoryUtils.MAX_PROPERTY_DISPLAY_LENGTH));
                }
            }
        }
        npc.update();
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
        MagicNPC npc = null;
        List<MagicNPC> list = selections.getList(mage.getCommandSender());
        if (list != null && name != null && !name.isEmpty()) {
            try {
                int index = Integer.parseInt(name);
                if (index <= 0 || index > list.size()) {
                    mage.sendMessage(ChatColor.RED + "Index out of range: " + ChatColor.WHITE + name
                        + ChatColor.GRAY + "/" + ChatColor.WHITE + list.size());
                    return;
                }
                npc = list.get(index - 1);
            } catch (NumberFormatException ignore) {
            }
        }
        if (npc == null) {
            npc = findNPC(name);
        }
        if (npc == null && mage.isPlayer()) {
            npc = selections.getTarget(mage.getCommandSender(), null);
        }
        if (npc == null) {
            if (name == null || name.isEmpty()) {
                if (!mage.isPlayer()) {
                    mage.sendMessage(ChatColor.RED + "When using from console, must provide NPC name or index");
                } else {
                    mage.sendMessage(ChatColor.RED + "There is no NPC in front of you");
                }
            } else {
                mage.sendMessage(ChatColor.RED + "Could not find NPC: " + ChatColor.GOLD + name);
            }
        } else {
            selections.highlight(npc);
            selections.setSelection(mage.getCommandSender(), npc);
            mage.sendMessage(ChatColor.GRAY + "Selected NPC: " + ChatColor.GOLD + npc.getName());
        }
    }

    protected void onRemoveNPC(Mage mage, MagicNPC npc) {
        controller.removeNPC(npc);
        selections.clearSelection(mage.getCommandSender());
        mage.sendMessage(ChatColor.GREEN + " Removed npc: " + ChatColor.GOLD + npc.getName());
    }

    protected void onNPCDialog(Mage mage, MagicNPC npc) {
        if (!mage.isPlayer()) {
            mage.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return;
        }

        ItemStack item = mage.getPlayer().getInventory().getItemInMainHand();
        if (InventoryUtils.hasMeta(item, "npc")) {
            BookMeta meta = (BookMeta)item.getItemMeta();
            List<String> pages = meta.getPages();
            boolean isEmpty = true;
            for (String page : pages) {
                if (!page.trim().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                pages = null;
                mage.sendMessage(ChatColor.GREEN + "NPC dialog script cleared for " + ChatColor.GOLD + npc.getName());
            } else {
                mage.sendMessage(ChatColor.GREEN + "NPC dialog script set for " + ChatColor.GOLD + npc.getName());
            }
            npc.configure("dialog", pages);
            mage.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            return;
        }

        Material bookMaterial = DefaultMaterials.getWriteableBook();
        if (bookMaterial == null) {
            mage.sendMessage(ChatColor.RED + "Could not create writable book");
            return;
        }
        ItemStack book = new ItemStack(bookMaterial);
        BookMeta meta = (BookMeta)book.getItemMeta();
        meta.setTitle("NPC Script: " + npc.getName());
        meta.setAuthor(mage.getDisplayName());
        List<String> pages = npc.getParameters().getStringList("dialog");
        if (pages == null) {
            pages = new ArrayList<>();
        }
        if (pages.isEmpty()) {
            pages.add("");
        }
        meta.setPages(pages);
        selections.highlight(npc);
        book.setItemMeta(meta);
        book = InventoryUtils.makeReal(book);
        InventoryUtils.setMeta(book, "npc", npc.getUUID().toString());
        mage.giveItem(book);
        mage.sendMessage(ChatColor.GREEN + "Edit the script book and use this command again while holding the book to set the NPC's chat dialog");
    }

    protected void onTPNPC(Mage mage, MagicNPC npc) {
        if (!mage.isPlayer()) {
            mage.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return;
        }
        mage.getEntity().teleport(npc.getLocation());
    }

    protected void onMoveNPC(Mage mage, MagicNPC npc) {
        if (!mage.isPlayer()) {
            mage.sendMessage(ChatColor.RED + "This command may only be used in-game");
            return;
        }
        npc.teleport(mage.getEntity().getLocation());
    }

    protected void onDescribeNPC(Mage mage, MagicNPC npc) {
        npc.describe(mage.getCommandSender());
        selections.highlight(npc);
    }

    protected void onPlayerNPC(Mage mage, MagicNPC npc, String playerName) {
        if (!controller.hasDisguises()) {
            mage.sendMessage(ChatColor.RED + "Player NPCs require LibsDisguises");
            return;
        }
        ConfigurationSection parameters = npc.getParameters();
        ConfigurationSection disguise = parameters.getConfigurationSection("disguise");
        disguise.set("skin", playerName);
        disguise.set("type", "player");
        npc.update();
    }

    protected void onNPCCost(Mage mage, MagicNPC npc, String[] parameters) {
        if (parameters.length == 0) {
            mage.sendMessage(ChatColor.GREEN + " Configured npc " + ChatColor.GOLD + npc.getName() + ChatColor.GREEN + ", cleared costs");
            npc.configure("interact_costs", null);
            return;
        }
        double value = 0;
        try {
            value = Double.parseDouble(parameters[0]);
        } catch (Exception ex) {
            mage.sendMessage(ChatColor.RED + "Invalid cost amount: " + parameters[0]);
            return;
        }
        String costType = parameters.length > 1 ? parameters[1] : "currency";
        if (controller.getCurrency(costType) == null) {
            mage.sendMessage(ChatColor.RED + "Invalid cost type: " + costType);
            return;
        }
        ConfigurationSection costSection = new MemoryConfiguration();
        costSection.set(costType, value);
        npc.configure("interact_costs", costSection);
        mage.sendMessage(ChatColor.GREEN + " Configured npc " + ChatColor.GOLD + npc.getName()
            + ChatColor.GREEN + " to cost " + ChatColor.WHITE + ((int)value) + ChatColor.YELLOW + " " + costType);
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

        // Some special helper cases
        if (key.equals("spell") || key.equals("cast")) {
            key = "interact_spell";
        } else if (key.equals("command") || key.equals("commands")) {
            key = "interact_commands";
        } else if (key.equals("cast_source")) {
            key = "interact_spell_source";
        } else if (key.equals("cast_target")) {
            key = "interact_spell_target";
        } else if (key.equals("command_source")) {
            key = "interact_command_source";
        } else if (key.equals("permission")) {
            key = "interact_permission";
        }
        selections.highlight(npc);
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
            options.add("move");
            options.add("import");
            options.add("select");
            options.add("cast");
            options.add("costs");
            options.add("player");
            options.add("dialog");
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
            options.add("cast_source");
            options.add("command_source");
            options.add("cast_target");
            options.add("commands");
            options.add("permission");
            options.add("helmet");
            options.add("item");
            options.add("offhand");
            options.add("chestplate");
            options.add("boots");
            options.add("leggings");
        } else if ((args.length == 3 && args[0].equals("configure") && args[1].equals("interact_spell"))
               || (args.length == 2 && args[0].equals("cast"))
               || (args.length == 2 && args[0].equals("spell"))) {
            for (SpellTemplate spell : controller.getSpellTemplates(true)) {
                options.add(spell.getKey());
            }
        } else if (args.length == 3 && args[0].equals("configure")
               && (args[1].equals("ai") || args[1].equals("gravity") || args[1].equals("interact_spell_caster"))) {
            options.add("true");
            options.add("false");
        } else if (args.length == 3 && args[0].equals("configure") && (args[1].equals("interact_spell_target") || args[1].equals("cast_target"))) {
            options.add("none");
            options.add("npc");
            options.add("player");
        } else if (args.length == 3 && args[0].equals("configure") && (args[1].equals("interact_spell_source") || args[1].equals("cast_source")
                || args[1].equals("interact_command_source") || args[1].equals("command_source"))) {
            options.add("npc");
            options.add("player");
        } else if (args.length == 3 && args[0].equals("configure")
               && (args[1].equals("helmet") || args[1].equals("item") || args[1].equals("offhand")
                    || args[1].equals("chestplate") || args[1].equals("boots") || args[1].equals("leggings"))
        ) {
            Collection<SpellTemplate> spellList = api.getSpellTemplates(true);
            for (SpellTemplate spell : spellList) {
                options.add(spell.getKey());
            }
            Collection<String> allWands = api.getWandKeys();
            for (String wandKey : allWands) {
               options.add(wandKey);
            }
            for (Material material : Material.values()) {
                options.add(material.name().toLowerCase());
            }
            Collection<String> allItems = api.getController().getItemKeys();
            for (String itemKey : allItems) {
                options.add(itemKey);
            }
        } else if (args.length == 2 && args[0].equals("player")) {
            for (Player player : controller.getPlugin().getServer().getOnlinePlayers()) {
                options.add(player.getName());
            }
        } else if (args.length == 2 && args[0].equals("costs")) {
            options.add("1");
            options.add("10");
            options.add("100");
        } else if (args.length == 3 && args[0].equals("costs")) {
            for (String currency : controller.getCurrencyKeys()) {
                options.add(currency);
            }
            Collection<String> allWands = api.getWandKeys();
            for (String wandKey : allWands) {
               options.add(wandKey);
            }
            for (Material material : Material.values()) {
                options.add(material.name().toLowerCase());
            }
            Collection<String> allItems = api.getController().getItemKeys();
            for (String itemKey : allItems) {
                options.add(itemKey);
            }
        }
        return options;
    }
}
