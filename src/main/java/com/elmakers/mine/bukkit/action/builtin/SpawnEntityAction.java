package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

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
    private Vector direction = null;
    private double speed;
    private double dyOffset;
    private MaterialAndData item = null;
    private int amount;
    private Skeleton.SkeletonType skeletonType = null;
    private Horse.Variant horseVariant = null;
    private Horse.Color horseColor = null;
    private Horse.Style horseStyle = null;
    private Ocelot.Type ocelotType = null;
    private DyeColor color = null;
    private Double health;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        track = parameters.getBoolean("track", true);
        loot = parameters.getBoolean("loot", false);
        entityName = parameters.getString("name", "");
        spawnBaby = parameters.getBoolean("baby", false);
        setTarget = parameters.getBoolean("set_target", false);
        speed = parameters.getDouble("speed", 0);
        direction = ConfigurationUtils.getVector(parameters, "direction");
        dyOffset = parameters.getDouble("dy_offset", 0);
        item = ConfigurationUtils.getMaterialAndData(parameters, "item");
        amount = parameters.getInt("amount", 1);
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

        horseVariant = null;
        if (parameters.contains("horse_variant")) {
            try {
                String variantString = parameters.getString("horse_variant");
                horseVariant = Horse.Variant.valueOf(variantString.toUpperCase());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        horseColor = null;
        if (parameters.contains("horse_color")) {
            try {
                String colorString = parameters.getString("horse_color");
                horseColor = Horse.Color.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        horseStyle = null;
        if (parameters.contains("horse_style")) {
            try {
                String styleString = parameters.getString("horse_style");
                horseStyle = Horse.Style.valueOf(styleString.toUpperCase());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        ocelotType = null;
        if (parameters.contains("ocelot_type")) {
            try {
                String variantString = parameters.getString("ocelot_type");
                ocelotType = Ocelot.Type.valueOf(variantString.toUpperCase());
            } catch (Exception ex) {
            }
        }

        color = null;
        if (parameters.contains("color")) {
            try {
                String colorString = parameters.getString("color");
                color = DyeColor.valueOf(colorString.toUpperCase());
            } catch (Exception ex) {
            }
        }

        skeletonType = null;
        if (parameters.contains("skeleton_type")) {
            try {
                String skeletonString = parameters.getString("skeleton_type");
                skeletonType = Skeleton.SkeletonType.valueOf(skeletonString.toUpperCase());
            } catch (Exception ex) {
            }
        }

        health = null;
        if (parameters.contains("health")) {
            health = parameters.getDouble("health");
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
        if (spawnedEntity instanceof Ageable) {
            if (spawnBaby) {
                ((Ageable)spawnedEntity).setBaby();
            } else {
                ((Ageable)spawnedEntity).setAdult();
            }
        } else if (spawnedEntity instanceof Zombie) {
            ((Zombie)spawnedEntity).setBaby(spawnBaby);
        } else if (spawnedEntity instanceof PigZombie) {
            ((PigZombie)spawnedEntity).setBaby(spawnBaby);
        } else if (spawnedEntity instanceof Slime && spawnBaby) {
            Slime slime = (Slime)spawnedEntity;
            slime.setSize(slime.getSize() - 2);
        }

        if (spawnedEntity instanceof Horse) {
            if (horseVariant != null) {
                ((Horse)spawnedEntity).setVariant(horseVariant);
            }
            if (horseColor != null) {
                ((Horse)spawnedEntity).setColor(horseColor);
            }
            if (horseStyle != null) {
                ((Horse)spawnedEntity).setStyle(horseStyle);
            }
        }

        if (spawnedEntity instanceof Ocelot && ocelotType != null) {
            Ocelot ocelot = (Ocelot)spawnedEntity;
            ocelot.setCatType(ocelotType);
        }
        if (spawnedEntity instanceof Skeleton && skeletonType != null) {
            Skeleton skeleton = (Skeleton)spawnedEntity;
            skeleton.setSkeletonType(skeletonType);
        }
        if (spawnedEntity instanceof Sheep && color != null) {
            Sheep sheep = (Sheep)spawnedEntity;
            sheep.setColor(color);
        }
        if (spawnedEntity instanceof Wolf && color != null) {
            Wolf wolf = (Wolf)spawnedEntity;
            wolf.setCollarColor(color);
        }
        if (spawnedEntity instanceof Item && item != null) {
            ((Item)spawnedEntity).setItemStack(item.getItemStack(amount));
        }
        if (spawnedEntity instanceof ExperienceOrb) {
            ((ExperienceOrb)spawnedEntity).setExperience(amount);
        }
        if (spawnedEntity instanceof LivingEntity && health != null) {
            ((LivingEntity)spawnedEntity).setMaxHealth(health);
            ((LivingEntity)spawnedEntity).setHealth(health);
        }

        if (speed > 0)
        {
            Vector motion = direction;
            if (motion == null)
            {
                motion = context.getDirection();
            }
            else
            {
                motion = motion.clone();
            }

            if (dyOffset != 0) {
                motion.setY(motion.getY() + dyOffset);
            }
            motion.normalize();
            motion.multiply(speed);
            CompatibilityUtils.setEntityMotion(spawnedEntity, motion);
        }

        Collection<EffectPlayer> projectileEffects = context.getEffects("spawned");
        for (EffectPlayer effectPlayer : projectileEffects) {
            effectPlayer.start(spawnedEntity.getLocation(), spawnedEntity, null, null);
        }
        context.registerForUndo(spawnedEntity);

        if (track)
        {
            current = new WeakReference<Entity>(spawnedEntity);
        }
        if (setTarget)
        {
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
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);

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
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("track");
        parameters.add("loot");
        parameters.add("baby");
        parameters.add("name");
        parameters.add("type");
        parameters.add("speed");
        parameters.add("reason");
        parameters.add("skeleton_type");
        parameters.add("ocelot_type");
        parameters.add("horse_variant");
        parameters.add("horse_style");
        parameters.add("horse_color");
        parameters.add("color");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("type")) {
            for (EntityType type : EntityType.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("reason")) {
            for (CreatureSpawnEvent.SpawnReason type : CreatureSpawnEvent.SpawnReason.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("skeleton_type")) {
            for (Skeleton.SkeletonType type : Skeleton.SkeletonType.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("ocelot_type")) {
            for (Ocelot.Type type : Ocelot.Type.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("horse_variant")) {
            for (Horse.Variant type : Horse.Variant.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("horse_style")) {
            for (Horse.Style type : Horse.Style.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("horse_color")) {
            for (Horse.Color type : Horse.Color.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("color")) {
            for (DyeColor type : DyeColor.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("track") || parameterKey.equals("loot") || parameterKey.equals("baby")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("name")) {
            examples.add("Philbert");
        } else if (parameterKey.equals("speed")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
