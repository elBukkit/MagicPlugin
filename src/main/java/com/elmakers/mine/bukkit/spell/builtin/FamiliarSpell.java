package com.elmakers.mine.bukkit.spell.builtin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.spell.SpellEventType;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class FamiliarSpell extends UndoableSpell implements Listener
{
	private String DEFAULT_FAMILIARS = "Chicken,Sheep,Cow,Pig,Wolf,Villager,MushroomCow,Ozelot,HorseEntity";
	private String DEFAULT_MONSTERS = "Creeper,PigZombie,Skeleton,Spider,Zombie,Giant,Silverfish,CaveSpider,Blaze,Bat,Witch";

	private final Random rand = new Random();
	private PlayerFamiliar familiars = new PlayerFamiliar();
	private int spawnCount = 0;

	public enum FamiliarClass
	{
		SPECIFIC,
		ANY,
		FRIENDLY,
		MONSTER
	}

	public class PlayerFamiliar
	{
		public List<LivingEntity> familiars = null;

		public boolean hasFamiliar()
		{
			return familiars != null;
		}

		public void setFamiliars(List<LivingEntity> f)
		{
			familiars = f;
		}

		public void releaseFamiliar()
		{
			if (familiars != null)
			{
				for (LivingEntity familiar : familiars)
				{
					familiar.setHealth(0);
				}
				familiars = null;
			}
		}

		public void releaseFamiliar(Entity entity)
		{
			if (familiars != null)
			{
				List<LivingEntity> iterate = new ArrayList<LivingEntity>(familiars);
				for (LivingEntity familiar : iterate)
				{
					if (familiar.getUniqueId() == entity.getUniqueId()) {
						familiar.setHealth(0);
						familiars.remove(familiar);
					}
				}
				familiars = null;
			}
		}

		public boolean isFamiliar(Entity e)
		{
			if (familiars == null) return false;

			for (LivingEntity c : familiars)
			{
				if (c.getEntityId() == e.getEntityId()) return true;
			}

			return false;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public SpellResult onCast(ConfigurationSection parameters) 
	{
		spawnCount = 0;	
		
		// For squid spawning
		noTargetThrough(Material.STATIONARY_WATER);
		noTargetThrough(Material.WATER);

		Target target = getTarget();
		if (!target.hasTarget())
		{
			return SpellResult.NO_TARGET;
		}
		Block originalTarget = target.getBlock(); 
		Block targetBlock = originalTarget;
		LivingEntity targetEntity = null;

		boolean hasFamiliar = familiars.hasFamiliar();

		if (hasFamiliar)
		{   // Dispel familiars if you target them and cast
			boolean isFamiliar = target.hasEntity() && familiars.isFamiliar(target.getEntity());
			if (isFamiliar)
			{
				checkListener();
				familiars.releaseFamiliar(target.getEntity());
				return SpellResult.COST_FREE;
			}

			familiars.releaseFamiliar();
		}

		if (target.hasEntity())
		{

			targetBlock = targetBlock.getRelative(BlockFace.SOUTH);
			Entity e = target.getEntity();
			if (e instanceof LivingEntity)
			{
				targetEntity = (LivingEntity)e;
			}
		}

		targetBlock = targetBlock.getRelative(BlockFace.UP);

		EntityType famType = EntityType.PIG;
		FamiliarClass famClass = FamiliarClass.FRIENDLY;
		int famCount = parameters.getInt("count", 1);

		String famTypeName = parameters.getString("type", "");

		if (famTypeName.equalsIgnoreCase("any"))
		{
			famClass = FamiliarClass.ANY;
		}
		else if (famTypeName.equalsIgnoreCase("mob"))
		{
			famClass = FamiliarClass.MONSTER;
		}
		else if (famTypeName.length() > 1)
		{
			// annoying- why do they have to CamelCase???
			String testType = famTypeName.toUpperCase();
			for (EntityType ct : EntityType.values())
			{
				String name = ct.getName();
				if (name != null && name.toUpperCase().equals(testType))
				{
					famType = ct;
					famClass = FamiliarClass.SPECIFIC;
				}
			}
		}

		if (originalTarget.getType() == Material.WATER || originalTarget.getType() == Material.STATIONARY_WATER)
		{
			famType = EntityType.SQUID;
			famClass = FamiliarClass.SPECIFIC;
		}

		List<LivingEntity> newFamiliars = new ArrayList<LivingEntity>();
		Location centerLoc = targetBlock.getLocation();
		for (int i = 0; i < famCount; i++)
		{
			if (famClass != FamiliarClass.SPECIFIC)
			{
				if (famClass == FamiliarClass.ANY)
				{
					int randomFamiliar = rand.nextInt(EntityType.values().length - 1);
					famType = EntityType.values()[randomFamiliar];                        
				}
				else
				{
					String[] types = StringUtils.split(DEFAULT_FAMILIARS, ',');
					if (famClass == FamiliarClass.MONSTER)
					{
						types = StringUtils.split(DEFAULT_MONSTERS, ',');
					}
					int randomFamiliar = rand.nextInt(types.length);
					famType = EntityType.fromName(types[randomFamiliar]);
				}
			}      

			Location targetLoc = centerLoc.clone();
			if (famCount > 1)
			{
				targetLoc.setX(targetLoc.getX() + rand.nextInt(2 * famCount) - famCount);
				targetLoc.setZ(targetLoc.getZ() + rand.nextInt(2 * famCount) - famCount);
			}
			if (famType != null) {
                LivingEntity entity = spawnFamiliar(targetLoc, famType, targetEntity);
				if (entity != null)
				{
					newFamiliars.add(entity);
					spawnCount++;
					registerForUndo(entity);
				}
				
				registerForUndo();
			}
		}

		familiars.setFamiliars(newFamiliars);
		checkListener();
		return SpellResult.CAST;

	}

	protected LivingEntity spawnFamiliar(Location target, EntityType famType, LivingEntity targetEntity)
	{
        LivingEntity familiar = null;
		try {
            World world = getWorld();
            Entity famEntity = null;
            try {
                Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, CreatureSpawnEvent.SpawnReason.class);
                famEntity = (Entity)spawnMethod.invoke(world, target, famType.getEntityClass(), CreatureSpawnEvent.SpawnReason.EGG);
            } catch (Exception ex) {
                famEntity = getWorld().spawnEntity(target, famType);
            }

			if (famEntity == null || !(famEntity instanceof LivingEntity)) return null;
	
			familiar = (LivingEntity)famEntity;
			if (familiar instanceof Skeleton) {
				Skeleton skellie = (Skeleton)familiar;
				skellie.getEquipment().setItemInHand(new ItemStack(Material.BOW));
			}
			if (targetEntity != null)
			{
                if (familiar instanceof Creature) {
                    ((Creature)familiar).setTarget(targetEntity);
                }
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return familiar;
	}

	protected void checkListener()
	{
		if (familiars.hasFamiliar())
		{
			mage.registerEvent(SpellEventType.PLAYER_QUIT, this);
		}
		else
		{
			mage.unregisterEvent(SpellEventType.PLAYER_QUIT, this);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerEvent event)
	{
		if (familiars.hasFamiliar())
		{
			familiars.releaseFamiliar();
			checkListener();
		}
	}
	
	@Override
	public String getMessage(String messageKey, String def) {
		String message = super.getMessage(messageKey, def);
		return message.replace("$count", Integer.toString(spawnCount));
	}
}
