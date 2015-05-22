/**
 * 
 */
package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.wand.WandTemplate;
import de.slikey.effectlib.util.ParticleEffect;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Messages;

public class WandCommandExecutor extends MagicTabExecutor {
	
	public WandCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String commandName = command.getName();
        if (commandName.equalsIgnoreCase("wandp"))
		{
			if (args.length == 0) {
				sender.sendMessage("Usage: /wandp [player] [wand name/command]");
				return true;
			}
			Player player = Bukkit.getPlayer(args[0]);
			if (player == null) {
				sender.sendMessage("Can't find player " + args[0]);
				return true;
			}
			if (!player.isOnline()) {
				sender.sendMessage("Player " + args[0] + " is not online");
				return true;
			}
			String[] args2 = Arrays.copyOfRange(args, 1, args.length);
			return processWandCommand("wandp", sender, player, args2);
		}

		if (commandName.equalsIgnoreCase("wand") && args.length > 0 && args[0].equalsIgnoreCase("list"))
		{
			if (!api.hasPermission(sender, "Magic.commands.wand.list")) {
				sendNoPermission(sender);
				return true;
			}
			onWandList(sender);
			return true;
		}

		// Everything beyond this point is is-game only

		if (!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player)sender;
		if (commandName.equalsIgnoreCase("wand"))
		{
			return processWandCommand("wand", sender, player, args);
		}
		
