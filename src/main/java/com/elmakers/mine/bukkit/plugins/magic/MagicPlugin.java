package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class MagicPlugin extends JavaPlugin
{	
	/*
	 * Public API
	 */
	public Spells getSpells()
	{
		return spells;
	}

	/*
	 * Plugin interface
	 */
	
	public void onEnable() 
	{
	    initialize();
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);   
        pm.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
        
        pm.registerEvent(Type.BLOCK_PHYSICS, blockListener, Priority.Normal, this);
         
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}
	
	protected void initialize()
	{
	    bindToPermissions();
	    
	    spells.initialize(this);

		playerListener.setSpells(spells);
		entityListener.setSpells(spells);
        blockListener.setSpells(spells);
	}

	private void bindToPermissions() 
	{
	    if (permissionHandler != null) 
	    {
	        return;
	    }
	    
	    Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
	    
	    if (permissionsPlugin == null) 
	    {
	        log.info("Permissions plugin not found, everyone has full access!");
	        return;
	    }
	    
	    permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	    log.info("Magic: Using permissions plugin: " + ((Permissions)permissionsPlugin).getDescription().getFullName());
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
	    String commandName = cmd.getName();
	    
        if (commandName.equalsIgnoreCase("magic") && args.length > 0)
        {
            String subCommand = args[0];
            if (sender instanceof Player)
            {
                if (!spells.hasPermission((Player)sender, "Magic.commands.magic." + subCommand)) return true;
            }
            if (subCommand.equalsIgnoreCase("reload"))
            {
                spells.clear();
                spells.load();
                return true;
            }
            if (subCommand.equalsIgnoreCase("reset"))
            {   
                spells.reset();
                return true;
            }
            
        }
        
	    // Everything beyond this point is is-game only
	    if (!(sender instanceof Player)) return false;
	    
	    Player player = (Player)sender;

        if (commandName.equalsIgnoreCase("wand"))
        {
            if (!spells.hasPermission(player, "Magic.commands.wand")) return true;
            return onWand(player, args);
        }
	    
	    if (commandName.equalsIgnoreCase("cast"))
        {
	        if (!spells.hasPermission(player, "Magic.commands.cast")) return true;
            return onCast(player, args);
        }

        if (commandName.equalsIgnoreCase("spells"))
        {
            if (!spells.hasPermission(player, "Magic.commands.spells")) return true;
            return onSpells(player, args);
        }
        
	    return false;
	}
	
    public boolean onWand(Player player, String[] parameters)
    {
        if (parameters.length < 1)
        {
            boolean gaveWand = false;
  
            Inventory inventory = player.getInventory();
            if (!inventory.contains(spells.getWandTypeId()))
            {
                ItemStack itemStack = new ItemStack(Material.getMaterial(spells.getWandTypeId()), 1);
                player.getInventory().addItem(itemStack);
                gaveWand = true;
            }
            
            if (!gaveWand)
            {
                showWandHelp(player);
            }
            else
            {
                player.sendMessage("Use /wand again for help, /spells for spell list");
            }
            return true;
        }
        
        String spellName = parameters[0];
        Spell spell = spells.getSpell(spellName, player);
        if (spell == null)
        {
            player.sendMessage("Spell '" + spellName + "' unknown, Use /spells for spell list");
            return true;
        }
        
        ItemStack itemStack = new ItemStack(spell.getMaterial(), 1);
        player.getInventory().addItem(itemStack);
        
        return true;
    }
    
    private void showWandHelp(Player player)
    {
        player.sendMessage("How to use your wand:");
        player.sendMessage(" Type /spells to see what spells you know");
        player.sendMessage(" Place a spell item in your first inventory slot");
        player.sendMessage(" Left-click your wand to cast!");
        player.sendMessage(" Right-click to cycle spells in your inventory");

        if (spells.hasPermission(player, "Magic.commands.wand"))
        {
            player.sendMessage("/wand <spellname> : Give the item necessary to cast a spell");
        }
    }
    
	public boolean onCast(Player player, String[] castParameters)
	{
		if (castParameters.length < 1) return false;
		
		String spellName = castParameters[0];
		String[] parameters = new String[castParameters.length - 1];
		for (int i = 1; i < castParameters.length; i++)
		{
			parameters[i - 1] = castParameters[i];
		}
		
		Spell spell = spells.getSpell(spellName, player);
    	if (spell == null)
    	{
    		return false;
    	}
    	
    	spell.cast(parameters);
		
		return true;
	}
	
	public boolean onReload(CommandSender sender, String[] parameters)
	{
		spells.load();
		sender.sendMessage("Configuration reloaded.");
		return true;
	}

	public boolean onSpells(Player player, String[] parameters)
	{
	    int pageNumber = 1;
	    String category = null;
        if (parameters.length > 0)
        {
            try
            {
                pageNumber = Integer.parseInt(parameters[0]);
            }
            catch (NumberFormatException ex)
            {
                pageNumber = 1;
                category = parameters[0];
            }
        }
		listSpells(player, pageNumber, category);

		return true;
	}
	

	/* 
	 * Help commands
	 */

	public void listSpellsByCategory(Player player,String category)
	{
		List<Spell> categorySpells = new ArrayList<Spell>();
		List<Spell> spellVariants = spells.getAllSpells();
		for (Spell spell : spellVariants)
		{
			if (spell.getCategory().equalsIgnoreCase(category) && spell.hasSpellPermission(player))
			{
				categorySpells.add(spell);
			}
		}
		
		if (categorySpells.size() == 0)
		{
			player.sendMessage("You don't know any spells");
			return;
		}
		
		Collections.sort(categorySpells);
		for (Spell spell : categorySpells)
		{
			player.sendMessage(spell.getName() + " [" + spell.getMaterial().name().toLowerCase() + "] : " + spell.getDescription());
		}
	}
	
	public void listCategories(Player player)
	{
		HashMap<String, Integer> spellCounts = new HashMap<String, Integer>();
		List<String> spellGroups = new ArrayList<String>();
		List<Spell> spellVariants = spells.getAllSpells();
		
		for (Spell spell : spellVariants)
		{
			if (!spell.hasSpellPermission(player)) continue;
			
			Integer spellCount = spellCounts.get(spell.getCategory());
			if (spellCount == null || spellCount == 0)
			{
				spellCounts.put(spell.getCategory(), 1);
				spellGroups.add(spell.getCategory());
			}
			else
			{
				spellCounts.put(spell.getCategory(), spellCount + 1);
			}
		}
		if (spellGroups.size() == 0)
		{
			player.sendMessage("You don't know any spells");
			return;
		}
		
		Collections.sort(spellGroups);
		for (String group : spellGroups)
		{
			player.sendMessage(group + " [" + spellCounts.get(group) + "]");
		}
	}
	
	public void listSpells(Player player, int pageNumber, String category)
	{
	    if (category != null)
	    {
	        listSpellsByCategory(player, category);
	        return;
	    }
	    
		HashMap<String, SpellGroup> spellGroups = new HashMap<String, SpellGroup>();
		List<Spell> spellVariants = spells.getAllSpells();
		
		int spellCount = 0;
		for (Spell spell : spellVariants)
		{
		    if (!spell.hasSpellPermission(player))
            {
		        continue;
            }
		    spellCount++;
			SpellGroup group = spellGroups.get(spell.getCategory());
			if (group == null)
			{
				group = new SpellGroup();
				group.groupName = spell.getCategory();
				spellGroups.put(group.groupName, group);	
			}
			group.spells.add(spell);
		}
		
		List<SpellGroup> sortedGroups = new ArrayList<SpellGroup>();
		sortedGroups.addAll(spellGroups.values());
		Collections.sort(sortedGroups);
		
		int maxLines = 5;
		int maxPages = spellCount / maxLines + 1;
		if (pageNumber > maxPages)
		{
		    pageNumber = maxPages;
		}
		
		player.sendMessage("You know " + spellCount + " spells. [" + pageNumber + "/" + maxPages + "]");
		
		int currentPage = 1;
        int lineCount = 0;
        int printedCount = 0;
		for (SpellGroup group : sortedGroups)
		{
		    if (printedCount > maxLines) break;
            
			boolean isFirst = true;
			Collections.sort(group.spells);
			for (Spell spell : group.spells)
			{
			    if (printedCount > maxLines) break;
			    
				if (currentPage == pageNumber)
				{
				    if (isFirst)
                    {
                        player.sendMessage(group.groupName + ":");
                        isFirst = false;
                    }
				    player.sendMessage(" " + spell.getName() + " [" + spell.getMaterial().name().toLowerCase() + "] : " + spell.getDescription());
				    printedCount++;
				}
				lineCount++;
				if (lineCount == maxLines)
				{
				    lineCount = 0;
				    currentPage++;
				}	
			}
		}
	}

	public void onDisable() 
	{
		spells.clear();
	}

	public static PermissionHandler getPermissionHandler()
	{
	    return permissionHandler;
	}
	
	/*
	 * Private data
	 */	
	private final Spells spells = new Spells();
	private final Logger log = Logger.getLogger("Minecraft");
	private final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	private final SpellsEntityListener entityListener = new SpellsEntityListener();
    private final SpellsBlockListener blockListener = new SpellsBlockListener();
    
    // Permissions
    public static PermissionHandler permissionHandler;
	
}
