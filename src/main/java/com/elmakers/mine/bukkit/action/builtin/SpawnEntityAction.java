package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class SpawnEntityAction extends BaseSpellAction
{
    private final LinkedList<WeightedPair<String>> entityTypeProbability = new LinkedList<WeightedPair<String>>();

    private CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.EGG;
    private WeakReference<Entity> current = null;

    private boolean track = true;
    private boolean loot = false;
    private boolean setTarget = false;
    private String entityName;
    private EntityType entityType;
    private boolean spawnBaby = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        track = parameters.getBoolean("track", true);
        loot = parameters.getBoolean("loot", false);
        entityName = parameters.getString("name", "");
        spawnBaby = parameters.getBoolean("baby", false);
        setTarget = parameters.getBoolean("set_target", false);
        try {
            String entityTypeName = parameters.getString("type", "");
            if (!entityTypeName.isEmpty())
            {
                entityType = EntityType.valueOf(entityTypeName.toUpperCase());
            }
        } catch(Exception ex) {
            entityType = null;
        }

        if (parameters.contains("reason"))
        {
            String reasonText = parameters.getString("reason").toUpperCase();
            try {
                spawnReason = CreatureSpawnEvent.SpawnReason.valueOf(reasonText);
            } catch (Exception ex) {
                spawnReason = CreatureSpawnEvent.SpawnReason.EGG;
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        Block targetBlock = context.getTargetBlock();
        Entity currentEntity = current == null ? null : current.get();
        current = null;
        if (currentEntity != null)
        {
            currentEntity.remove();
        }

		targetBlock = targetBlock.getRelative(BlockFace.UP);

        Location spawnLocation = targetBlock.getLocation();
        Location sourceLocation = context.getLocation();
        spawnLocation.setPitch(sourceLocation.getPitch());
        spawnLocation.setYaw(sourceLocation.getYaw());

        EntityType useType = entityType;
        if (useType == null)
        {
            String randomType = RandomUtils.weightedRandom(entityTypeProbability);
            try {
                useType = EntityType.valueOf(randomType.toUpperCase());
            } catch (Throwable ex) {
                useType = null;
            }
        }
        if (useType == null)
        {
            return SpellResult.FAIL;
        }

        final Entity spawnedEntity = spawnEntity(spawnLocation, entityType);
        if (spawnedEntity == null) {
            return SpellResult.FAIL;
        }

        MageController controller = context.getController();
        LivingEntity livingEntity = spawnedEntity instanceof LivingEntity ? (LivingEntity)spawnedEntity : null;
        if (entityName != null && !entityName.isEmpty() && livingEntity != null)
        {
            livingEntity.setCustomName(entityName);
        }
        if (!loot)
        {
            livingEntity.setMetadata("nodrops", new FixedMetadataValue(controller.getPlugin(), true));
        }
        if (spawnBaby && spawnedEntity instanceof Ageable)
        {
            Ageable ageable = (Ageable)spawnedEntity;
            ageable.setBaby();
        }
        context.registerForUndo(spawnedEntity);

        if (track)
        {
            current = new WeakReference<Entity>(spawnedEntity);
        }
        if (setTarget) {
            context.setTargetEntity(spawnedEntity);
        }
		return SpellResult.CAST;

	}

	protected Entity spawnEntity(Location target, EntityType famType)
	{
        Entity entity = null;
		try {
            World world = target.getWorld();
            try {
                Method spawnMethod = world.getClass().getMethod("spawn", Location.class, Class.class, CreatureSpawnEvent.SpawnReason.class);
                entity = (Entity)spawnMethod.invoke(world, target, famType.getEntityClass(), spawnReason);
            } catch (Exception ex) {
                entity = target.getWorld().spawnEntity(target, famType);
            }
			if (entity != null && entity instanceof Skeleton) {
				Skeleton skellie = (Skeleton)entity;
				skellie.getEquipment().setItemInHand(new ItemStack(Material.BOW));
            }
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return entity;
	}

    @Override
    public void initialize(ConfigurationSection parameters)
    {
        super.initialize(parameters);

        if (parameters.contains("entity_types"))
        {
            RandomUtils.populateStringProbabilityMap(entityTypeProbability, parameters.getConfigurationSection("entity_types"), 0, 0, 0);
        } else {
            entityTypeProbability.add(new WeightedPair<String>(100.0f, "pig"));
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("track");
        parameters.add("loot");
        parameters.add("baby");
        parameters.add("name");
        parameters.add("type");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("type")) {
            for (EntityType type : EntityType.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("track") || parameterKey.equals("loot") || parameterKey.equals("baby")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("name")) {
            examples.add("Philbert");
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
