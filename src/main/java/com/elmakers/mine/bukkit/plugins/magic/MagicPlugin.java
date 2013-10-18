package com.elmakers.mine.bukkit.plugins.magic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.utilities.InventoryUtils;

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
		
        pm.registerEvents(spells, this);
        
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}
	
	protected void initialize()
	{
	    spells.initialize(this);
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
    	boolean holdingWand = Spells.isWandActive(player);
    	
        if (parameters.length < 1)
        {
            if (!holdingWand)
            {
            	ItemStack itemStack = new ItemStack(Material.STICK);
                itemStack.addUnsafeEnchantment(Spells.MagicEnchantment, 1);
                Spells.updateWand(itemStack, 0, "Wand");
                
                // Place directly in hand if possible
                PlayerInventory inventory = player.getInventory();
                ItemStack inHand = inventory.getItemInHand();
        		if (inHand == null || inHand.getType() == Material.AIR) {
        			PlayerSpells playerSpells = spells.getPlayerSpells(player);
        			inventory.setItem(inventory.getHeldItemSlot(), itemStack);
        			if (playerSpells.storeInventory()) {
        	    		// Create spell inventory
        	    		spells.updateWandInventory(player);
        	    	}
        		} else {
        			player.getInventory().addItem(itemStack);
        		}
                
                player.sendMessage("Use /wand again for help, /spells for spell list");
            }
            else 
            {
                showWandHelp(player);
            }
            return true;
        }
        
        if (!holdingWand) 
        {
        	player.sendMessage("Equip a wand to enchant with a spell");
            return true;
        }
        
        String spellName = parameters[0];
        Spell spell = spells.getSpell(spellName, player);
        if (spell == null)
        {
            player.sendMessage("Spell '" + spellName + "' unknown, Use /spells for spell list");
            return true;
        }
        
        PlayerInventory inventory = player.getInventory();
        int wandSlot = inventory.getHeldItemSlot();
        ItemStack newWand = addSpellToWand(player, spell, inventory.getItemInHand());
        inventory.setItem(wandSlot, newWand);    	
        spells.updateWandInventory(player);
        return true;
    }
    
    private ItemStack addSpellToWand(Player player, Spell spell, ItemStack wand) {
    	if (!Spells.isWand(wand)) {
    		return wand;
    	}
    	
    	// Add new spell to wand's spell list
    	String spellString = InventoryUtils.getMeta(wand, "magic_spells");
    	if (spellString == null) spellString = "";
    	
    	String[] spells = StringUtils.split(spellString, "|");
    	Map<String, Boolean> spellMap = new HashMap<String, Boolean>();
    	for (int i = 0; i < spells.length; i++) {
    		spellMap.put(spells[i], true);
    	}
    	spellMap.put(spell.getName(), true);
    	Collection<String> newSpells = spellMap.keySet();
    	return Spells.setWandSpells(wand, newSpells);
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
	
	/*
	 * Private data
	 */	
	private final Spells spells = new Spells();
	private final Logger log = Logger.getLogger("Minecraft");
}
