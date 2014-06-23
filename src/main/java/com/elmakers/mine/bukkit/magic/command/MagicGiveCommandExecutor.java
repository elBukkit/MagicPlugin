package com.elmakers.mine.bukkit.magic.command;

import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
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

        if (itemName.contains("spell:")) {
            String spellKey = itemName.substring(6);
            ItemStack itemStack = api.createSpellItem(spellKey);
            if (itemStack == null) {
                sender.sendMessage("Failed to create spell item for " + spellKey);
                return true;
            }

            itemStack.setAmount(count);
            api.giveItemToPlayer(player, itemStack);
            if (sender != player) {
                sender.sendMessage("Gave spell " + spellKey + " to " + player.getName());
            }
        } else if (itemName.contains("wand:")) {
            String wandKey = itemName.substring(5);
            giveWand(sender, player, wandKey, false, true, false, false);
        } else if (itemName.contains("upgrade:")) {
            String wandKey = itemName.substring(6);
            Wand wand = api.createWand(wandKey);
            if (wand == null) {
                sender.sendMessage("Failed to create upgrade item for " + wandKey);
                return true;
            }

            wand.makeUpgrade();
            api.giveItemToPlayer(player, wand.getItem());
            sender.sendMessage("Gave upgrade " + wand.getName() + " to " + player.getName());
            return true;
        } else if (itemName.contains("brush:")) {
            String brushKey = itemName.substring(6);
            ItemStack itemStack = api.createBrushItem(brushKey);
            if (itemStack == null) {
                sender.sendMessage("Failed to create brush item for " + brushKey);
                return false;
            }

            itemStack.setAmount(count);
            api.giveItemToPlayer(player, itemStack);
            sender.sendMessage("Gave brush " + brushKey + " to " + player.getName());
        } else {
            Wand wand = api.createWand(itemName);
            if (wand != null) {
                Mage mage = api.getMage(player);
                Wand currentWand =  mage.getActiveWand();
                if (currentWand != null) {
                    currentWand.closeInventory();
                }
                api.giveItemToPlayer(player, wand.getItem());
                sender.sendMessage("Gave wand " + wand.getName() + " to " + player.getName());
                return true;
            }
            ItemStack itemStack = api.createSpellItem(itemName);
            if (itemStack != null) {
                itemStack.setAmount(count);
                api.giveItemToPlayer(player, itemStack);
                sender.sendMessage("Gave spell " + itemName + " to " + player.getName());
                return true;
            }
            MaterialAndData item = new MaterialAndData(itemName);
            if (item.isValid()) {
                api.giveItemToPlayer(player, item.getItemStack(count));
                sender.sendMessage("Gave " + count + " of " + item.getName() + " to " + player.getName());
                return true;
            }
            itemStack = api.createBrushItem(itemName);
            if (itemStack != null) {
                itemStack.setAmount(count);
                api.giveItemToPlayer(player, itemStack);
                sender.sendMessage("Gave brush " + itemName + " to " + player.getName());
            }
        }

        sender.sendMessage(ChatColor.RED + "Unknown item type " + itemName);

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
			if (!quiet) sender.sendMessage(Messages.getParameterized("wand.unknown_template", "$name", wandKey));
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
	public List<String> onTabComplete(CommandSender sender, String comandName, String[] args) {
		List<String> options = new ArrayList<String>();
		if (args.length == 1) {
			addIfPermissible(sender, options, "Magic.commands.magic.", "clean");
			addIfPermissible(sender, options, "Magic.commands.magic.", "clearcache");
			addIfPermissible(sender, options, "Magic.commands.magic.", "cancel");
			addIfPermissible(sender, options, "Magic.commands.magic.", "load");
			addIfPermissible(sender, options, "Magic.commands.magic.", "save");
			addIfPermissible(sender, options, "Magic.commands.magic.", "commit");
			addIfPermissible(sender, options, "Magic.commands.magic.", "give");
            addIfPermissible(sender, options, "Magic.commands.magic.", "worth");
            addIfPermissible(sender, options, "Magic.commands.magic.", "sell");
			addIfPermissible(sender, options, "Magic.commands.magic.", "list");
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("list")) {
				addIfPermissible(sender, options, "Magic.commands.magic.list", "maps");
				addIfPermissible(sender, options, "Magic.commands.magic.list", "wands");
				addIfPermissible(sender, options, "Magic.commands.magic.list", "automata");
			} else if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("worth") || args[0].equalsIgnoreCase("sell")) {
				options.add("wand");
				options.add("material");
				options.add("upgrade");
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
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("sell")) {
				if (args[1].equalsIgnoreCase("upgrade") || args[1].equalsIgnoreCase("wand")) {
					Collection<String> allWands = api.getWandKeys();
					for (String wandKey : allWands) {
						options.add(wandKey);
					}
				} else if (args[1].equalsIgnoreCase("material")) {
					options.addAll(api.getBrushes());
				}
			}
		}
		Collections.sort(options);
		return options;
	}
}
