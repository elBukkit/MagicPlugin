package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

import de.slikey.effectlib.math.EquationStore;
import de.slikey.effectlib.math.EquationTransform;

public class MageCommandExecutor extends MagicConfigurableExecutor {
    public MageCommandExecutor(MagicAPI api) {
        super(api, "mage");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
        {
            if (!api.hasPermission(sender, getPermissionNode())) {
                sendNoPermission(sender);
                return true;
            }
            return false;
        }

        String subCommand = args[0];
        if (sender instanceof Player)
        {
            if (!api.hasPermission(sender, "Magic.commands.mage." + subCommand)) {
                sendNoPermission(sender);
                return true;
            }
        }

        Player player = null;
        int argStart = 1;

        if (sender instanceof Player) {
            if (args.length > 1 && sender.hasPermission("Magic.commands.mage.others"))
            {
                player = DeprecatedUtils.getPlayer(args[1]);
            }
            if (player == null)
            {
                player = (Player)sender;
            }
            else
            {
                argStart = 2;
            }
        } else {
            if (args.length <= 1) {
                sender.sendMessage("Must specify a player name");
                return true;
            }
            argStart = 2;
            player = DeprecatedUtils.getPlayer(args[1]);
            if (player == null) {
                sender.sendMessage("Can't find player " + args[1]);
                return true;
            }
            if (!player.isOnline()) {
                sender.sendMessage("Player " + args[1] + " is not online");
                return true;
            }
        }

        String[] args2 = Arrays.copyOfRange(args, argStart, args.length);

        if (subCommand.equalsIgnoreCase("check"))
        {
            return onMageCheck(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("reset"))
        {
            return onMageReset(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("debug"))
        {
            return onMageDebug(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("getdata"))
        {
            return onMageGetData(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("setdata"))
        {
            return onMageSetData(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("attribute"))
        {
            return onMageAttribute(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("unbind"))
        {
            return onMageUnbind(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("configure"))
        {
            return onMageConfigure(sender, player, args2, false);
        }
        if (subCommand.equalsIgnoreCase("upgrade"))
        {
            return onMageConfigure(sender, player, args2, true);
        }
        if (subCommand.equalsIgnoreCase("describe"))
        {
            return onMageDescribe(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("activate"))
        {
            return onMageActivate(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("unlock"))
        {
            return onMageUnlock(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("lock"))
        {
            return onMageLock(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("switch"))
        {
            return onMageSwitch(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("add"))
        {
            return onMageAdd(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("remove"))
        {
            return onMageRemove(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("levelspells"))
        {
            return onMageLevelSpells(sender, player, args2);
        }
        if (subCommand.equalsIgnoreCase("clear"))
        {
            return onMageClear(sender, player, args2);
        }

        sender.sendMessage("Unknown mage command: " + subCommand);
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mage.", "add");
            addIfPermissible(sender, options, "Magic.commands.mage.", "remove");
            addIfPermissible(sender, options, "Magic.commands.mage.", "configure");
            addIfPermissible(sender, options, "Magic.commands.mage.", "describe");
            addIfPermissible(sender, options, "Magic.commands.mage.", "upgrade");
            addIfPermissible(sender, options, "Magic.commands.mage.", "getdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "setdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "check");
            addIfPermissible(sender, options, "Magic.commands.mage.", "debug");
            addIfPermissible(sender, options, "Magic.commands.mage.", "reset");
            addIfPermissible(sender, options, "Magic.commands.mage.", "clear");
            addIfPermissible(sender, options, "Magic.commands.mage.", "unbind");
            addIfPermissible(sender, options, "Magic.commands.mage.", "activate");
            addIfPermissible(sender, options, "Magic.commands.mage.", "unlock");
            addIfPermissible(sender, options, "Magic.commands.mage.", "lock");
            addIfPermissible(sender, options, "Magic.commands.mage.", "levelspells");
            addIfPermissible(sender, options, "Magic.commands.mage.", "attribute");
        } else if (args.length == 2 && sender.hasPermission("Magic.commands.mage.others")) {
            options.addAll(api.getPlayerNames());
        }

        if (args.length == 3 || args.length == 2) {
            CommandSender target = args.length == 2 ? sender : DeprecatedUtils.getPlayer(args[1]);
            String subCommand = args[0];
            String subCommandPNode = "Magic.commands.mage." + subCommand;
            if (subCommand.equalsIgnoreCase("setdata") || subCommand.equalsIgnoreCase("getdata")) {
                if (target != null) {
                    Mage mage = controller.getMage(target);
                    ConfigurationSection data = mage.getData();
                    options.addAll(data.getKeys(false));
                }
            }

            if (subCommand.equalsIgnoreCase("add")) {
                Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
                for (SpellTemplate spell : spellList) {
                    addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
                }
                addIfPermissible(sender, options, subCommandPNode, "brush", true);
            }

            if (subCommand.equalsIgnoreCase("remove")) {
                if (target != null) {
                    Mage mage = controller.getMage(target);
                    MageClass mageClass = mage.getActiveClass();
                    if (mageClass != null) {
                        options.addAll(mageClass.getSpells());
                    }
                }
                options.add("brush");
            }

            if (subCommand.equalsIgnoreCase("clear")) {
                options.add("all");
                options.add("magic");
                options.add("skills");
                options.add("wands");
            }

            if (subCommand.equalsIgnoreCase("configure") || subCommand.equalsIgnoreCase("describe") || subCommand.equalsIgnoreCase("upgrade")) {
                for (String key : BaseMagicProperties.PROPERTY_KEYS) {
                    options.add(key);
                }

                for (String protection : api.getController().getDamageTypes()) {
                    options.add("protection." + protection);
                }
            }

            if (subCommand.equalsIgnoreCase("attribute")) {
                for (String attribute : api.getController().getAttributes()) {
                    options.add(attribute);
                }
            }

            if (subCommand.equalsIgnoreCase("add")) {
                Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
                for (SpellTemplate spell : spellList) {
                    addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
                }
                addIfPermissible(sender, options, subCommandPNode, "brush", true);
            }

            if (args[0].equalsIgnoreCase("lock")
                    || args[0].equalsIgnoreCase("unlock")
                    || args[0].equalsIgnoreCase("activate")) {
                options.addAll(api.getController().getMageClassKeys());
            }
        }
        return options;
    }

    public boolean onMageLevelSpells(CommandSender sender, Player player, String[] parameters)
    {
        Integer maxLevel = null;
        if (parameters.length > 0) {
            try {
                maxLevel = Integer.parseInt(parameters[0]);
            } catch (Exception ex) {
                sender.sendMessage("Usage: /wand levelspells <level>");
                return true;
            }
        }

        Mage mage = controller.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        return onLevelSpells("mage", sender, player, activeClass == null ? mage.getProperties() : activeClass, maxLevel);
    }

    public boolean onMageClear(CommandSender sender, Player player, String[] parameters)
    {
        String type = "magic";
        if (parameters.length > 0) {
            type = parameters[0];
        }
        if (!type.equalsIgnoreCase("magic") && !type.equalsIgnoreCase("all")
                && !type.equalsIgnoreCase("wands") && !type.equalsIgnoreCase("skills")) {
            sender.sendMessage(ChatColor.RED + "Unknown clear type: " + ChatColor.WHITE + type + ChatColor.RED + ", expected one of: "
                + ChatColor.AQUA + "all,magic,wands,skills");
        }

        int cleared = 0;
        Mage mage = controller.getMage(player);
        mage.deactivate();;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (type.equalsIgnoreCase("all")) {
                inventory.setItem(i, null);
                continue;
            }
            ItemStack item = inventory.getItem(i);
            if (CompatibilityUtils.isEmpty(item)) continue;

            if ((type.equalsIgnoreCase("wands") && controller.isWand(item))
            || (type.equalsIgnoreCase("skills") && controller.isSkill(item))
            || (type.equalsIgnoreCase("magic") && controller.isMagic(item))) {
                inventory.setItem(i, null);
                cleared++;
                continue;
            }
        }
        mage.checkWand();
        sender.sendMessage(ChatColor.AQUA + "Cleared " + ChatColor.WHITE + cleared + " " + ChatColor.DARK_AQUA + type
            + ChatColor.AQUA + " items from inventory of " + ChatColor.GOLD + player.getName());
        return true;
    }

    public boolean onMageCheck(CommandSender sender, Player player, String[] args)
    {
        Mage mage = controller.getMage(player);
        mage.debugPermissions(sender, null);
        return true;
    }

    public boolean onMageReset(CommandSender sender, Player player, String[] args)
    {
        if (args.length == 0) {
            api.getController().deleteMage(player.getUniqueId().toString());
            sender.sendMessage(ChatColor.RED + "Reset player " + player.getName());
        } else {
            Mage mage = controller.getMage(player);
            if (mage.removeClass(args[0])) {
                sender.sendMessage(ChatColor.RED + "Reset " + ChatColor.GOLD + "class " + args[0] + " for player " + player.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Player " + player.getName() + " does not have class " + args[0] + " unlocked");
            }
        }
        return true;
    }

    public boolean onMageDebug(CommandSender sender, Player player, String[] args)
    {
        Mage mage = controller.getMage(player);
        if (args.length > 0) {
            try {
                int level = Integer.parseInt(args[0]);
                mage.setDebugLevel(level);
                if (level > 0) {
                    mage.setDebugger(sender);
                } else {
                    mage.setDebugger(null);
                }
                sender.sendMessage(ChatColor.GOLD + "Setting debug level for  " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + " to " + ChatColor.GREEN + Integer.toString(level));
            } catch (Exception ex) {
                sender.sendMessage("Expecting integer, got: " + args[0]);
            }
            return true;
        }
        if (mage.getDebugLevel() > 0) {
            sender.sendMessage(ChatColor.GOLD + "Disabling debug for " + ChatColor.AQUA + player.getDisplayName());
            mage.setDebugLevel(0);
            mage.setDebugger(null);
        } else {
            sender.sendMessage(ChatColor.AQUA + "Enabling debug for " + ChatColor.AQUA + player.getDisplayName());
            mage.setDebugLevel(1);
            mage.setDebugger(sender);
        }
        return true;
    }

    public boolean onMageGetData(CommandSender sender, Player player, String[] args)
    {
        Mage mage = controller.getMage(player);
        ConfigurationSection data = mage.getData();
        if (args != null && args.length > 0)
        {
            if (args[0].equals("*"))
            {
                sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + ": ");
                Collection<Spell> spells = mage.getSpells();
                if (spells.size() == 0) {
                    sender.sendMessage(ChatColor.RED + "No spell casts!");
                    return true;
                }
                for (Spell spell : spells) {
                    sender.sendMessage(ChatColor.LIGHT_PURPLE + spell.getName() + ChatColor.AQUA + " Cast Count: " + ChatColor.GOLD + spell.getCastCount());
                }
                return true;
            }
            Spell spell = mage.getSpell(args[0]);
            if (spell != null)
            {
                sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName() + ChatColor.GOLD + ": " + ChatColor.LIGHT_PURPLE + spell.getName());
                sender.sendMessage(ChatColor.AQUA + " Cast Count: " + ChatColor.GOLD + spell.getCastCount());
                return true;
            }
            ConfigurationSection subSection = data.getConfigurationSection(args[0]);
            if (subSection == null) {
                sender.sendMessage(ChatColor.RED + "Unknown subsection or spell: " + args[0]);
                return true;
            }
            data = subSection;
        }
        Collection<String> keys = data.getKeys(false);
        sender.sendMessage(ChatColor.GOLD + "Mage data for " + ChatColor.AQUA + player.getDisplayName());
        for (String key : keys) {
            if (data.isConfigurationSection(key)) {
                ConfigurationSection subSection = data.getConfigurationSection(key);
                sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + " (" + subSection.getKeys(true).size() + " items)");
            } else {
                String value = data.getString(key);
                if (value != null) {
                    sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + " (" + value + ")");
                } else {
                    sender.sendMessage(ChatColor.AQUA + " " + key);
                }
            }
        }
        return true;
    }

    public boolean onMageSetData(CommandSender sender, Player player, String[] args)
    {
        Mage mage = controller.getMage(player);
        if (args.length == 1)
        {
            ConfigurationSection data = mage.getData();
            String key = args[0];
            if (!data.contains(key)) {
                sender.sendMessage(ChatColor.RED + "No data found with key " + ChatColor.AQUA + key + ChatColor.RED + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
                return true;
            }
            data.set(key, null);
            sender.sendMessage(ChatColor.GOLD + "Removed data for key " + ChatColor.AQUA + key + ChatColor.GOLD  + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }
        if (args.length != 2)
        {
            return false;
        }
        if (args[0].equals("*"))
        {
            long value = 0;
            try {
                value = Long.parseLong(args[1]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Cast count must be a number");
                return true;
            }
            Collection<Spell> spells = mage.getSpells();
            for (Spell spell : spells)
            {
                spell.setCastCount(value);
            }
            sender.sendMessage(ChatColor.GOLD + "Set all spell cast counts to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }
        Spell spell = mage.getSpell(args[0]);
        if (spell != null)
        {
            long value = 0;
            try {
                value = Long.parseLong(args[1]);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "Cast count must be a number");
                return true;
            }
            spell.setCastCount(value);
            sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.AQUA + spell.getName() + ChatColor.GOLD + " cast count to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
            return true;
        }

        ConfigurationSection data = mage.getData();
        String key = args[0];
        String value = args[1];
        ConfigurationUtils.set(data, key, value);
        sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.AQUA + key + ChatColor.GOLD + " to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
        return true;
    }

    public boolean onMageAttribute(CommandSender sender, Player player, String[] args)
    {
        Mage mage = controller.getMage(player);
        Set<String> attributes = api.getController().getAttributes();
        if (attributes.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No attributes configured, see attributes.yml");
            return true;
        }
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.GOLD + "Attributes for: " + ChatColor.AQUA + player.getName());
            for (String key : attributes) {
                Double value = mage.getAttribute(key);
                String valueDescription = value == null ? ChatColor.RED + "(not set)" : ChatColor.AQUA + Double.toString(value);
                sender.sendMessage(ChatColor.DARK_AQUA + key + ChatColor.BLUE + " = " + valueDescription);
            }
            return true;
        }
        String key = args[0];
        if (args.length == 1)
        {
            if (!attributes.contains(key)) {
                sender.sendMessage(ChatColor.RED + "Unknown attribute: " + ChatColor.YELLOW + key);
                return true;
            }
            Double value = mage.getAttribute(key);
            String valueDescription = value == null ? ChatColor.RED + "(not set)" : ChatColor.AQUA + Double.toString(value);
            sender.sendMessage(ChatColor.AQUA + player.getName() + " has " + ChatColor.DARK_AQUA + key + ChatColor.BLUE + " of " + valueDescription);
            return true;
        }

        MageClass activeClass = mage.getActiveClass();
        CasterProperties attributeProperties = activeClass == null ? mage.getProperties() : activeClass;

        String value = args[1];
        for (int i = 2; i < args.length; i++) {
            value = value + " " + args[i];
        }
        if (value.equals("-"))
        {
            Double oldValue = attributeProperties.getAttribute(key);
            attributeProperties.setAttribute(key, null);
            String valueDescription = oldValue == null ? ChatColor.RED + "(not set)" : ChatColor.AQUA + Double.toString(oldValue);
            sender.sendMessage(ChatColor.BLUE + "Removed attribute " + ChatColor.DARK_AQUA + key + ChatColor.BLUE + ", was " + valueDescription);
            return true;
        }

        double transformed = Double.NaN;
        try {
            transformed = Double.parseDouble(value);
        } catch (Exception ex) {
            EquationTransform transform = EquationStore.getInstance().getTransform(value);
            if (transform.getException() == null) {
                Double property = attributeProperties.getAttribute(key);
                if (property == null || Double.isNaN(property)) {
                    property = 0.0;
                }
                transform.setVariable("x", property);
                transformed = transform.get();
            }
        }

        if (Double.isNaN(transformed)) {
            sender.sendMessage(ChatColor.RED + "Could not set " + ChatColor.YELLOW + key + ChatColor.RED + " to " + ChatColor.YELLOW + value);
            return true;
        }
        attributeProperties.setAttribute(key, transformed);

        sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.DARK_AQUA + key + ChatColor.GOLD + " to " + ChatColor.AQUA + transformed + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
        return true;
    }

    public boolean onMageUnbind(CommandSender sender, Player player, String[] parameters)
    {
        Mage mage = controller.getMage(player);
        if (parameters.length > 0) {
            String template = parameters[0];
            if (mage.unbind(template)) {
                mage.sendMessage(api.getMessages().get("wand.unbound"));
                if (sender != player) {
                    sender.sendMessage(api.getMessages().getParameterized("wand.player_unbound", "$name", player.getName()));
                }
            } else {
                mage.sendMessage(api.getMessages().get("wand.notunbound").replace("$wand", parameters[0]));
                if (sender != player) {
                    sender.sendMessage(api.getMessages().getParameterized("wand.player_notunbound", "$name", player.getName()).replace("$wand", parameters[0]));
                }
            }
            return true;
        }

        mage.unbindAll();

        mage.sendMessage(api.getMessages().get("wand.unboundall"));
        if (sender != player) {
            sender.sendMessage(api.getMessages().getParameterized("wand.player_unboundall", "$name", player.getName()));
        }
        return true;
    }

    public boolean onMageConfigure(CommandSender sender, Player player, String[] parameters, boolean safe)
    {
        Mage mage = controller.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        return onConfigure("mage", activeClass == null ? mage.getProperties() : activeClass, sender, player, parameters, safe);
    }

    public boolean onMageUnlock(CommandSender sender, Player player, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mage unlock [player] <class>");
            return true;
        }
        Mage mage = controller.getMage(player);
        String classKey = parameters[0];
        MageClass mageClass = mage.unlockClass(classKey);
        if (mageClass == null) {
            sender.sendMessage(ChatColor.RED + "Invalid class: " + ChatColor.WHITE + classKey);
        } else {
            sender.sendMessage("Unlocked class " + classKey + " for " + player.getName());
        }
        return true;
    }

    public boolean onMageLock(CommandSender sender, Player player, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mage lock [player] <class>");
            return true;
        }
        Mage mage = controller.getMage(player);
        String classKey = parameters[0];
        boolean locked = mage.lockClass(classKey);
        if (!locked) {
            sender.sendMessage(ChatColor.RED + "No unlocked class: " + ChatColor.WHITE + classKey + ChatColor.RED + " for " + ChatColor.WHITE + player.getName());
        } else {
            sender.sendMessage("Locked class " + classKey + " for " + player.getName());
        }
        return true;
    }

    public boolean onMageSwitch(CommandSender sender, Player player, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: " + ChatColor.WHITE + "/mage switch [player] <class>");
            return true;
        }
        String classKey = parameters[0];
        Mage mage = controller.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        if (activeClass != null && activeClass.getKey().equals(classKey)) {
            sender.sendMessage(ChatColor.RED + "Player "  + ChatColor.WHITE + player.getName() + ChatColor.RED + " already has class active: " + ChatColor.WHITE + classKey);
            return true;
        }

        if (activeClass != null) {
            mage.lockClass(activeClass.getKey());
        }

        MageClass targetClass = mage.unlockClass(classKey);
        if (targetClass == null) {
            sender.sendMessage(ChatColor.RED + "Invalid class: " + ChatColor.WHITE + classKey);

            if (activeClass != null) {
                mage.unlockClass(activeClass.getKey());
            }
            return true;
        }
        mage.setActiveClass(targetClass.getKey());
        mage.deactivate();
        mage.checkWand();
        sender.sendMessage("Switched class to " + classKey + " for " + player.getName());

        return true;
    }

    public boolean onMageActivate(CommandSender sender, Player player, String[] parameters)
    {
        Mage mage = controller.getMage(player);
        String classKey = parameters.length == 0 ? null : parameters[0];
        if (mage.setActiveClass(classKey)) {
            if (classKey == null) {
                sender.sendMessage("Cleared active class for " + player.getName());
            } else {
                sender.sendMessage("Activated class " + classKey + " for " + player.getName());
            }
        } else {
            sender.sendMessage(ChatColor.RED + player.getName() + " does not have class: " + ChatColor.WHITE + classKey + ChatColor.RED + " unlocked");
        }
        return true;
    }

    public boolean onMageDescribe(CommandSender sender, Player player, String[] parameters) {
        Mage mage = controller.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        MagicProperties mageProperties = mage.getProperties();

        if (parameters.length == 0) {
            sender.sendMessage(ChatColor.BLUE + "Use " + ChatColor.AQUA + "/mage describe <property>" + ChatColor.BLUE + " for specific properties");
            sender.sendMessage(ChatColor.BLUE + "Use " + ChatColor.AQUA + "/mage activate" + ChatColor.BLUE + " to change or clear the active class");
            Collection<String> classKeys = mage.getClassKeys();
            if (classKeys.size() > 0) {
                Collection<String> coloredClasses = new ArrayList<>();
                for (String classKey : classKeys) {
                    ChatColor color = mage.hasClassUnlocked(classKey) ? ChatColor.GREEN : ChatColor.GRAY;
                    coloredClasses.add(color + classKey);
                }
                sender.sendMessage(ChatColor.AQUA + "Classes: " + ChatColor.GREEN + StringUtils.join(coloredClasses, ","));
            }
            if (!mageProperties.isEmpty()) {
                 sender.sendMessage(ChatColor.AQUA + "Mage properties:");
                 mageProperties.describe(sender, BaseMagicProperties.HIDDEN_PROPERTY_KEYS);
            }
            if (activeClass != null) {
                sender.sendMessage(ChatColor.AQUA + "Active class: " + ChatColor.GREEN + activeClass.getKey());
            } else {
                sender.sendMessage(ChatColor.DARK_GREEN + "No active class");
            }
            Set<Spell> activeSpells = mage.getActiveSpells();
            if (activeSpells != null && !activeSpells.isEmpty()) {
                Collection<String> spellNames = new ArrayList<>();
                for (Spell spell : activeSpells) {
                    spellNames.add(spell.getName());
                }
                sender.sendMessage(ChatColor.AQUA + "Active spells: " + ChatColor.DARK_AQUA + StringUtils.join(spellNames, ","));
            }
            if (activeClass != null) {
                activeClass.describe(sender, BaseMagicProperties.HIDDEN_PROPERTY_KEYS);
            }
        } else {
            Object property = activeClass.getProperty(parameters[0]);
            if (property == null) {
                sender.sendMessage(ChatColor.DARK_AQUA + parameters[0] + ChatColor.GRAY + ": " + ChatColor.RED + "(Not Set)");
            } else {
                sender.sendMessage(ChatColor.DARK_AQUA + parameters[0] + ChatColor.GRAY + ": " + ChatColor.WHITE + InventoryUtils.describeProperty(property));
            }
        }

        return true;
    }

    public boolean onMageAdd(CommandSender sender, Player player, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage("Use: /mage add <spell|material> [material:data]");
            return true;
        }

        Mage mage = controller.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        if (activeClass == null) {
            sender.sendMessage("Player " + player.getName() + " has no active class");
            return true;
        }

        String spellName = parameters[0];
        if (spellName.equals("material") || spellName.equals("brush")) {
            if (parameters.length < 2) {
                sender.sendMessage("Use: /mage add brush <material:data>");
                return true;
            }

            String materialKey = parameters[1];
            if (!MaterialBrush.isValidMaterial(materialKey, false)) {
                sender.sendMessage(materialKey + " is not a valid brush");
                return true;
            }

            if (activeClass.addBrush(materialKey)) {
                if (sender != player) {
                    sender.sendMessage("Added brush '" + materialKey + "' to " + player.getName());
                } else {
                    sender.sendMessage(api.getMessages().get("mage.brush_added").replace("$name", materialKey));
                }
            }

            return true;
        }
        Spell spell = mage.getSpell(spellName);
        if (spell == null)
        {
            sender.sendMessage("Spell '" + spellName + "' unknown, Use /spells for spell list");
            return true;
        }

        SpellTemplate currentSpell = activeClass.getSpellTemplate(spellName);
        if (activeClass.addSpell(spellName)) {
            if (currentSpell != null) {
                String levelDescription = spell.getLevelDescription();
                if (levelDescription == null || levelDescription.isEmpty()) {
                    levelDescription = spell.getName();
                }
                if (sender != player) {
                    sender.sendMessage(api.getMessages().get("mage.player_spell_upgraded").replace("$player", player.getName()).replace("$name", currentSpell.getName()).replace("$level", levelDescription));
                }
            } else {
                if (sender != player) {
                    sender.sendMessage("Added '" + spell.getName() + "' to " + player.getName());
                }
            }
        } else if (sender != player) {
            sender.sendMessage("Could not add " + spellName + " to " + player.getName());
        }
        return true;
    }

    public boolean onMageRemove(CommandSender sender, Player player, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage("Use: /mage remove <spell|material> [material:data]");
            return true;
        }

        Mage mage = controller.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        if (activeClass == null) {
            sender.sendMessage("Can't modify player " + player.getName());
            return true;
        }

        String spellName = parameters[0];
        if (spellName.equals("material") || spellName.equals("brush")) {
            if (parameters.length < 2) {
                sender.sendMessage("Use: /mage remove brush <material:data>");
                return true;
            }
            String materialKey = parameters[1];

            if (activeClass.removeBrush(materialKey)) {
                mage.sendMessage("Brush '" + materialKey + "' has been removed");
                if (sender != player) {
                    sender.sendMessage("Removed brush '" + materialKey + "' from " + player.getName());
                }
            } else {
                if (sender != player) {
                    sender.sendMessage(player.getName() + " does not have brush " + materialKey);
                }
            }

            return true;
        }

        if (activeClass.removeSpell(spellName)) {
            SpellTemplate template = api.getSpellTemplate(spellName);
            if (template != null) {
                spellName = template.getName();
            }
            mage.sendMessage("Spell '" + spellName + "' has been removed");
            if (sender != player) {
                sender.sendMessage("Removed '" + spellName + "' from " + player.getName());
            }
        } else {
            if (sender != player) {
                sender.sendMessage(player.getName() + " does not have " + spellName);
            }
        }

        return true;
    }
}
