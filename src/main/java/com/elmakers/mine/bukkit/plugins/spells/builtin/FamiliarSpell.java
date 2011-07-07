package com.elmakers.mine.bukkit.plugins.spells.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerEvent;

import com.elmakers.mine.bukkit.plugins.spells.Spell;
import com.elmakers.mine.bukkit.plugins.spells.SpellEventType;
import com.elmakers.mine.bukkit.plugins.spells.Target;
import com.elmakers.mine.bukkit.plugins.spells.utilities.PluginProperties;

public class FamiliarSpell extends Spell
{
	private String DEFAULT_FAMILIARS = "chicken,sheep,cow,pig,wolf";
	private String DEFAULT_MONSTERS = "creeper,pigzombie,skeleton,spider,zombie,ghast,giant,monster";
	
	private List<String> defaultFamiliars = new ArrayList<String>();
	private List<String> defaultMonsters = new ArrayList<String>();
	private final Random rand = new Random();
	private HashMap<String, PlayerFamiliar> familiars = new HashMap<String, PlayerFamiliar>();
	   
    public FamiliarSpell()
    {
        addVariant("monster", Material.PUMPKIN, "combat", "Call a monster to your side", "monster");
        addVariant("mob", Material.JACK_O_LANTERN, "combat", "Call a monster to your side", "mob 20");
        addVariant("farm", Material.WHEAT, "farming", "Create a herd", "30");
    }
    
	public enum FamiliarClass
	{
	    SPECIFIC,
	    ANY,
	    FRIENDLY,
	    MONSTER
	}
	
	public class PlayerFamiliar
	{
		public List<Creature> familiars = null;
		
		public boolean hasFamiliar()
		{
			return familiars != null;
		}
		
		public void setFamiliars(List<Creature> f)
		{
		    familiars = f;
		}
		
		public void releaseFamiliar()
		{
			if (familiars != null)
			{
			    for (Creature familiar : familiars)
			    {
			        familiar.setHealth(0);
			    }
				familiars = null;
			}
		}
		
		public boolean isFamiliar(Entity e)
		{
		    if (familiars == null) return false;
		    
		    for (Creature c : familiars)
		    {
		        if (c.getEntityId() == e.getEntityId()) return true;
		    }
		    
		    return false;
		}
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		noTargetThrough(Material.STATIONARY_WATER);
		noTargetThrough(Material.WATER);
		
		targetEntity(LivingEntity.class);
		Target target = getTarget();
		if (target == null || !target.hasTarget())
		{
			castMessage(player, "No target");
			return false;
		}
		Block targetBlock = target.getBlock();
		LivingEntity targetEntity = null;
		targetBlock = targetBlock.getFace(BlockFace.UP);
       
		PlayerFamiliar fam = getFamiliar(player.getName());	
		boolean hasFamiliar = fam.hasFamiliar();
		
    	if (hasFamiliar)
        {   // Dispel familiars if you target them and cast
    	    boolean isFamiliar = target.isEntity() && fam.isFamiliar(target.getEntity());
            fam.releaseFamiliar();
            if (isFamiliar)
            {
                castMessage(player, "You release your familiar(s)");
                checkListener();
                return true;
            }
        }
		if (target.isEntity())
		{
		    
		    targetBlock = targetBlock.getFace(BlockFace.SOUTH);
		    Entity e = target.getEntity();
		    if (e instanceof LivingEntity)
		    {
		        targetEntity = (LivingEntity)e;
		    }
		}
		
		CreatureType famType = CreatureType.PIG;
		FamiliarClass famClass = FamiliarClass.FRIENDLY;
		int famCount = 1;
		for (String parameter : parameters)
		{
		    try
		    {
		        famCount = Integer.parseInt(parameter);
		    }
		    catch (NumberFormatException e)
		    {
		        famCount = 1;
		        if (parameter.equalsIgnoreCase("any"))
                {
                    famClass = FamiliarClass.ANY;
                }
                else if (parameter.equalsIgnoreCase("mob"))
                {
                    famClass = FamiliarClass.MONSTER;
                }
                else
                {
                    // annoying- why do they have to CamelCase???
                    String testType = parameters[0].toUpperCase();
                    for (CreatureType ct : CreatureType.values())
                    {
                        if (ct.getName().toUpperCase().equals(testType))
                        {
                            famType = ct;
                        }
                    }
                    famClass = FamiliarClass.SPECIFIC;
                }
		    }	
		}
		
