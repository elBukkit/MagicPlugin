package com.elmakers.mine.bukkit.magic.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.maps.URLMap;
import com.elmakers.mine.bukkit.block.batch.SpellBatch;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.RunnableJob;
import com.elmakers.mine.bukkit.utility.TimedRunnable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import com.elmakers.mine.bukkit.api.block.BlockBatch;
import com.elmakers.mine.bukkit.api.magic.Automaton;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.LostWand;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.wand.WandCleanupRunnable;

public class MagicCommandExecutor extends MagicTabExecutor {

	private RunnableJob runningTask = null;
	
	public MagicCommandExecutor(MagicAPI api) {
		super(api);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0)
		{
			if (!api.hasPermission(sender, "Magic.commands.magic")) {
				sendNoPermission(sender);
				return true;
			}
			sender.sendMessage("Magic " + getMagicVersion());
			return true;
		}
		
		String subCommand = args[0];
		if (sender instanceof Player)
		{
			if (!api.hasPermission(sender, "Magic.commands.magic." + subCommand)) {
				sendNoPermission(sender);
				return true;
			}
		}
		if (subCommand.equalsIgnoreCase("save"))
		{
			api.save();
			sender.sendMessage("Data saved.");
			return true;
		}
		if (subCommand.equalsIgnoreCase("load"))
		{		
			api.reload();
			sender.sendMessage("Configuration reloaded.");
			return true;
		}
		if (subCommand.equalsIgnoreCase("clearcache"))
		{		
			api.clearCache();
			sender.sendMessage("Image map and schematic caches cleared.");
			return true;
		}
		if (subCommand.equalsIgnoreCase("commit"))
		{
			if (api.commit()) {
				sender.sendMessage("All changes committed");
			} else {
				sender.sendMessage("Nothing to commit");
			}
			return true;
		}
		if (subCommand.equalsIgnoreCase("give") || subCommand.equalsIgnoreCase("sell"))
		{
			Player player = null;
			int argStart = 1;
			
			if (sender instanceof Player) {
				player = (Player)sender;
			} else {
				argStart = 2;
				player = Bukkit.getPlayer(args[0]);
				if (player == null) {
					sender.sendMessage("Can't find player " + args[0]);
					return true;
				}
				if (!player.isOnline()) {
					sender.sendMessage("Player " + args[0] + " is not online");
					return true;
				}
			}
            boolean showWorth = subCommand.equalsIgnoreCase("sell");
			String[] args2 = Arrays.copyOfRange(args, argStart, args.length);
			return onMagicGive(sender, player, subCommand, args2);
		}
        if (subCommand.equalsIgnoreCase("worth"))
        {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("This command may only be used in-game");
                return true;
            }

