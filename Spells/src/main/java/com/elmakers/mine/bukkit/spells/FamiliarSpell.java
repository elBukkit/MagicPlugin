package com.elmakers.mine.bukkit.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerEvent;

import com.elmakers.mine.bukkit.magic.Spell;
import com.elmakers.mine.bukkit.magic.SpellEventType;

public class FamiliarSpell extends Spell
{
	private String DEFAULT_FAMILIARS = "chicken,sheep,cow,pig";
	private String DEFAULT_MONSTERS = "creeper,pigzombie,skeleton,spider,squid,zombie,ghast,giant";
	
	private List<String> defaultFamiliars = new ArrayList<String>();
	private List<String> defaultMonsters = new ArrayList<String>();
	private final Random rand = new Random();
	private HashMap<String, PlayerFamiliar> familiars = new HashMap<String, PlayerFamiliar>();
	
	class PlayerFamiliar
	{
		public EntityLiving familiar = null;
		
		public boolean hasFamiliar()
		{
			return familiar != null;
		}
		
		public void setFamiliar(EntityLiving f)
		{
			familiar = f;
		}
		
		public void releaseFamiliar()
		{
			if (familiar != null)
			{
				familiar.health = 0;
				familiar = null;
			}
		}
	}
	
	enum FamiliarType
	{
		CHICKEN,
		SHEEP,
		COW,
		PIG,
		CREEPER,
		PIGZOMBIE,
		SKELETON,
		SPIDER,
		SQUID,
		ZOMBIE,
		GHAST,
		GIANT,
		//FISH,
		//SLIME,
		UNKNOWN;
		
		public static FamiliarType parseString(String s)
		{
			return parseString(s, UNKNOWN);
		}
		
		public static FamiliarType parseString(String s, FamiliarType defaultFamiliarType)
		{
			FamiliarType foundType = defaultFamiliarType;
			for (FamiliarType t : FamiliarType.values())
			{
				if (t.name().equalsIgnoreCase(s))
				{
					foundType = t;
				}
			}
			return foundType;
		}
		
	};
	
	public FamiliarSpell()
	{
		addVariant("monster", Material.PUMPKIN, "combat", "Call a monster to your side", "monster");
	}
	
	@Override
	public boolean onCast(String[] parameters)
	{
		PlayerFamiliar fam = getFamiliar(player.getName());
		if (fam.hasFamiliar())
		{
			fam.releaseFamiliar();
			castMessage(player, "You release your familiar");
			checkListener();
			return true;
		}
		else
		{
			noTargetThrough(Material.STATIONARY_WATER);
			noTargetThrough(Material.WATER);
			
			Block target = getTargetBlock();
			if (target == null)
			{
				castMessage(player, "No target");
				return false;
			}
			target = target.getFace(BlockFace.UP);
			
			FamiliarType famType = FamiliarType.UNKNOWN;
			if (parameters.length > 0)
			{
				if (parameters[0].equalsIgnoreCase("any"))
				{
					int randomFamiliar = rand.nextInt(FamiliarType.values().length - 1);
					famType = FamiliarType.values()[randomFamiliar];
				}
				else if (parameters[0].equalsIgnoreCase("monster"))
				{
					int randomFamiliar = rand.nextInt(defaultMonsters.size());
					famType = FamiliarType.parseString(defaultMonsters.get(randomFamiliar));
				}
				else
				{
					famType = FamiliarType.parseString(parameters[0]);
				}
			}
			
			if (famType == FamiliarType.UNKNOWN)
			{
				int randomFamiliar = rand.nextInt(defaultFamiliars.size());
				famType = FamiliarType.parseString(defaultFamiliars.get(randomFamiliar));
			}
			
			if (target.getType() == Material.WATER || target.getType() == Material.STATIONARY_WATER)
			{
				famType = FamiliarType.SQUID;
			}
			
			EntityLiving entity =  spawnFamiliar(target, famType);
			if (entity == null)
			{
				sendMessage(player, "Your familiar is DOA");
				return false;
			}
			castMessage(player, "You create a " + famType.name().toLowerCase() + " familiar!");
			fam.setFamiliar(entity);
			checkListener();
			return true;
		}
	}
		
	protected EntityLiving spawnFamiliar(Block target, FamiliarType famType)
	{
		Location location = new Location(player.getWorld(), target.getX(), target.getY(), target.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
		EntityLiving e = null;
		CraftPlayer craftPlayer = (CraftPlayer)player;
		CraftWorld craftWorld = (CraftWorld)craftPlayer.getWorld();
		World world = craftWorld.getHandle();
		
		switch (famType)
		{
			case SHEEP: e = new EntitySheep(world); break;
			case PIG: e = new EntityPig(world); break;
			case CHICKEN: e = new EntityChicken(world); break;
			case COW: e = new EntityCow(world); break;
			case CREEPER: e = new EntityCreeper(world); break;
			case PIGZOMBIE: e = new EntityPigZombie(world); break;
			case SKELETON: e = new EntitySkeleton(world); break;
			case SPIDER: e = new EntitySpider(world); break;
			case SQUID: e = new EntitySquid(world); break;
			case GHAST: e = new EntityGhast(world); break;
			case ZOMBIE: e = new EntityZombie(world); break;
			case GIANT: e = new EntityGiantZombie(world); break;
			//case SLIME: e = new EntitySlime(world); break;
			//case FISH: e = new EntityFish(world); break;
		}
		
		if (e != null)
		{
			e.c(location.getX(), location.getY(), location.getZ(), location.getYaw(), 0.0F);
	        world.a(e);
		}
		return e;
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
