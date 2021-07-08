package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageModifier;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

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

        int argStart = 1;
        List<Player> players = new ArrayList<>();
        String playerName = args.length > 1 ? args[1] : null;
        if (playerName != null && sender.hasPermission("Magic.commands.mage.others")) {
            List<Entity> targets = CompatibilityLib.getCompatibilityUtils().selectEntities(sender, playerName);
            if (targets != null) {
                argStart = 2;
                for (Entity entity : targets) {
                    if (entity instanceof Player) {
                        players.add((Player)entity);
                    }
                }
            } else {
                Player player = CompatibilityLib.getDeprecatedUtils().getPlayer(playerName);
                if (player != null) {
                    argStart = 2;
                    players.add(player);
                }
            }

            if (players.isEmpty() && subCommand.equalsIgnoreCase("reset")) {
                // Special-case for resetting an offline player
                if (args.length == 2) {
                    try {
                        UUID uuid = UUID.fromString(playerName);
                        api.getController().deleteMage(uuid.toString());
                        sender.sendMessage(ChatColor.RED + "Reset offline player id " + uuid.toString());
                        return true;
                    } catch (Exception ignore) {
                    }
                    OfflinePlayer offlinePlayer = CompatibilityLib.getDeprecatedUtils().getOfflinePlayer(playerName);
                    if (offlinePlayer != null) {
                        api.getController().deleteMage(offlinePlayer.getUniqueId().toString());
                        sender.sendMessage(ChatColor.RED + "Reset offline player "
                            + offlinePlayer.getName() + ChatColor.GRAY + " (" + offlinePlayer.getUniqueId().toString() + ")");
                        return true;
                    }
                }
            }
        }

        String[] args2 = Arrays.copyOfRange(args, argStart, args.length);
        if (players.isEmpty()) {
            if (!(sender instanceof Player)) {
                if (subCommand.equalsIgnoreCase("debug")) {
                    onMageDebug(sender, sender, args2);
                    return true;
                }
                if (playerName == null) {
                    sender.sendMessage("Must specify a player name");
                } else {
                    sender.sendMessage("No players matched: " + playerName);
                }
                return true;
            }
            players.add((Player)sender);
        }

        boolean handled = false;
        for (Player player : players) {
            if (subCommand.equalsIgnoreCase("check"))
            {
                onMageCheck(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("bypass"))
            {
                onMageBypass(sender, player);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("reset"))
            {
                onMageReset(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("modifier"))
            {
                onMageModifier(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("debug"))
            {
                onMageDebug(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("getdata"))
            {
                onMageGetData(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("setdata"))
            {
                onMageSetData(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("attribute"))
            {
                onMageAttribute(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("unbind"))
            {
                onMageUnbind(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("configure"))
            {
                onMageConfigure(sender, player, args2, false);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("upgrade"))
            {
                onMageConfigure(sender, player, args2, true);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("promote"))
            {
                onMagePromote(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("describe") || subCommand.equalsIgnoreCase("desc"))
            {
                onMageDescribe(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("activate"))
            {
                onMageActivate(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("deactivate"))
            {
                onMageDeactivate(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("unlock"))
            {
                onMageUnlock(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("lock"))
            {
                onMageLock(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("switch"))
            {
                onMageSwitch(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("add"))
            {
                onMageAdd(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("reward"))
            {
                onMageReward(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("remove"))
            {
                onMageRemove(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("levelspells"))
            {
                onMageLevelSpells(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("clear"))
            {
                onMageClear(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("forget"))
            {
                onMageForget(sender, player, args2);
                handled = true;
            }
            if (subCommand.equalsIgnoreCase("skin"))
            {
                onMageSkin(sender, player);
                handled = true;
            }
        }

        if (!handled) {
            sender.sendMessage("Unknown mage command: " + subCommand);
            return false;
        }
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            addIfPermissible(sender, options, "Magic.commands.mage.", "add");
            addIfPermissible(sender, options, "Magic.commands.mage.", "reward");
            addIfPermissible(sender, options, "Magic.commands.mage.", "remove");
            addIfPermissible(sender, options, "Magic.commands.mage.", "configure");
            addIfPermissible(sender, options, "Magic.commands.mage.", "describe");
            addIfPermissible(sender, options, "Magic.commands.mage.", "desc");
            addIfPermissible(sender, options, "Magic.commands.mage.", "upgrade");
            addIfPermissible(sender, options, "Magic.commands.mage.", "modifier");
            addIfPermissible(sender, options, "Magic.commands.mage.", "getdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "setdata");
            addIfPermissible(sender, options, "Magic.commands.mage.", "check");
            addIfPermissible(sender, options, "Magic.commands.mage.", "debug");
            addIfPermissible(sender, options, "Magic.commands.mage.", "reset");
            addIfPermissible(sender, options, "Magic.commands.mage.", "clear");
            addIfPermissible(sender, options, "Magic.commands.mage.", "unbind");
            addIfPermissible(sender, options, "Magic.commands.mage.", "activate");
            addIfPermissible(sender, options, "Magic.commands.mage.", "deactivate");
            addIfPermissible(sender, options, "Magic.commands.mage.", "unlock");
            addIfPermissible(sender, options, "Magic.commands.mage.", "lock");
            addIfPermissible(sender, options, "Magic.commands.mage.", "switch");
            addIfPermissible(sender, options, "Magic.commands.mage.", "levelspells");
            addIfPermissible(sender, options, "Magic.commands.mage.", "attribute");
            addIfPermissible(sender, options, "Magic.commands.mage.", "bypass");
            addIfPermissible(sender, options, "Magic.commands.mage.", "promote");
            addIfPermissible(sender, options, "Magic.commands.mage.", "forget");
        } else if (args.length == 2 && sender.hasPermission("Magic.commands.mage.others")) {
            options.addAll(api.getPlayerNames());
        }

        if (args.length >= 2) {
            CommandSender target = sender;
            String subCommand = args[0];
            args = Arrays.copyOfRange(args, 1, args.length);
            if (args.length > 1) {
                Player targetPlayer = CompatibilityLib.getDeprecatedUtils().getPlayer(args[0]);
                if (targetPlayer != null) {
                    target = targetPlayer;
                    args = Arrays.copyOfRange(args, 1, args.length);
                }
            }

            String subCommandPNode = "Magic.commands.mage." + subCommand;
            if (subCommand.equalsIgnoreCase("setdata") || subCommand.equalsIgnoreCase("getdata")) {
                if (target != null) {
                    Mage mage = controller.getMage(target);
                    if (args.length == 2) {
                        MageSpell mageSpell = mage.getSpell(args[0]);
                        if (mageSpell != null) {
                            options.addAll(mageSpell.getVariables().getKeys(false));
                        }
                    } else if (args.length == 1) {
                        ConfigurationSection data = mage.getData();
                        options.addAll(data.getKeys(false));
                        Collection<Spell> spells = mage.getSpells();
                        for (Spell spell : spells) {
                            options.add(spell.getKey());
                        }
                    }
                }
            }

            if (subCommand.equalsIgnoreCase("promote") && args.length <= 1) {
                if (target != null) {
                    Mage mage = controller.getMage(target);
                    ProgressionPath path = mage.getActiveProperties().getPath();
                    ProgressionPath next = path == null ? null : path.getNextPath();
                    while (next != null) {
                        options.add(next.getKey());
                        next = next.getNextPath();
                    }
                }
            }

            if (subCommand.equalsIgnoreCase("modifier")) {
                if (args.length <= 1) {
                    options.add("add");
                    options.add("remove");
                } else if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("remove") && target != null) {
                        Mage mage = controller.getMage(target);
                        options.addAll(mage.getModifierKeys());
                    } else {
                        options.addAll(controller.getModifierTemplateKeys());
                    }
                }
            }

            if (args.length < 4) {
                if (subCommand.equalsIgnoreCase("add")) {
                    Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
                    for (SpellTemplate spell : spellList) {
                        addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
                    }
                    addIfPermissible(sender, options, subCommandPNode, "brush", true);
                }
                if (subCommand.equalsIgnoreCase("reward")) {
                    if (args.length > 1) {
                         options.addAll(controller.getCurrencyKeys());
                    } else {
                        Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
                        for (SpellTemplate spell : spellList) {
                            addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
                        }
                    }
                }

                if (subCommand.equalsIgnoreCase("remove")) {
                    if (target != null) {
                        Mage mage = controller.getMage(target);
                        CasterProperties mageClass = mage.getActiveProperties();
                        if (mageClass != null) {
                            options.addAll(mageClass.getSpells());
                        }
                    }
                    options.add("brush");
                }

                if (subCommand.equalsIgnoreCase("add") && args.length > 0 && args[0].equalsIgnoreCase("brush")) {
                    options.addAll(api.getBrushes());
                }

                if (subCommand.equalsIgnoreCase("add")) {
                    Collection<SpellTemplate> spellList = api.getSpellTemplates(sender.hasPermission("Magic.bypass_hidden"));
                    for (SpellTemplate spell : spellList) {
                        addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
                    }
                    addIfPermissible(sender, options, subCommandPNode, "brush", true);
                }


                if (subCommand.equalsIgnoreCase("clear")) {
                    options.add("all");
                    options.add("magic");
                    options.add("skills");
                    options.add("wands");
                }

                if (subCommand.equalsIgnoreCase("configure") || subCommand.equalsIgnoreCase("describe") || subCommand.equalsIgnoreCase("upgrade")) {
                    for (String key : com.elmakers.mine.bukkit.magic.MageClass.PROPERTY_KEYS) {
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
                if (subCommand.equalsIgnoreCase("lock")
                        || subCommand.equalsIgnoreCase("unlock")
                        || subCommand.equalsIgnoreCase("activate")
                        || subCommand.equalsIgnoreCase("switch")) {
                    options.addAll(api.getController().getMageClassKeys());
                }
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

    public boolean onMageSkin(CommandSender sender, Player player)
    {
        String skinBlock = controller.getPlayerSkin(player);
        if (skinBlock == null) {
            sender.sendMessage("Could not get skin for player " + player.getName());
            sender.sendMessage("(This command requires LibsDisguise to be installed!)");
            return true;
        }
        sender.sendMessage(skinBlock);
        return true;
    }

    public boolean onMageForget(CommandSender sender, Player player, String[] parameters)
    {
        int count = 0;
        for (String recipeKey : controller.getRecipeKeys()) {
            if (CompatibilityLib.getCompatibilityUtils().undiscoverRecipe(player, recipeKey)) {
                count++;
            }
        }
        sender.sendMessage(ChatColor.AQUA + "Removed knowledge of " + ChatColor.DARK_AQUA + count + ChatColor.AQUA + " crafting recipes from "
            + ChatColor.GOLD + player.getName());
        return true;
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
        mage.deactivate();
        cleared += clearWands(player.getInventory(), type);
        cleared += clearWands(player.getEnderChest(), type);
        mage.checkWand();
        sender.sendMessage(ChatColor.AQUA + "Cleared " + ChatColor.WHITE + cleared + " " + ChatColor.DARK_AQUA + type
            + ChatColor.AQUA + " items from inventory of " + ChatColor.GOLD + player.getName());
        return true;
    }

    private int clearWands(Inventory inventory, String type) {
        int cleared = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (type.equalsIgnoreCase("all")) {
                inventory.setItem(i, null);
                continue;
            }
            ItemStack item = inventory.getItem(i);
            if (CompatibilityLib.getItemUtils().isEmpty(item)) continue;

            if ((type.equalsIgnoreCase("wands") && controller.isWand(item))
                    || (type.equalsIgnoreCase("skills") && controller.isSkill(item))
                    || (type.equalsIgnoreCase("magic") && controller.isMagic(item))) {
                inventory.setItem(i, null);
                cleared++;
                continue;
            }
        }
        return cleared;
    }

    public boolean onMageCheck(CommandSender sender, Player player, String[] args)
    {
        Mage mage = controller.getMage(player);
        mage.debugPermissions(sender, null);
        return true;
    }

    protected boolean onMageBypass(CommandSender sender, Player player) {
        Mage mage = controller.getMage(player);
        if (mage.isBypassEnabled()) {
            if (sender != player) {
                sender.sendMessage(ChatColor.GOLD + "Turned off bypass for player " + player.getName());
            }
            player.sendMessage(ChatColor.GOLD + "Your magic permissions are back to normal");
            mage.setBypassEnabled(false);
        } else {
            if (sender != player) {
                sender.sendMessage(ChatColor.GOLD + "Turned on bypass for player " + player.getName());
            }
            player.sendMessage(ChatColor.GOLD + "Magic PVP/build/break/etc bypass enabled");
            mage.setBypassEnabled(true);
        }
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

    public boolean onMageDebug(CommandSender sender, CommandSender player, String[] args)
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
                sender.sendMessage(ChatColor.GOLD + "Setting debug level for  " + ChatColor.AQUA + player.getName() + ChatColor.GOLD + " to " + ChatColor.GREEN + Integer.toString(level));
            } catch (Exception ex) {
                sender.sendMessage("Expecting integer, got: " + args[0]);
            }
            return true;
        }
        if (mage.getDebugLevel() > 0) {
            sender.sendMessage(ChatColor.GOLD + "Disabling debug for " + ChatColor.AQUA + player.getName());
            mage.setDebugLevel(0);
            mage.setDebugger(null);
        } else {
            sender.sendMessage(ChatColor.AQUA + "Enabling debug for " + ChatColor.AQUA + player.getName());
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
                sender.sendMessage(ChatColor.DARK_AQUA + " Cast Count: " + ChatColor.GOLD + spell.getCastCount());
                ConfigurationSection variables = spell.getVariables();
                for (String key : variables.getKeys(false)) {
                    String value = variables.getString(key);
                    sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + ": " + ChatColor.GOLD + value);
                }
                return true;
            }
            String key = args[0];
            ConfigurationSection subSection = data.getConfigurationSection(key);
            if (subSection == null) {
                Object value = data.get(key);
                if (value != null) {
                    sender.sendMessage(ChatColor.AQUA + " " + key + ChatColor.DARK_AQUA + " (" + value + ")");
                } else {
                    sender.sendMessage(ChatColor.RED + "Unknown subsection or spell: " + args[0]);
                }
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
        if (args.length > 3)
        {
            sender.sendMessage(ChatColor.RED + "Too many parameters");
            return true;
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
            if (args.length > 2) {
                String key = args[1];
                String value = args[2];
                spell.getVariables().set(key, value);
                sender.sendMessage(ChatColor.GOLD + "Set " + ChatColor.AQUA + spell.getName() + " " + ChatColor.DARK_AQUA + key + ChatColor.GOLD + " variable to " + ChatColor.AQUA + value + ChatColor.GOLD + " for " + ChatColor.DARK_AQUA + player.getDisplayName());
                return true;
            }
            long value = 0;
            try {
                value = Long.parseLong(args[1]);
            } catch (Exception ex) {
                String key = args[1];
                if (spell.getVariables().contains(key)) {
                    spell.getVariables().set(key, null);
                    sender.sendMessage(ChatColor.GOLD + "Cleared " + ChatColor.AQUA + spell.getName() + " " + ChatColor.DARK_AQUA + key + ChatColor.GOLD + " variable for " + ChatColor.DARK_AQUA + player.getDisplayName());
                    return true;
                }

                sender.sendMessage(ChatColor.RED + "Cast count must be a number, and no variable found with name " + key);
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
        Set<String> internalAttributes = api.getController().getInternalAttributes();
        Set<String> attributes = api.getController().getAttributes();
        if (attributes.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No attributes configured, see attributes.yml");
            return true;
        }
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.GOLD + "Attributes for: " + ChatColor.AQUA + player.getName());
            List<String> attributeList = new ArrayList<>(attributes);
            Collections.sort(attributeList);
            for (String key : attributeList) {
                ChatColor attributeType = internalAttributes.contains(key) ? ChatColor.DARK_AQUA : ChatColor.GRAY;
                Double value = mage.getAttribute(key);
                String valueDescription = value == null ? ChatColor.RED + "(not set)" : ChatColor.AQUA + Double.toString(value);
                sender.sendMessage(attributeType + key + ChatColor.BLUE + " = " + valueDescription);
            }
            return true;
        }
        String key = args[0];
        if (!attributes.contains(key)) {
            sender.sendMessage(ChatColor.RED + "Unknown attribute: " + ChatColor.YELLOW + key);
            return true;
        }
        if (args.length == 1)
        {
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

    public boolean onMagePromote(CommandSender sender, Player player, String[] parameters)
    {
        Mage mage = controller.getMage(player);
        CasterProperties activeProperties = mage.getActiveProperties();
        ProgressionPath currentPath = activeProperties.getPath();
        if (currentPath == null) {
            sender.sendMessage(ChatColor.RED + "Player " + ChatColor.YELLOW + player.getName()
                + ChatColor.RED + " is not currently on a path");
            return true;
        }
        ProgressionPath nextPath = currentPath.getNextPath();
        String targetPath = null;
        if (parameters.length > 0) {
            targetPath = parameters[0];
            boolean foundPath = false;
            while (nextPath != null && !foundPath) {
                foundPath = nextPath.getKey().equalsIgnoreCase(targetPath);
                nextPath = nextPath.getNextPath();
            }
            if (!foundPath) {
                sender.sendMessage(ChatColor.RED + "Player " + ChatColor.YELLOW + player.getName()
                + ChatColor.RED + " is not on a path that leads to " + ChatColor.GOLD + targetPath);
                return true;
            }
        } else {
            if (nextPath != null) {
                targetPath = nextPath.getKey();
            }
        }
        int totalLevels = 0;
        int iterations = 0;
        while (targetPath == null || !targetPath.equals(currentPath.getKey())) {
            int levels = activeProperties.randomize(1, true);
            if (levels == 0) {
                break;
            }
            totalLevels += levels;
            currentPath = activeProperties.getPath();
            iterations++;
            if (iterations > 10000) {
                controller.getLogger().warning("Something went wrong with the mage promote command, it got stuck in a loop");
            }
        }

        if (totalLevels == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Could not promote player " + ChatColor.GOLD + player.getName());
        } else {
            sender.sendMessage(ChatColor.GREEN + "Promoted player " + ChatColor.GOLD + player.getName());
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
        String classKey = parameters[0];
        Mage mage = controller.getMage(player);
        if (mage.hasClassUnlocked(classKey)) {
            sender.sendMessage(ChatColor.RED + "Class is already unlocked: " + ChatColor.WHITE + classKey);
        } else {
            MageClass mageClass = mage.unlockClass(classKey);
            if (mageClass == null) {
                sender.sendMessage(ChatColor.RED + "Invalid class: " + ChatColor.WHITE + classKey);
            } else {
                sender.sendMessage("Unlocked class " + classKey + " for " + player.getName());
            }
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

    public boolean onMageDeactivate(CommandSender sender, Player player, String[] parameters)
    {
        Mage mage = controller.getMage(player);
        mage.deactivate();
        sender.sendMessage("Deactivated " + player.getName());
        return true;
    }

    public boolean onMageDescribe(CommandSender sender, Player player, String[] parameters) {
        Mage mage = controller.getMage(player);
        MageClass activeClass = mage.getActiveClass();
        MagicProperties mageProperties = mage.getProperties();

        if (parameters.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "Mage " + ChatColor.GOLD + mage.getName());
            sender.sendMessage(ChatColor.GRAY + mage.getId());
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
            Collection<String> modifierKeys = mage.getModifierKeys();
            if (!modifierKeys.isEmpty()) {
                 sender.sendMessage(ChatColor.AQUA + "Modifiers:");
                 for (String modifierKey : modifierKeys) {
                     MageModifier modifier = mage.getModifier(modifierKey);
                     String modifierDescription = ChatColor.DARK_AQUA + modifier.getName();
                     if (modifier.hasDuration()) {
                        int timeRemaining = modifier.getTimeRemaining();
                        modifierDescription += ChatColor.GRAY + " (" + ChatColor.WHITE + controller.getMessages().getTimeDescription(timeRemaining) + ChatColor.GRAY + ")";
                     }
                     sender.sendMessage(modifierDescription);
                 }
            }
            if (!mageProperties.isEmpty()) {
                 sender.sendMessage(ChatColor.AQUA + "Mage properties:");
                 mageProperties.describe(sender, BaseMagicProperties.HIDDEN_PROPERTY_KEYS);
            }
            ConfigurationSection variables = mage.getVariables();
            Set<String> keys = variables.getKeys(false);
            if (!keys.isEmpty()) {
                sender.sendMessage(ChatColor.AQUA + "Mage variables:");
                for (String key : keys) {
                    Object value = variables.get(key);
                    if (value != null) {
                        sender.sendMessage(ChatColor.DARK_AQUA + key + ChatColor.GRAY + ": " + ChatColor.WHITE + CompatibilityLib.getInventoryUtils().describeProperty(value, CompatibilityConstants.MAX_PROPERTY_DISPLAY_LENGTH));
                    }
                }
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
                sender.sendMessage(ChatColor.DARK_AQUA + parameters[0] + ChatColor.GRAY + ": " + ChatColor.WHITE + CompatibilityLib.getInventoryUtils().describeProperty(property));
            }
        }

        return true;
    }

    public boolean onMageReward(CommandSender sender, Player player, String[] parameters)
    {
        if (parameters.length < 1) {
            sender.sendMessage("Use: /mage reward <spell> [currency]");
            return true;
        }

        Mage mage = controller.getMage(player);
        CasterProperties caster = mage.getActiveProperties();
        if (caster == null) {
            sender.sendMessage("Player " + player.getName() + " has no active class");
            return true;
        }

        String itemName = parameters[0];
        ItemStack item = controller.createItem(itemName);
        if (CompatibilityLib.getItemUtils().isEmpty(item)) {
            sender.sendMessage("Invalid item: " + itemName);
            return true;
        }

        if (caster.addItem(item)) {
            mage.sendMessage(controller.getMessages().get("commands.mage.reward.received").replace("$item", controller.describeItem(item)));
            if (sender != player) {
                sender.sendMessage(controller.getMessages().get("commands.mage.reward.gave")
                    .replace("$player", mage.getName())
                    .replace("$item", controller.describeItem(item)));
            }
        } else {
            String currencyKey = "sp";
            if (parameters.length > 1) {
                currencyKey = parameters[1];
            }
            Currency currency = controller.getCurrency(currencyKey);
            if (currency == null || !currency.isValid()) {
                sender.sendMessage("Invalid currency: " + currency);
                return true;
            }
            double worth = controller.getWorth(item, currencyKey);
            mage.addCurrency(currencyKey, worth);
            mage.sendMessage(controller.getMessages().get("commands.mage.reward.received")
                .replace("$item", currency.formatAmount(worth, controller.getMessages())));
            if (sender != player) {
                sender.sendMessage(controller.getMessages().get("commands.mage.reward.replacement")
                    .replace("$player", mage.getName())
                    .replace("$replacement", currency.formatAmount(worth, controller.getMessages()))
                    .replace("$item", controller.describeItem(item)));
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
            if (materialKey.equals("*")) {
                int added = 0;
                for (Material material : Material.values()) {
                    if (material.isBlock() && activeClass.addBrush(material.name().toLowerCase())) {
                        added++;
                    }
                }
                if (sender != player) {
                    sender.sendMessage("Added " + added + " brushes to " + player.getName());
                }
                return true;
            }
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

    public boolean onMageAddModifier(CommandSender sender, Player player, String[] parameters) {
        Mage mage = controller.getMage(player);
        String modifierKey = parameters[0];
        MageModifier modifier = null;
        int duration = 0;
        if (parameters.length > 1) {
            try {
                duration = Integer.parseInt(parameters[1]);
            } catch (Exception ex) {
                sender.sendMessage(controller.getMessages().get("commands.modifier.add.invalid")
                    .replace("$input", parameters[1]));
                return true;
            }
        }
        if (mage.addModifier(modifierKey, duration)) {
            modifier = mage.getModifier(modifierKey);
            if (modifier != null) {
                String message = duration > 0 ? "commands.modifier.add.success_duration" : "commands.modifier.add.success";
                sender.sendMessage(controller.getMessages().get(message)
                    .replace("$name", modifier.getName())
                    .replace("$player", player.getName())
                    .replace("$duration", controller.getMessages().getTimeDescription(duration))
                );
            }
        }
        if (modifier == null) {
            sender.sendMessage(controller.getMessages().get("commands.modifier.add.fail")
                .replace("$name", modifierKey)
                .replace("$player", player.getName())
            );
        }
        return true;
    }

    public boolean onMageRemoveModifier(CommandSender sender, Player player, String[] parameters) {
        Mage mage = controller.getMage(player);
        String modifierKey = parameters[0];
        MageModifier modifier = mage.removeModifier(modifierKey);
        if (modifier != null) {
            sender.sendMessage(controller.getMessages().get("commands.modifier.remove.success")
                .replace("$name", modifier.getName())
                .replace("$player", player.getName())
            );
        } else {
            sender.sendMessage(controller.getMessages().get("commands.modifier.remove.fail")
                .replace("$name", modifierKey)
                .replace("$player", player.getName())
            );
        }
        return true;
    }

    public boolean onMageModifier(CommandSender sender, Player player, String[] parameters) {
        if (parameters.length < 2) {
            sender.sendMessage(controller.getMessages().get("commands.modifier.usage"));
            return true;
        }
        String subCommand = parameters[0];
        parameters = Arrays.copyOfRange(parameters, 1, parameters.length);
        if (subCommand.equalsIgnoreCase("add")) {
            return onMageAddModifier(sender, player, parameters);
        }
        if (subCommand.equalsIgnoreCase("remove")) {
            return onMageRemoveModifier(sender, player, parameters);
        }

        sender.sendMessage(controller.getMessages().get("commands.modifier.usage"));
        return true;
    }
}