            Player player = (Player)sender;
            ItemStack item = player.getItemInHand();
            if (item == null || item.getType() == Material.AIR)
            {
                player.sendMessage("You must be holding an item");
                return true;
            }
            showWorth(player, item);
            return true;
        }
		if (subCommand.equalsIgnoreCase("list"))
		{
			String usage = "Usage: magic list <wands [player]|maps [keyword]|automata|tasks>";
			String listCommand = "";
			if (args.length > 1)
			{
				listCommand = args[1];
				if (!api.hasPermission(sender, "Magic.commands.magic." + subCommand + "." + listCommand)) {
					sendNoPermission(sender);
					return false;
				}
			}
			else
			{				
				sender.sendMessage(ChatColor.GRAY + "For more specific information, add 'tasks', 'wands', 'maps' or 'automata' parameter.");

				Collection<Mage> mages = api.getMages();
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Active mages: " + mages.size());
				Collection<Mage> pending = api.getMagesWithPendingBatches();
				sender.sendMessage(ChatColor.AQUA + "Pending construction batches (" + pending.size() + "): ");
				for (Mage mage : pending) {
                    int totalSize = 0;
                    int totalRemaining = 0;
					Collection<BlockBatch> pendingBatches = mage.getPendingBatches();
                    String names = "";
					if (pendingBatches.size() > 0) {
                        for (BlockBatch batch : pendingBatches) {
                            if (batch instanceof SpellBatch) {
                                names = names + ((SpellBatch)batch).getSpell().getName() + " ";
                            }

                            totalSize += batch.size();
                            totalRemaining = batch.remaining();
                        }
					}

                    sender.sendMessage(ChatColor.AQUA + mage.getName() + ChatColor.GRAY + " has "
                            + ChatColor.WHITE + "" + pendingBatches.size() + "" + ChatColor.GRAY
                            + " pending (" + ChatColor.WHITE + "" + totalRemaining + "/" + totalSize
                            + "" + ChatColor.GRAY + ") (" + names + ")");
				}
				return true;
			}

            if (listCommand.equalsIgnoreCase("tasks")) {
                List<BukkitTask> tasks = Bukkit.getScheduler().getPendingTasks();
                HashMap<String, Integer> pluginCounts = new HashMap<String, Integer>();
                HashMap<String, HashMap<String, Integer>> taskCounts = new HashMap<String, HashMap<String, Integer>>();
                for (BukkitTask task : tasks)  {
                    String pluginName = task.getOwner().getName();
                    HashMap<String, Integer> pluginTaskCounts = taskCounts.get(pluginName);
                    if (pluginTaskCounts == null) {
                        pluginTaskCounts = new HashMap<String, Integer>();
                        taskCounts.put(pluginName, pluginTaskCounts);
                    }
                    String className = "(Unknown)";
                    Runnable taskRunnable = CompatibilityUtils.getTaskRunnable(task);
                    if (taskRunnable != null) {
                        if (taskRunnable instanceof TimedRunnable) {
                            TimedRunnable timed = (TimedRunnable)taskRunnable;
                            long count = timed.getCount();
                            long totalTime = timed.getTotalTime();
                            long avg = count == 0 ? 0 : totalTime / count;
                            className = timed.getName() + "(" + totalTime + ", " + count + ", " + avg + ")";
                        } else {
                            Class<? extends Runnable> taskClass = taskRunnable.getClass();
                            if (taskClass != null) {
                                className = taskClass.getName();
                            }
                        }
                    }
                    Integer count = pluginTaskCounts.get(className);
                    if (count == null) count = 0;
                    count++;
                    pluginTaskCounts.put(className, count);

                    Integer totalCount = pluginCounts.get(pluginName);
                    if (count == null) count = 0;
                    count++;
                    pluginCounts.put(pluginName, count);
                }
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "Active tasks: " + tasks.size());
                for (Entry<String, HashMap<String, Integer>> pluginEntry : taskCounts.entrySet()) {
                    String pluginName = pluginEntry.getKey();
                    sender.sendMessage(" " + ChatColor.DARK_PURPLE + pluginName + ": " + ChatColor.LIGHT_PURPLE + pluginCounts.get(pluginName));
                    for (Entry<String, Integer> taskEntry : pluginEntry.getValue().entrySet()) {
                        sender.sendMessage("  " + ChatColor.DARK_PURPLE + taskEntry.getKey() + ": " + ChatColor.LIGHT_PURPLE + taskEntry.getValue());
                    }
                }

                return true;
            }
			
			if (listCommand.equalsIgnoreCase("wands")) {
				String owner = "";
				if (args.length > 2) {
					owner = args[2];
				}
				Collection<LostWand> lostWands = api.getLostWands();
				int shown = 0;
				for (LostWand lostWand : lostWands) {
					Location location = lostWand.getLocation();
					if (location == null) continue;
					if (owner.length() > 0 && !owner.equalsIgnoreCase	(lostWand.getOwner())) {
						continue;
					}
					shown++;
					sender.sendMessage(ChatColor.AQUA + lostWand.getName() + ChatColor.WHITE + " (" + lostWand.getOwner() + ") @ " + ChatColor.BLUE + location.getWorld().getName() + " " +
							location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
				}
				
				sender.sendMessage(shown + " lost wands found" + (owner.length() > 0 ? " for " + owner : ""));
				return true;
			} else if (listCommand.equalsIgnoreCase("automata")) {
				Collection<Automaton> automata = api.getAutomata();
				for (Automaton automaton : automata) {
					BlockVector location = automaton.getPosition();
					String worldName = automaton.getWorldName();
                    boolean isOnline = false;
                    World world = Bukkit.getWorld(worldName);
                    if (worldName != null) {
                        Location automatonLocation = new Location(world, location.getX(), location.getY(), location.getZ());
                        Chunk chunk = world.getChunkAt(automatonLocation);
                        isOnline = chunk.isLoaded();
                    }
                    ChatColor nameColor = isOnline ? ChatColor.AQUA : ChatColor.GRAY;
					sender.sendMessage(nameColor + automaton.getName() + ChatColor.WHITE + " @ " + ChatColor.BLUE + worldName + " " +
							location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
				}
				
				sender.sendMessage(automata.size() + " automata active");
				return true;
			}
			else if (listCommand.equalsIgnoreCase("maps")) {
				String keyword = "";
				if (args.length > 2) {
					keyword = args[2];
				}

				int shown = 0;
                MageController apiController = api.getController();
                Collection<URLMap> maps = api.getController().getMaps().getAll();
				for (URLMap map : maps) {
					Short mapId = map.getId();
					if (map == null || mapId == null) continue;

					if (map.matches(keyword)) {
						shown++;
						String name = map.getName();
						name = (name == null ? "(None)" : name);
						sender.sendMessage(ChatColor.AQUA + "" + mapId + ChatColor.WHITE + ": " +
								name + " => " + ChatColor.GRAY + map.getURL());
					}
				}
				if (shown == 0) {
					sender.sendMessage("No maps found" + (keyword.length() > 0 ? " matching " + keyword : "") + ", use /castp <player> camera [url|player] [...]");
				} else {
					sender.sendMessage(shown + " maps found matching " + keyword);
				}
				return true;
			}
		
			sender.sendMessage(usage);
			return true;
		}
		if (subCommand.equalsIgnoreCase("cancel"))
		{ 
			checkRunningTask();
			if (runningTask != null) {
				runningTask.cancel();
				runningTask = null;
				sender.sendMessage("Job cancelled");
			} else {
				sender.sendMessage("There is no job running");
			}
			
			int stoppedPending = 0;
			for (Mage mage : api.getMages()) {
				while (mage.cancelPending() != null) stoppedPending++;
			}
			
			sender.sendMessage("Stopped " + stoppedPending + " pending construction batches");
			
			return true;
		}
		if (subCommand.equalsIgnoreCase("clean"))
		{ 
			checkRunningTask();
			if (runningTask != null) {
				sender.sendMessage("Cancel current job first");
				return true;
			}
			World world = null;
			String owner = null;
			if (args.length > 1) {
				owner = args[1];
			}
			if (sender instanceof Player) {
				world = ((Player)sender).getWorld();
			} else {
				if (args.length > 2) {
					String worldName = args[2];
					world = Bukkit.getWorld(worldName);
				}
			}

            boolean check = false;
            if (owner != null && owner.equals("check")){
                check = true;
                owner = "ALL";
            }
            String description = check ? "Checking for" : "Cleaning up";
			String ownerName = owner == null ? "(Unowned)" : owner;
			if (world == null) {
				sender.sendMessage(description + " lost wands in all worlds for owner: " + ownerName);
			} else {
				sender.sendMessage(description + " lost wands in world " + world.getName() + " for owner " + ownerName);
			}
			runningTask = new WandCleanupRunnable(api, world, owner, check);
			runningTask.runTaskTimer(api.getPlugin(), 5, 5);
			
			return true;
		}
		
		sender.sendMessage("Unknown magic command: " + subCommand);
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
			if (!quiet) sender.sendMessage("Failed to create spell item for " + spellKey);
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
			if (!quiet) sender.sendMessage("Failed to create material item for " + materialKey);
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
