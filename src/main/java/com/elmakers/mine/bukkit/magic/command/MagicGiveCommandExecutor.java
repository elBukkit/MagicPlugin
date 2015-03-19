package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.Messages;
import com.elmakers.mine.bukkit.utility.RunnableJob;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MagicGiveCommandExecutor extends MagicTabExecutor {

	private RunnableJob runningTask = null;

	public MagicGiveCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!api.hasPermission(sender, "Magic.commands.mgive"))
        {
            sendNoPermission(sender);
            return true;
        }

        if (args.length == 0 || args.length > 3)
		{
            sender.sendMessage("Usage: mgive [player] <item> [count]");
			return true;
		}

        String playerName = null;
		String itemName = null;
        String countString = null;

        if (args.length == 1) {
            itemName = args[0];
        } else if (args.length == 3) {
            playerName = args[0];
            itemName = args[1];
            countString = args[2];
        } else {
            playerName = args[0];
            Player testPlayer = Bukkit.getPlayer(playerName);
            if (testPlayer == null) {
                itemName = args[0];
                countString = args[1];
            } else {
                itemName = args[1];
            }
        }

        int count = 1;
        if (countString != null) {
            try {
                count = Integer.parseInt(countString);
            } catch (Exception ex) {
                sender.sendMessage("Error parsing count: " + countString + ", should be an integer.");
                return true;
            }
        }

        Player player = null;
        if (playerName != null) {
            player = Bukkit.getPlayer(playerName);
        }

        if (player == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Console usage: mgive <player> <item> [count]");
                return true;
            }
            player = (Player)sender;
        }

        if (itemName.equalsIgnoreCase("xp")) {
            api.giveExperienceToPlayer(player, count);
            sender.sendMessage("Gave " + count + " experience to " + player.getName());
            return true;
        } else {
            ItemStack item = api.createItem(itemName);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Unknown item type " + itemName);
                return true;
            }
            item.setAmount(count);
            String displayName = api.describeItem(item);
            sender.sendMessage("Gave " + count + " " + displayName + " to " + player.getName());
            api.giveItemToPlayer(player, item);
        }

        return true;
	}

	protected boolean onMagicGive(CommandSender sender, Player player, String command, String[] args)
	{
		String playerCommand = (sender instanceof Player) ? "" : "<player> ";
		String usageString = "Usage: /magic give " + playerCommand + "<spellname|'material'|'upgrade'|'wand'> [materialname|wandname]";
		if (args.length == 0) {
			sender.sendMessage(usageString);
			return true;
		}
		
		String key = "";
		boolean isMaterial = false;
		boolean isWand = false;
		boolean isUpgrade = false;
		
		if (args.length > 1 && !args[0].equals("material") && !args[0].equals("wand") && !args[0].equals("upgrade")) {
			sender.sendMessage(usageString);
			return true;
		}
		
		if (args[0].equals("wand")) {
			isWand = true;
			key = args.length > 1 ? args[1] : "";
		} else if (args[0].equals("upgrade")) {
			isUpgrade = true;
			key =  args.length > 1 ? args[1] : "";
		} else if (args[0].equals("material")) {
			if (args.length < 2) {
				sender.sendMessage(usageString);
				return true;
			}
			isMaterial = true;
			key = args[1];
		} else {
			key = args[0];
		}

        boolean giveItem = command.equals("give") || command.equals("sell");
        boolean showWorth = command.equals("worth") || command.equals("sell");
        boolean giveValue = command.equals("sell");

		if (isWand) {
			giveWand(sender, player, key, false, giveItem, giveValue, showWorth);
		} else if (isMaterial) {
			onGiveBrush(sender, player, key, false, giveItem, giveValue, showWorth);
		} else if (isUpgrade) {
			onGiveUpgrade(sender, player, key, false, giveItem, giveValue, showWorth);
		} else {
			onGive(sender, player, key, giveItem, giveValue, showWorth);
		}
		
		return true;
	}
	
	protected void onGive(CommandSender sender, Player player, String key, boolean giveItem, boolean giveValue, boolean showWorth)
	{
		if (!onGiveSpell(sender, player, key, true, giveItem, giveValue, showWorth)) {
			if (!onGiveBrush(sender, player, key, true, giveItem, giveValue, showWorth))
			{
				if (!giveWand(sender, player, key, true, giveItem, giveValue, showWorth))
				{
					sender.sendMessage("Failed to create a spell, brush or wand item for " + key);
				}
			}
		}
	}
	
	protected boolean onGiveSpell(CommandSender sender, Player player, String spellKey, boolean quiet, boolean giveItem, boolean giveValue, boolean showWorth)
	{
		ItemStack itemStack = api.createSpellItem(spellKey);
		if (itemStack == null) {
			if (!quiet) sender.sendMessage("Failed to spell spell item for " + spellKey);
			return false;
		}

        if (giveItem) {
            api.giveItemToPlayer(player, itemStack);
            if (sender != player && !quiet) {
                sender.sendMessage("Gave spell " + spellKey + " to " + player.getName());
            }
        }
        if (showWorth) {
            showWorth(sender, itemStack);
        }
		return true;
	}
	
	protected boolean onGiveBrush(CommandSender sender, Player player, String materialKey, boolean quiet, boolean giveItem, boolean giveValue, boolean showWorth)
	{
		ItemStack itemStack = api.createBrushItem(materialKey);
		if (itemStack == null) {
			if (!quiet) sender.sendMessage("Failed to material spell item for " + materialKey);
			return false;
		}

        if (giveItem) {
            api.giveItemToPlayer(player, itemStack);
            if (sender != player && !quiet) {
                sender.sendMessage("Gave brush " + materialKey + " to " + player.getName());
            }
        }
        if (showWorth) {
            showWorth(sender, itemStack);
        }
		return true;
	}
	
	protected boolean onGiveUpgrade(CommandSender sender, Player player, String wandKey, boolean quiet, boolean giveItem, boolean giveValue, boolean showWorth)
	{
		Mage mage = api.getMage(player);
		Wand currentWand =  mage.getActiveWand();
		if (currentWand != null) {
			currentWand.closeInventory();
		}
	
		Wand wand = api.createWand(wandKey);
		if (wand != null) {
			wand.makeUpgrade();
            if (giveItem) {
                api.giveItemToPlayer(player, wand.getItem());
                if (sender != player && !quiet) {
                    sender.sendMessage("Gave upgrade " + wand.getName() + " to " + player.getName());
                }
            }
            if (showWorth) {
                showWorth(sender, wand.getItem());
            }
		} else  {
			if (!quiet) sender.sendMessage(api.getMessages().getParameterized("wand.unknown_template", "$name", wandKey));
			return false;
		}
		return true;
	}
	
	protected void checkRunningTask()
	{
		if (runningTask != null && runningTask.isFinished()) {
			runningTask = null;
		}
	}
	
	@Override
	public Collection<String> onTabComplete(CommandSender sender, String commandName, String[] args) {
		List<String> options = new ArrayList<String>();
        if (!sender.hasPermission("Magic.commands.mgive")) return options;

		if (args.length == 1) {
            options.addAll(api.getPlayerNames());
		}

        if (args.length == 1 || args.length == 2) {
            Collection<SpellTemplate> spellList = api.getSpellTemplates();
            for (SpellTemplate spell : spellList) {
                options.add(spell.getKey());
            }
            Collection<String> allWands = api.getWandKeys();
            for (String wandKey : allWands) {
                options.add(wandKey);
            }
            options.addAll(api.getBrushes());
		}
		return options;
	}
}
