package com.elmakers.mine.bukkit.spell.builtin;

import java.lang.reflect.Method;
import java.util.*;

import com.elmakers.mine.bukkit.utility.AscendingPair;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
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
    private final LinkedList<WeightedPair<String>> entityTypeProbability = new LinkedList<WeightedPair<String>>();

	private final Random rand = new Random();
	private PlayerFamiliar familiars = new PlayerFamiliar();
	private int spawnCount = 0;
    private CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.EGG;

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
        boolean track = parameters.getBoolean("track", true);

		if (hasFamiliar && track)
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

		EntityType famType = null;
		int famCount = parameters.getInt("count", 1);

		String famTypeName = parameters.getString("type", "");
        if (famTypeName != null) {
            try {
                famType = EntityType.fromName(famTypeName);
            } catch (Throwable ex) {
                mage.sendMessage("Unknown entity type: " + famTypeName);
                return SpellResult.FAIL;
            }
        }

		if (originalTarget.getType() == Material.WATER || originalTarget.getType() == Material.STATIONARY_WATER)
		{
			famType = EntityType.SQUID;
		}

        boolean spawnBaby = parameters.getBoolean("baby", false);

		List<LivingEntity> newFamiliars = new ArrayList<LivingEntity>();
		Location centerLoc = targetBlock.getLocation();
		for (int i = 0; i < famCount; i++)
		{
            EntityType entityType = famType;
			if (entityType == null)
			{
				String randomType = RandomUtils.weightedRandom(entityTypeProbability);
                try {
                    entityType = EntityType.fromName(randomType);
                } catch (Throwable ex) {
                    mage.sendMessage("Unknown entity type: " + randomType);
                    return SpellResult.FAIL;
                }
			}

            if (parameters.contains("reason")) {
                String reasonText = parameters.getString("reason").toUpperCase();
                try {
                    spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
                } catch (Exception ex) {
                    mage.sendMessage("Unknown spawn reason: " + reasonText);
                    return SpellResult.FAIL;
                }
            }

			Location targetLoc = centerLoc.clone();
			if (famCount > 1)
			{
				targetLoc.setX(targetLoc.getX() + rand.nextInt(2 * famCount) - famCount);
				targetLoc.setZ(targetLoc.getZ() + rand.nextInt(2 * famCount) - famCount);
			}
			if (entityType != null) {
                LivingEntity entity = spawnFamiliar(targetLoc, entityType, targetEntity);
				if (entity != null)
				{
                    if (spawnBaby && entity instanceof Ageable) {
                        Ageable ageable = (Ageable)entity;
                        ageable.setBaby();
                    }
					newFamiliars.add(entity);
					spawnCount++;
					registerForUndo(entity);
				}
				
				registerForUndo();
			}
		}

        if (track) {
            familiars.setFamiliars(newFamiliars);
            checkListener();
        }
		return SpellResult.CAST;

	}

	protected LivingEntity spawnFamiliar(Location target, EntityType famType, LivingEntity targetEntity)
	{
        LivingEntity familiar = null;
		try {
            World world = getWorld();
            Entity famEntity;
            try {
                Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, CreatureSpawnEvent.SpawnReason.class);
                famEntity = (Entity)spawnMethod.invoke(world, target, famType.getEntityClass(), spawnReason);
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

    @Override
    protected void loadTemplate(ConfigurationSection template)
    {
        super.loadTemplate(template);

        if (template.contains("entity_types")) {
            RandomUtils.populateStringProbabilityMap(entityTypeProbability, template.getConfigurationSection("entity_types"), 0, 0, 0);
        } else {
            entityTypeProbability.add(new WeightedPair<String>(100.0f, "pig"));
        }
    }
}