		if (targetBlock.getType() == Material.WATER || targetBlock.getType() == Material.STATIONARY_WATER)
		{
			famType = CreatureType.SQUID;
		}
		
		List<Creature> familiars = new ArrayList<Creature>();
		int spawnCount = 0;
		for (int i = 0; i < famCount; i++)
		{
            if (famClass != FamiliarClass.SPECIFIC)
            {
                if (famClass == FamiliarClass.ANY)
                {
                    int randomFamiliar = rand.nextInt(CreatureType.values().length - 1);
                    famType = CreatureType.values()[randomFamiliar];                        
                }
                else
                {
                    List<String> types = defaultFamiliars;
                    if (famClass == FamiliarClass.MONSTER)
                    {
                        types = defaultMonsters;
                    }
                    int randomFamiliar = rand.nextInt(types.size());
                    famType = CreatureType.fromName(types.get(randomFamiliar));
                }
            }      

		    Location targetLoc = target.getLocation();
		    if (famCount > 1)
		    {
		        targetLoc.setX(targetLoc.getX() + rand.nextInt(2 * famCount) - famCount);
		        targetLoc.setZ(targetLoc.getZ() + rand.nextInt(2 * famCount) - famCount);
		    }
		    Creature entity =  spawnFamiliar(targetLoc, famType, targetEntity);
		    if (entity != null)
		    {
		        familiars.add(entity);
		        spawnCount++;
		    }
		}
		
		String typeMessage = "";
		if (famClass == FamiliarClass.SPECIFIC)
		{
		    typeMessage = " " + famType.name().toLowerCase();
		}
		else if (famClass != FamiliarClass.ANY)
		{
		    typeMessage = " " + famClass.name().toLowerCase();
		}
		castMessage(player, "You create " + famCount + typeMessage +" familiar(s)!");
		fam.setFamiliars(familiars);
		checkListener();
		return true;
	
	}
		
	protected Creature spawnFamiliar(Location target, CreatureType famType, LivingEntity targetEntity)
	{
	    LivingEntity famEntity = player.getWorld().spawnCreature(target, famType);
	    if (!(famEntity instanceof Creature)) return null;
	    
	    Creature familiar = (Creature)famEntity;
	    if (targetEntity != null)
	    {
	        familiar.setTarget(targetEntity);
	    }
		return familiar;
	}

	protected PlayerFamiliar getFamiliar(String playerName)
	{
		PlayerFamiliar familiar = familiars.get(playerName);
		if (familiar == null)
		{
			familiar = new PlayerFamiliar();
			familiars.put(playerName, familiar);
		}
		return familiar;
	}
	
	
	protected void checkListener()
	{
		boolean anyFamiliars = false;
		for (PlayerFamiliar familiar : familiars.values())
		{
			if (familiar.hasFamiliar())
			{
				anyFamiliars = true;
				break;
			}
		}
		if (anyFamiliars)
		{
			spells.registerEvent(SpellEventType.PLAYER_QUIT, this);
		}
		else
		{
			spells.unregisterEvent(SpellEventType.PLAYER_QUIT, this);
		}
	}
	
	@Override
	public String getName()
	{
		return "familiar";
	}

	@Override
	public String getCategory()
	{
		return "farming";
	}

	@Override
	public String getDescription()
	{
		return "Create an animal familiar to follow you around";
	}

	@Override
	public void onLoad(PluginProperties properties)
	{
		defaultFamiliars = properties.getStringList("spells-familiar-animals", DEFAULT_FAMILIARS);
		defaultMonsters = properties.getStringList("spells-familiar-monsters", DEFAULT_MONSTERS);
	}
	
	public void onPlayerQuit(PlayerEvent event)
	{
		PlayerFamiliar fam = getFamiliar(event.getPlayer().getName());
		if (fam.hasFamiliar())
		{
			fam.releaseFamiliar();
			checkListener();
		}
	}

	@Override
	public Material getMaterial()
	{
		return Material.EGG;
	}
	
}
