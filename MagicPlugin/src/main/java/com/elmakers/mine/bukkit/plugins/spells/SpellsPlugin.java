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

import com.elmakers.mine.bukkit.magic.Magic;
import com.elmakers.mine.bukkit.magic.SpellGroup;
import com.elmakers.mine.bukkit.magic.dao.SpellVariant;
import com.elmakers.mine.bukkit.persistence.Persistence;
import com.elmakers.mine.bukkit.persistence.dao.PluginCommand;
import com.elmakers.mine.bukkit.plugins.persistence.PersistencePlugin;
import com.elmakers.mine.bukkit.utilities.PluginUtilities;

public class SpellsPlugin extends JavaPlugin
{	
	/*
	 * Public API
	 */
	public Magic getSpells()
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
		
        pm.registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
        
        pm.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Normal, this);   
        pm.registerEvent(Type.ENTITY_DAMAGED, entityListener, Priority.Normal, this);
         
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
	}
	
	protected void initialize()
	{
		bindNetherGatePlugin();

		spells.initialize(getServer(), persistence, utilities);

		playerListener.setSpells(spells);
		entityListener.setSpells(spells);

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

	public boolean onSpells(Player player, String[] parameters)
	{
		listSpells(player);

		return true;
	}

	/* 
	 * Help commands
	 */

	public void listSpellsByCategory(Player player, String category)
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
			player.sendMessage(spell.getName() + " : " + spell.getDescription());
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
	
	public void listSpells(Player player)
	{
		HashMap<String, SpellGroup> spellGroups = new HashMap<String, SpellGroup>();
		List<SpellVariant> spellVariants = spells.getAllSpells();
		
		for (SpellVariant spell : spellVariants)
		{
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
		
		for (SpellGroup group : sortedGroups)
		{
			boolean isFirst = true;
			Collections.sort(group.spells);
			for (SpellVariant spell : group.spells)
			{
				if (spell.hasSpellPermission(player))
				{
					if (isFirst)
					{
						player.sendMessage(group.groupName + ":");
						isFirst = false;
					}
					player.sendMessage(" " + spell.getName() + " : " + spell.getDescription());
				}
			}
		}
	}

	public void onDisable() 
	{
		spells.clear();
	}
	
	protected void bindNetherGatePlugin() 
	{
		/*
		Plugin checkForNether = this.getServer().getPluginManager().getPlugin("NetherGate");

	    if (checkForNether != null) 
	    {
	    	log.info("Spells: found NetherGate! Thanks for using my plugins :)");
	    	NetherGatePlugin plugin = (NetherGatePlugin)checkForNether;
	    	spells.setNether(plugin.getManager());
	    }
	    */
	}

	/*
	 * Private data
	 */
	protected Persistence persistence = null;
	protected PluginUtilities utilities = null;

	protected PluginCommand castCommand;
	protected PluginCommand spellsCommand;	
	
	private final Magic spells = new Magic();
	private final Logger log = Logger.getLogger("Minecraft");
	private final SpellsPlayerListener playerListener = new SpellsPlayerListener();
	private final SpellsEntityListener entityListener = new SpellsEntityListener();
	
}
