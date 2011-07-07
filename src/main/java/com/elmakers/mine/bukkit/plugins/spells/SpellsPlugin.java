package com.elmakers.mine.bukkit.plugins.spells;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.elmakers.mine.bukkit.persisted.Persistence;
import com.elmakers.mine.bukkit.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;

public class SpellsPlugin extends JavaPlugin
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
		Plugin checkForPersistence = this.getServer().getPluginManager().getPlugin("Persistence");
	    if(checkForPersistence != null) 
	    {
	    	PersistencePlugin plugin = (PersistencePlugin)checkForPersistence;
	    	persistence = plugin.getPersistence();
	    	utilities = plugin.createUtilities(this);
	    } 
	    else 
	    {
	    	log.warning("The Spells plugin depends on Persistence");
	    	this.getServer().getPluginManager().disablePlugin(this);
	    	return;
	    }

	    initialize();
		
        PluginManager pm = getServer().getPluginManager();
		
        pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
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
		spells.initialize(this, persistence, utilities);

		playerListener.setSpells(spells);
		entityListener.setSpells(spells);
        blockListener.setSpells(spells);

		// setup commands
		castCommand = utilities.getPlayerCommand("cast", "Cast spells by name", "<spellname>");
		spellsCommand = utilities.getPlayerCommand("spells", "List spells you know", null);

		castCommand.bind("onCast");
		spellsCommand.bind("onSpells");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		return utilities.dispatch(this, sender, cmd.getName(), args);
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
		
    	SpellVariant spell = spells.getSpell(spellName, player);
    	if (spell == null)
    	{
    		return false;
    	}
    	
    	spells.castSpell(spell, parameters, player);
		
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
		List<SpellVariant> categorySpells = new ArrayList<SpellVariant>();
		List<SpellVariant> spellVariants = spells.getAllSpells();
		for (SpellVariant spell : spellVariants)
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
		for (SpellVariant spell : categorySpells)
		{
			player.sendMessage(spell.getName() + " [" + spell.getMaterial().name().toLowerCase() + "] : " + spell.getDescription());
		}
	}
	
	public void listCategories(Player player)
	{
		HashMap<String, Integer> spellCounts = new HashMap<String, Integer>();
		List<String> spellGroups = new ArrayList<String>();
		List<SpellVariant> spellVariants = spells.getAllSpells();
		
		for (SpellVariant spell : spellVariants)
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
		List<SpellVariant> spellVariants = spells.getAllSpells();
		
		int spellCount = 0;
		for (SpellVariant spell : spellVariants)
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
			for (SpellVariant spell : group.spells)
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
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected PluginCommand castCommand;
	protected PluginCommand spellsCommand;	
	
	private final Spells spells = new Spells();
	private final Logger log = Logger.getLogger("Minecraft");
	private final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	private final SpellsEntityListener entityListener = new SpellsEntityListener();
    private final SpellsBlockListener blockListener = new SpellsBlockListener();
	
}