		return false;
	}

	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
		
		Player player = (sender instanceof Player) ? (Player)sender : null;

        String permissionKey = "wand";
		if (commandName.contains("wandp"))
		{
            permissionKey = "wandp";
			if (args.length > 0) {
				player = Bukkit.getPlayer(args[0]);
			}
			if (args.length == 1) {
				options.addAll(api.getPlayerNames());
				return options;
			} else if (args.length > 1) {
				args = Arrays.copyOfRange(args, 1, args.length);
			}
		}
		
		if (args.length == 1) {
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "add");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "remove");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "name");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "fill");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "configure");
            addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "override");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "organize");
            addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "alphabetize");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "combine");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "upgrade");
			addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "describe");
            addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "enchant");
            addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "unenchant");
            addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "duplicate");
            addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "restore");
            addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".", "unlock");
		}
		
		if (args.length == 2) {
			String subCommand = args[0];
			String subCommandPNode = "Magic.commands." + permissionKey + "." + subCommand;
			
			if (!api.hasPermission(sender, subCommandPNode)) {
				return options;
			}
			
			subCommandPNode += ".";
			
			if (subCommand.equalsIgnoreCase("add")) {
				Collection<SpellTemplate> spellList = api.getSpellTemplates();
				for (SpellTemplate spell : spellList) {
					addIfPermissible(sender, options, subCommandPNode, spell.getKey(), true);
				}
				addIfPermissible(sender, options, subCommandPNode, "material", true);
			}
			
			if (subCommand.equalsIgnoreCase("configure")) {
				for (String key : com.elmakers.mine.bukkit.wand.Wand.PROPERTY_KEYS) {
					options.add(key);
				}
			}
			
			if (subCommand.equalsIgnoreCase("remove")) {
				Wand activeWand = null;
				if (player != null) {
					Mage mage = api.getMage(player);
					activeWand = mage.getActiveWand();
				}
				if (activeWand != null) {
					Collection<String> spellNames = activeWand.getSpells();
					for (String spellName : spellNames) {
						options.add(spellName);
					}
					
					options.add("material");
				}
			}
			
			if (subCommand.equalsIgnoreCase("combine")) {
				Collection<String> allWands = api.getWandKeys();
				for (String wandKey : allWands) {
					addIfPermissible(sender, options, "Magic.commands." + permissionKey + ".combine.", wandKey, true);
				}
			}
		}
		
		if (args.length == 3)
		{
			String subCommand = args[0];
			String subCommand2 = args[1];

            String commandPNode = "Magic.commands." + permissionKey + "." + subCommand;

            if (!api.hasPermission(sender, commandPNode)) {
                return options;
            }

            if (subCommand.equalsIgnoreCase("configure") || subCommand.equalsIgnoreCase("upgrade")) {
                if (subCommand2.equals("effect_sound")) {
                    Sound[] sounds = Sound.values();
                    for (Sound sound : sounds) {
                        options.add(sound.name().toLowerCase());
                    }
                } else if (subCommand2.equals("effect_particle")) {
                    ParticleEffect[] particleTypes = ParticleEffect.values();
                    for (ParticleEffect particleType : particleTypes) {
                        options.add(particleType.name().toLowerCase());
                    }
                }
            }
			
			String subCommandPNode = "Magic.commands." + permissionKey + "." + subCommand + "." + subCommand2;
			if (!api.hasPermission(sender, subCommandPNode)) {
				return options;
			}
			
			if (subCommand.equalsIgnoreCase("remove") && subCommand2.equalsIgnoreCase("material")) {
				Wand activeWand = null;
				if (player != null) {
					Mage mage = api.getMage(player);
					activeWand = mage.getActiveWand();
				}
				if (activeWand != null) {
					Collection<String> materialNames = activeWand.getBrushes();
					for (String materialName : materialNames) {
						options.add(materialName);
					}
				}
			}
			
			if (subCommand.equalsIgnoreCase("add") && subCommand2.equalsIgnoreCase("material")) {
				options.addAll(api.getBrushes());
			}
		}

		return options;
	}

	protected boolean processWandCommand(String command, CommandSender sender, Player player, String[] args)
	{
		String subCommand = "";
		String[] args2 = args;

		if (args.length > 0) {
			subCommand = args[0];
			args2 = new String[args.length - 1];
			for (int i = 1; i < args.length; i++) {
				args2[i - 1] = args[i];
			}
		}
		if (subCommand.equalsIgnoreCase("list"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandList(sender);
			return true;
		}
		if (subCommand.equalsIgnoreCase("add"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;
			if (args2.length > 0 && args2[0].equals("material") && !api.hasPermission(sender,"Magic.commands.wand.add." + args2[0], true)) return true;
			if (args2.length > 0 && !api.hasPermission(sender,"Magic.commands.wand.add.spell." + args2[0], true)) return true;
			onWandAdd(sender, player, args2);
			return true;
		}
		if (subCommand.equalsIgnoreCase("configure"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandConfigure(sender, player, args2, false);
			return true;
		}
        if (subCommand.equalsIgnoreCase("override"))
        {
            if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

            onWandOverride(sender, player, args2);
            return true;
        }
		if (subCommand.equalsIgnoreCase("enchant"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

            if (args2.length > 0) {
                onWandEnchant(sender, player, args2[0]);
            } else {
                onWandEnchant(sender, player);
            }
			return true;
		}
		if (subCommand.equalsIgnoreCase("unenchant"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandUnenchant(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("duplicate"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandDuplicate(sender, player);
			return true;
		}
        if (subCommand.equalsIgnoreCase("restore"))
        {
            if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

            onWandRestore(sender, player);
            return true;
        }
        if (subCommand.equalsIgnoreCase("unlock"))
        {
            if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

            onWandUnlock(sender, player);
            return true;
        }
		if (subCommand.equalsIgnoreCase("organize"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandOrganize(sender, player);
			return true;
		}
        if (subCommand.equalsIgnoreCase("alphabetize"))
        {
            if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

            onWandAlphabetize(sender, player);
            return true;
        }
		if (subCommand.equalsIgnoreCase("combine"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;
			if (args2.length > 0 && !api.hasPermission(sender,"Magic.commands." + command + ".combine." + args2[0], true)) return true;
			
			onWandCombine(sender, player, args2);
			return true;
		}
		if (subCommand.equalsIgnoreCase("describe"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandDescribe(sender, player, args2.length > 0);
			return true;
		}
		if (subCommand.equalsIgnoreCase("upgrade"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandConfigure(sender, player, args2, true);
			return true;
		}
		if (subCommand.equalsIgnoreCase("organize"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandOrganize(sender, player);
			return true;
		}
		if (subCommand.equalsIgnoreCase("fill"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;
            int maxLevel = api.getController().getMaxWandFillLevel();
            if (args2.length > 0) {
                if (args2[0].equalsIgnoreCase("max")) {
                    maxLevel = 0;
                } else {
                    try {
                        maxLevel = Integer.parseInt(args2[0]);
                    } catch (Exception ex) {
                        sender.sendMessage("Invalid level: " + args2[0]);
                        return true;
                    }
                }
            }
            onWandFill(sender, player, maxLevel);
			return true;
		}
		if (subCommand.equalsIgnoreCase("remove"))
		{   
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandRemove(sender, player, args2);
			return true;
		}

		if (subCommand.equalsIgnoreCase("name"))
		{
			if (!api.hasPermission(sender, "Magic.commands." + command + "." + subCommand)) return true;

			onWandName(sender, player, args2);
			return true;
		}

		if (subCommand.length() == 0) 
		{
			if (!api.hasPermission(sender, "Magic.commands." + command)) return true;
            if (!api.hasPermission(sender, "Magic.commands." + command + ".create")) return true;
			if (!api.hasPermission(sender, "Magic.commands." + command + ".wand.default", true)) return true;
		} 
		else 
		{
            if (!api.hasPermission(sender, "Magic.commands." + command + ".create")) return true;
			if (!api.hasPermission(sender, "Magic.commands." + command +".wand." + subCommand, true)) return true;
		}
		
		return onWand(sender, player, args);
	}

	public boolean onWandList(CommandSender sender) {
		Collection<WandTemplate> templates = com.elmakers.mine.bukkit.wand.Wand.getWandTemplates();
		Map<String, ConfigurationSection> nameMap = new TreeMap<String, ConfigurationSection>();
		for (WandTemplate template : templates)
		{
			nameMap.put(template.getKey(), template.getConfiguration());
		}
		for (ConfigurationSection templateConfig : nameMap.values())
		{
			if (templateConfig.getBoolean("hidden", false)) continue;
			
			String key = templateConfig.getString("key");
			String name = api.getMessages().get("wands." + key + ".name", api.getMessages().get("wand.default_name"));
			String description = api.getMessages().get("wands." + key + ".description", "");
			description = ChatColor.YELLOW + description; 
			if (!name.equals(key)) {
				description = ChatColor.BLUE + name + ChatColor.WHITE + " : " + description;
			}
			sender.sendMessage(ChatColor.AQUA + key + ChatColor.WHITE + " : " + description);
		}

		return true;
	}

	public boolean onWandDescribe(CommandSender sender, Player player, boolean details) {
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) {
            if (sender != player) {
                sender.sendMessage(api.getMessages().getParameterized("wand.player_no_item", "$name", player.getName()));
            } else {
                player.sendMessage(api.getMessages().get("wand.no_item"));
            }
            return true;
        }

        if (api.isSpell(itemInHand)) {
            String spellKey = api.getSpell(itemInHand);
            sender.sendMessage(ChatColor.GOLD + "Spell: " + spellKey);
            SpellTemplate spell = api.getSpellTemplate(spellKey);
            if (spell != null) {
                sender.sendMessage(" " + ChatColor.GOLD + spell.getName());
            } else {
                sender.sendMessage(ChatColor.RED + " (Unknown Spell)");
            }
        }
        if (api.isBrush(itemInHand)) {
            String brushKey = api.getBrush(itemInHand);
            sender.sendMessage(ChatColor.GRAY + "Brush: " + brushKey);
            MaterialAndData brush = new MaterialAndData(brushKey);
            if (brush != null) {
                sender.sendMessage(" " + ChatColor.GRAY + brush.getName());
            } else {
                sender.sendMessage(ChatColor.RED + " (Unknown Brush)");
            }
        }
        if (api.isWand(itemInHand)) {
            Wand wand = api.getWand(itemInHand);
            wand.describe(sender);
        }

        if (details) {
            long wandId = System.identityHashCode(NMSUtils.getHandle(itemInHand));
            sender.sendMessage(ChatColor.AQUA + "Id: " + ChatColor.DARK_AQUA + Long.toHexString(wandId));
            Wand activeWand = api.getMage(player).getActiveWand();
            if (activeWand == null) {
                sender.sendMessage(ChatColor.RED + "Mis-match - player has no active wand!");
            } else {
                long activeWandId = System.identityHashCode(NMSUtils.getHandle(activeWand.getItem()));
                if (activeWandId != wandId) {
                    sender.sendMessage(ChatColor.RED + " Mis-match - Active wand id: " + ChatColor.DARK_RED + Long.toHexString(activeWandId));
                }
            }
            String itemString = itemInHand.toString();
            sender.sendMessage(itemString);
        }

        if (!api.isBrush(itemInHand) && !api.isSpell(itemInHand) && !api.isWand(itemInHand)) {
            if (sender != player) {
                sender.sendMessage(api.getMessages().getParameterized("wand.player_no_magic_item", "$name", player.getName()));
            } else {
                player.sendMessage(api.getMessages().get("wand.no_magic_item"));
            }
        }

        return true;
	}
	
	public boolean onWandOrganize(CommandSender sender, Player player)
	{
		// Allow reorganizing modifiable wands
		if (!checkWand(sender, player, true)) {
			return true;
		}
		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		wand.organizeInventory(mage);
		mage.sendMessage(api.getMessages().get("wand.reorganized"));
		if (sender != player) {
			sender.sendMessage(api.getMessages().getParameterized("wand.player_reorganized", "$name", player.getName()));
		}
		
		return true;
	}

    public boolean onWandAlphabetize(CommandSender sender, Player player)
    {
        if (!checkWand(sender, player, true)) {
            return true;
        }
        Mage mage = api.getMage(player);
        Wand wand = mage.getActiveWand();
        wand.alphabetizeInventory();
        mage.sendMessage(api.getMessages().get("wand.alphabetized").replace("$wand", wand.getName()));
        if (sender != player) {
            sender.sendMessage(api.getMessages().getParameterized("wand.player_alphabetized", "$name", player.getName()).replace("$wand", wand.getName()));
        }

        return true;
    }

    public boolean onWandEnchant(CommandSender sender, Player player, String levelString)
    {
        if (!checkWand(sender, player)) {
            return false;
        }
        Mage mage = api.getMage(player);
        Wand wand = mage.getActiveWand();

        int xpLevels = 0;
        boolean useXp = levelString.equalsIgnoreCase("xp");
        if (useXp) {
            xpLevels = mage.getLevel();
        } else {
            try {
                xpLevels = Integer.parseInt(levelString);
            } catch (Exception ex) {
                sender.sendMessage("Invalid parameter: " + levelString);
            }
        }
        int levels = wand.enchant(xpLevels);
        if (levels > 0 && useXp) {
            mage.setLevel(Math.max(0, mage.getLevel() - levels));
        }

        if (sender != player) {
            if (levels > 0) {
                sender.sendMessage(api.getMessages().getParameterized("wand.player_upgraded", "$name", player.getName()));
            } else {
                sender.sendMessage(api.getMessages().getParameterized("wand.player_not_upgraded", "$name", player.getName()));
            }
        }
        return true;
    }
	
	public boolean onWandEnchant(CommandSender sender, Player player)
	{
		Mage mage = api.getMage(player);
		ItemStack heldItem = player.getItemInHand();
		if (heldItem == null || heldItem.getType() == Material.AIR)
		{
			mage.sendMessage(api.getMessages().get("wand.no_item"));
			if (sender != player) {
				sender.sendMessage(api.getMessages().getParameterized("wand.player_no_item", "$name", player.getName()));
			}
			return false;
		}
        if (api.isWand(heldItem) || api.isSpell(heldItem) || api.isBrush(heldItem)) {
            sender.sendMessage(api.getMessages().getParameterized("wand.already_enchanted", "$item", MaterialAndData.getMaterialName(heldItem)));
            return false;
        }
		
		Wand wand = api.createWand(heldItem);
		player.setItemInHand(wand.getItem());
		wand.activate(mage);
		
		mage.sendMessage(api.getMessages().getParameterized("wand.enchanted", "$item", MaterialAndData.getMaterialName(heldItem)));
				
		if (sender != player) {
			sender.sendMessage(api.getMessages().getParameterized("wand.player_enchanted",
					"$item", MaterialAndData.getMaterialName(heldItem),
					"$name", player.getName()
			));
		}
		
		return true;
	}
	
	public boolean onWandUnenchant(CommandSender sender, Player player)
	{
		if (!checkWand(sender, player)) {
			return true;
		}
		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		
		// Trying to make sure the player is actually holding the active wand
		// Just in case. This isn't fool-proof though, if they have more than one wand.
		if (wand == null || !api.isWand(player.getItemInHand())) {
			mage.sendMessage(api.getMessages().get("wand.no_wand"));
			if (sender != player) {
				sender.sendMessage(api.getMessages().getParameterized("wand.player_no_wand", "$name", player.getName()));
			}
			return false;
		}

		wand.deactivate();
		wand.unenchant();
		player.setItemInHand(wand.getItem());
		
		mage.sendMessage(api.getMessages().get("wand.unenchanted"));
		if (sender != player) {
			sender.sendMessage(api.getMessages().getParameterized("wand.player_unenchanted", "$name", player.getName()));
		}
		return true;
	}

	public boolean onWandDuplicate(CommandSender sender, Player player)
	{
		if (!checkWand(sender, player, false, false)) {
			return true;
		}
		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		Wand newWand = wand.duplicate();
		
		api.giveItemToPlayer(player, newWand.getItem());
		
		mage.sendMessage(api.getMessages().get("wand.duplicated"));
		if (sender != player) {
			sender.sendMessage(api.getMessages().getParameterized("wand.player_duplicated", "$name", player.getName()));
		}
		return true;
	}

    public boolean onWandRestore(CommandSender sender, Player player)
    {
        Mage mage = api.getMage(player);
        if (mage.restoreWand()) {
            mage.sendMessage(api.getMessages().get("wand.restored"));
            if (sender != player) {
                sender.sendMessage(api.getMessages().getParameterized("wand.player_restored", "$name", player.getName()));
            }
        } else {
            mage.sendMessage(api.getMessages().get("wand.not_restored"));
            if (sender != player) {
                sender.sendMessage(api.getMessages().getParameterized("wand.player_not_restored", "$name", player.getName()));
            }
        }

        return true;
    }

    public boolean onWandUnlock(CommandSender sender, Player player)
    {
        if (!checkWand(sender, player, true, true, false)) {
            return true;
        }
        Mage mage = api.getMage(player);
        Wand wand = mage.getActiveWand();

        wand.unlock();
        mage.sendMessage(api.getMessages().get("wand.unlocked"));
        if (sender != player) {
            sender.sendMessage(api.getMessages().getParameterized("wand.player_unlocked", "$name", player.getName()));
        }
        return true;
    }

    public boolean onWandOverride(CommandSender sender, Player player, String[] parameters)
    {
        if (!checkWand(sender, player, true, true, false))
        {
            return true;
        }

        Mage mage = api.getMage(player);
        Wand wand = mage.getActiveWand();
        if (parameters.length == 0)
        {
            Map<String, String> overrides = wand.getOverrides();
            if (overrides.size() == 0)
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "This wand has no overrides");
            }
            else
            {
                for (Map.Entry<String, String> override : overrides.entrySet())
                {
                    sender.sendMessage(ChatColor.AQUA + override.getKey()
                            + ChatColor.WHITE + " = " + ChatColor.DARK_AQUA + override.getValue());
                }
            }
            return true;
        }

        if (!checkWand(sender, player))
        {
            return true;
        }

        if (parameters.length == 1)
        {
            wand.removeOverride(parameters[0]);
            sender.sendMessage(ChatColor.DARK_AQUA  + "Removed override " + parameters[0]);
            return true;
        }

        String value = "";
        for (int i = 1; i < parameters.length; i++) {
            if (i != 1) value = value + " ";
            value = value + parameters[i];
        }

        wand.setOverride(parameters[0], value);
        sender.sendMessage(ChatColor.DARK_AQUA  + "Added override " + ChatColor.AQUA + parameters[0]
                + ChatColor.WHITE + " = " + ChatColor.DARK_AQUA + parameters[1]);

        return true;
    }
	
	public boolean onWandConfigure(CommandSender sender, Player player, String[] parameters, boolean safe)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand configure <property> <value>");
			sender.sendMessage("Properties: " + StringUtils.join(com.elmakers.mine.bukkit.wand.Wand.PROPERTY_KEYS, ", "));
			return true;
		}

		if (!checkWand(sender, player)) {
			return true;
		}

		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		Map<String, Object> node = new HashMap<String, Object>();
		String value = "";
		for (int i = 1; i < parameters.length; i++) {
			if (i != 1) value = value + " ";
			value = value + parameters[i];
		}
		node.put(parameters[0], value);
		wand.deactivate();
		if (safe) {
			wand.upgrade(node);
		} else {
			wand.configure(node);
		}
		wand.activate(mage);
		mage.sendMessage(api.getMessages().get("wand.reconfigured"));
		if (sender != player) {
			sender.sendMessage(api.getMessages().getParameterized("wand.player_reconfigured", "$name", player.getName()));
		}
		return true;
	}
	
	protected boolean checkWand(CommandSender sender, Player player)
	{
		return checkWand(sender, player, false, false);
	}
	
	protected boolean checkWand(CommandSender sender, Player player, boolean skipModifiable)
	{
		return checkWand(sender, player, skipModifiable, false);
	}
	
	protected boolean checkWand(CommandSender sender, Player player, boolean skipModifiable, boolean skipBound)
	{
		return checkWand(sender, player, skipModifiable, skipBound, false);
	}
	
	protected boolean checkWand(CommandSender sender, Player player, boolean skipModifiable, boolean skipBound, boolean quiet)
	{
		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		
		if (wand == null) {
			if (!quiet) mage.sendMessage(api.getMessages().get("wand.no_wand"));
			if (sender != player) {
				sender.sendMessage(api.getMessages().getParameterized("wand.player_no_wand", "$name", player.getName()));
			}
			return false;
		}
        boolean bypassLocked = (sender instanceof Player) && api.hasPermission(sender, "Magic.wand.override_locked");
		if (!skipModifiable && wand.isLocked() && !bypassLocked) {
			if (!quiet) mage.sendMessage(api.getMessages().get("wand.unmodifiable"));
			if (sender != player) {
				sender.sendMessage(api.getMessages().getParameterized("wand.player_unmodifiable", "$name", player.getName()));
			}
			return false;
		}
		if (!skipBound && !wand.canUse(mage.getPlayer()) ) {
			if (!quiet) mage.sendMessage(api.getMessages().get("wand.bound_to_other"));
			if (sender != player) {
				sender.sendMessage(api.getMessages().getParameterized("wand.player_unmodifiable", "$name", player.getName()));
			}
			return false;
		}
		
		return true;
	}

	public boolean onWandCombine(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand combine <wandname>");
			return false;
		}
		
		if (!checkWand(sender, player)) {
			return true;
		}

		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		
		String wandName = parameters[0];
		Wand newWand = api.createWand(wandName);
		if (newWand == null) {
			sender.sendMessage(api.getMessages().getParameterized("wand.unknown_template", "$name", wandName));
			return false;
		}
		wand.deactivate();
		boolean result = wand.add(newWand);
		wand.activate(mage);
		
		if (sender != player) {
			if (result) {
				sender.sendMessage(api.getMessages().getParameterized("wand.player_upgraded", "$name", player.getName()));
			} else {
				sender.sendMessage(api.getMessages().getParameterized("wand.player_not_upgraded", "$name", player.getName()));
			}
		}
		return true;
	}

	public boolean onWandFill(CommandSender sender, Player player, int maxLevel)
	{
		if (!checkWand(sender, player)) {
			return true;
		}
		
		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		
		wand.fill(player, maxLevel);
		mage.sendMessage(api.getMessages().get("wand.filled").replace("$wand", wand.getName()));
		if (sender != player) {
			sender.sendMessage(api.getMessages().getParameterized("wand.player_filled", "$name", player.getName()));
		}
		
		return true;
	}
	
	public boolean onWandAdd(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand add <spell|material> [material:data]");
			return true;
		}
		
		if (!checkWand(sender, player)) {
			return true;
		}

		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
        Messages messages = api.getMessages();

		String spellName = parameters[0];
		if (spellName.equals("material")) {
			if (parameters.length < 2) {
				sender.sendMessage("Use: /wand add material <material:data>");
				return true;
			}
			
			String materialKey = parameters[1];
			if (!MaterialBrush.isValidMaterial(materialKey, false)) {
				sender.sendMessage(materialKey + " is not a valid material");
				return true;
			}

			if (wand.addBrush(materialKey)) {
				wand.setActiveBrush(materialKey);
                mage.sendMessage(messages.get("wand.brush_added").replace("$wand", wand.getName()).replace("$name", MaterialBrush.getMaterialName(messages, materialKey)));
				if (sender != player) {
					sender.sendMessage("Added material '" + materialKey + "' to " + player.getName() + "'s wand");
				}
			} else {
				wand.setActiveBrush(materialKey);
				mage.sendMessage("Material activated: " + materialKey);
				if (sender != player) {
					sender.sendMessage(player.getName() + "'s wand already has material " + materialKey);
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

        SpellTemplate currentSpell = wand.getBaseSpell(spellName);
		if (wand.addSpell(spellName)) {
			wand.setActiveSpell(spellName);
            if (currentSpell != null) {
                String levelDescription = spell.getLevelDescription();
                if (levelDescription == null || levelDescription.isEmpty()) {
                    levelDescription = spell.getName();
                }
                mage.sendMessage(api.getMessages().get("wand.spell_upgraded").replace("$name", currentSpell.getName()).replace("$level", levelDescription));
                if (sender != player) {
                    sender.sendMessage(api.getMessages().get("wand.player_spell_upgraded").replace("$player", player.getName()).replace("$name", currentSpell.getName()).replace("$level", levelDescription));
                }
            } else {
                mage.sendMessage(messages.get("wand.spell_added").replace("$name", spellName).replace("$wand", wand.getName()));
                if (sender != player) {
                    sender.sendMessage("Added '" + spell.getName() + "' to " + player.getName() + "'s wand");
                }
            }
		} else {
			wand.setActiveSpell(spellName);
			mage.sendMessage(spell.getName() + " activated");
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand already has " + spell.getName());
			}
		}

		return true;
	}

	public boolean onWandRemove(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand remove <spell|material> [material:data]");
			return true;
		}

		if (!checkWand(sender, player)) {
			return true;
		}

		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();

		String spellName = parameters[0];	
		if (spellName.equals("material")) {
			if (parameters.length < 2) {
				sender.sendMessage("Use: /wand remove material <material:data>");
				return true;
			}
			String materialKey = parameters[1];

			if (wand.removeBrush(materialKey)) {
				mage.sendMessage("Material '" + materialKey + "' has been removed from your wand");
				if (sender != player) {
					sender.sendMessage("Removed material '" + materialKey + "' from " + player.getName() + "'s wand");
				}
			} else {
				if (sender != player) {
					sender.sendMessage(player.getName() + "'s wand does not have material " + materialKey);
				}
			}

			return true;
		}
		
		if (wand.removeSpell(spellName)) {
            SpellTemplate template = api.getSpellTemplate(spellName);
            if (template != null) {
                spellName = template.getName();
            }
			mage.sendMessage("Spell '" + spellName + "' has been removed from your wand");
			if (sender != player) {
				sender.sendMessage("Removed '" + spellName + "' from " + player.getName() + "'s wand");
			}
		} else {
			if (sender != player) {
				sender.sendMessage(player.getName() + "'s wand does not have " + spellName);
			}
		}

		return true;
	}

	public boolean onWandName(CommandSender sender, Player player, String[] parameters)
	{
		if (parameters.length < 1) {
			sender.sendMessage("Use: /wand name <name>");
			return true;
		}

		if (!checkWand(sender, player)) {
			return true;
		}
		
		Mage mage = api.getMage(player);
		Wand wand = mage.getActiveWand();
		
		wand.setName(StringUtils.join(parameters, " "));
		mage.sendMessage(api.getMessages().get("wand.renamed"));
		if (sender != player) {
			sender.sendMessage(api.getMessages().getParameterized("wand.player_renamed", "$name", player.getName()));
		}

		return true;
	}
	
	public boolean onWand(CommandSender sender, Player player, String[] parameters)
	{
		String wandName = null;
		if (parameters.length > 0)
		{
			wandName = parameters[0];
		}
		
		return giveWand(sender, player, wandName, false, true, false, false);
	}
}
