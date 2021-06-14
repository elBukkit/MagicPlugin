package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicMetaKeys;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import com.elmakers.mine.bukkit.utility.WeightedPair;
import com.elmakers.mine.bukkit.utility.metadata.EntityMetadataUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;

public class SpawnEntityAction extends CompoundAction
{
    private Deque<WeightedPair<String>> entityTypeProbability;

    private CreatureSpawnEvent.SpawnReason spawnReason = CreatureSpawnEvent.SpawnReason.EGG;

    private boolean loot = false;
    private boolean setTarget = false;
    private boolean setSource = false;
    private boolean force = false;
    private boolean waitForDeath = true;
    private boolean repeatRandomize = true;
    private boolean tamed = false;
    private boolean setOwner = true;
    private boolean onBlock = true;
    private boolean allowReplacement = true;
    private boolean parameterizeName = true;

    private Vector direction = null;
    private double speed;
    private double dyOffset;

    private EntityData entityData;
    private WeakReference<Entity> entity;
    private boolean spawnActionsRun = false;
    private boolean deathActionsRun = false;
    private boolean hasSpawnActions = false;
    private boolean hasDeathActions = false;
    private boolean hasAnyActions = false;
    private boolean useWandName = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        loot = parameters.getBoolean("loot", false);
        force = parameters.getBoolean("force", false);
        tamed = parameters.getBoolean("tamed", false);
        setOwner = parameters.getBoolean("owned", true);
        setSource = parameters.getBoolean("set_source", false);
        repeatRandomize = parameters.getBoolean("repeat_random", true);
        speed = parameters.getDouble("speed", 0);
        direction = ConfigurationUtils.getVector(parameters, "direction");
        dyOffset = parameters.getDouble("dy_offset", 0);
        onBlock = parameters.getBoolean("on_block", true);
        allowReplacement = parameters.getBoolean("allow_replacement", true);
        useWandName = parameters.getBoolean("use_wand_name", false);
        parameterizeName = parameters.getBoolean("parameterize_name", true);

        String disguiseTarget = parameters.getString("disguise_target");
        if (disguiseTarget != null) {
            Entity targetEntity = disguiseTarget.equals("target") ? context.getTargetEntity() : context.getEntity();
            if (targetEntity != null) {
                ConfigurationSection disguiseConfig = parameters.createSection("disguise");
                disguiseConfig.set("type", targetEntity.getType().name().toLowerCase());
                if (targetEntity instanceof Player) {
                    MageController controller = context.getController();
                    Player targetPlayer = (Player)targetEntity;
                    disguiseConfig.set("name", targetPlayer.getName());
                    disguiseConfig.set("skin", targetPlayer.getName());
                    PlayerInventory inventory = targetPlayer.getInventory();
                    ItemStack helmet = inventory.getHelmet();
                    if (!ItemUtils.isEmpty(helmet)) {
                        disguiseConfig.set("helmet", controller.getItemKey(helmet));
                    }
                    ItemStack chestplate = inventory.getChestplate();
                    if (!ItemUtils.isEmpty(chestplate)) {
                        disguiseConfig.set("chestplate", controller.getItemKey(chestplate));
                    }
                    ItemStack leggings = inventory.getLeggings();
                    if (!ItemUtils.isEmpty(leggings)) {
                        disguiseConfig.set("leggings", controller.getItemKey(leggings));
                    }
                    ItemStack boots = inventory.getBoots();
                    if (!ItemUtils.isEmpty(boots)) {
                        disguiseConfig.set("boots", controller.getItemKey(boots));
                    }
                    ItemStack mainhand = inventory.getItemInMainHand();
                    if (!ItemUtils.isEmpty(mainhand)) {
                        disguiseConfig.set("mainhand", controller.getItemKey(mainhand));
                    }
                    ItemStack offhand = inventory.getItemInOffHand();
                    if (!ItemUtils.isEmpty(offhand)) {
                        disguiseConfig.set("offhand", controller.getItemKey(offhand));
                    }
                }
            }
        }

        if (parameters.contains("type"))
        {
            entityData = context.getController().getMob(parameters);
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

        ActionHandler actions = getHandler("actions");
        hasDeathActions = actions != null && actions.size() > 0;
        ActionHandler handler = getHandler("spawn");
        hasSpawnActions = handler != null && handler.size() > 0;
        hasAnyActions = hasDeathActions || hasSpawnActions;

        // These defaults change depending on the action setup
        // This may be kind of confusing but I think it is handy and will make
        // using this action more intuitive.
        waitForDeath = parameters.getBoolean("wait_for_death", hasDeathActions);
        setTarget = parameters.getBoolean("set_target", hasSpawnActions);
    }

    @Override
    protected void addHandlers(Spell spell, ConfigurationSection parameters) {
        super.addHandlers(spell, parameters);
        addHandler(spell, "spawn");
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        entity = null;
    }

    @Override
    public boolean next(CastContext context) {
        return (hasSpawnActions && !spawnActionsRun) || (hasDeathActions && !deathActionsRun);
    }

    @Override
    public SpellResult step(CastContext context) {
        if (entity == null) {
            SpellResult result = spawn(context);
            // Run the spawn actions right away
            if (!result.isSuccess() || !hasAnyActions) {
                return result;
            }
        }
        Entity spawned = entity.get();
        if (hasSpawnActions && !spawnActionsRun) {
            spawnActionsRun = true;
            return startTargetedActions("spawn", spawned, context);
        }
        boolean isDead = spawned == null || spawned.isDead() || !spawned.isValid();
        if (hasDeathActions && (isDead || !waitForDeath) && !deathActionsRun) {
            deathActionsRun = true;
            return startTargetedActions("actions", spawned, context);
        }

        return isDead || !waitForDeath ? SpellResult.CAST : SpellResult.PENDING;
    }

    private SpellResult startTargetedActions(String actionKey, Entity spawned, CastContext context) {
        if ((setTarget || setSource) && spawned != null) {
            Entity sourceEntity = setSource ? spawned : context.getEntity();
            if (setTarget) {
                createActionContext(context, sourceEntity, sourceEntity.getLocation(), spawned, spawned.getLocation());
            } else {
                createActionContext(context, sourceEntity, sourceEntity.getLocation());
            }
        }
        return startActions(actionKey);
    }

    private SpellResult spawn(CastContext context) {
        Location spawnLocation = context.getTargetLocation();
        if (spawnLocation == null || onBlock) {
            Block targetBlock = context.getTargetBlock();
            targetBlock = targetBlock.getRelative(BlockFace.UP);
            spawnLocation = targetBlock.getLocation();
        }
        Location sourceLocation = context.getLocation();
        spawnLocation.setPitch(sourceLocation.getPitch());
        spawnLocation.setYaw(sourceLocation.getYaw());

        MageController controller = context.getController();
        if (entityTypeProbability != null && !entityTypeProbability.isEmpty())
        {
            if (repeatRandomize || entityData == null)
            {
                String randomType = RandomUtils.weightedRandom(entityTypeProbability);
                try {
                    entityData = controller.getMob(randomType);
                    if (entityData == null) {
                        entityData = new com.elmakers.mine.bukkit.entity.EntityData(controller, EntityType.valueOf(randomType.toUpperCase()));
                    }
                } catch (Throwable ex) {
                    entityData = null;
                }
            }
        }
        if (entityData == null)
        {
            return SpellResult.FAIL;
        }

        if (force) {
            controller.setForceSpawn(true);
        }
        if (!allowReplacement) {
            controller.setDisableSpawnReplacement(true);
        }
        Entity spawnedEntity = null;
        try {
            spawnedEntity = entityData.spawn(spawnLocation, spawnReason);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!allowReplacement) {
            controller.setDisableSpawnReplacement(false);
        }

        // Special check to assign ownership
        if (spawnedEntity instanceof AreaEffectCloud) {
            ((AreaEffectCloud)spawnedEntity).setSource(context.getLivingEntity());
        } else if (spawnedEntity instanceof Projectile) {
            ((Projectile)spawnedEntity).setShooter(context.getLivingEntity());
        }

        if (force) {
            controller.setForceSpawn(false);
        }

        if (spawnedEntity == null) {
            return SpellResult.FAIL;
        }

        if (!loot) {
            EntityMetadataUtils.instance().setBoolean(spawnedEntity, MagicMetaKeys.NO_DROPS, true);
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
            CompatibilityLib.getCompatibilityUtils().setEntityMotion(spawnedEntity, motion);
        }

        Collection<EffectPlayer> entityEffects = context.getEffects("spawned");
        for (EffectPlayer effectPlayer : entityEffects) {
            effectPlayer.start(spawnedEntity.getLocation(), spawnedEntity, null, null);
        }
        context.registerForUndo(spawnedEntity);
        if (setOwner && spawnedEntity instanceof Creature) {
            EntityMetadataUtils.instance().setString(spawnedEntity, MagicMetaKeys.OWNER, context.getMage().getId());
        }
        if (setTarget && !hasAnyActions)
        {
            context.setTargetEntity(spawnedEntity);
        }
        LivingEntity shooter = context.getLivingEntity();
        if (shooter != null) {
            if (spawnedEntity instanceof Projectile) {
                ((Projectile)spawnedEntity).setShooter(shooter);
            } else if (spawnedEntity instanceof AreaEffectCloud) {
                ((AreaEffectCloud)spawnedEntity).setSource(shooter);
            }
        }
        if (tamed && spawnedEntity instanceof Tameable) {
            Tameable tameable = (Tameable)spawnedEntity;
            tameable.setTamed(true);
            Player owner = context.getMage().getPlayer();
            if (owner != null) {
                tameable.setOwner(owner);
            }
        }
        if (useWandName) {
            Wand wand = context.getWand();
            if (wand != null) {
                spawnedEntity.setCustomName(wand.getName());
            }
        }
        if (parameterizeName) {
            String name = spawnedEntity.getCustomName();
            if (name != null) {
                name = context.parameterize(name);
                spawnedEntity.setCustomName(name);
            }
        }

        entity = new WeakReference<>(spawnedEntity);
        return SpellResult.CAST;

    }

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);

        if (parameters.contains("entity_types"))
        {
            entityTypeProbability = new ArrayDeque<>();
            RandomUtils.populateStringProbabilityMap(entityTypeProbability, ConfigurationUtils.getConfigurationSection(parameters, "entity_types"), 0, 0, 0);
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
        parameters.add("loot");
        parameters.add("baby");
        parameters.add("name");
        parameters.add("type");
        parameters.add("speed");
        parameters.add("reason");
        parameters.add("villager_profession");
        parameters.add("skeleton_type");
        parameters.add("ocelot_type");
        parameters.add("rabbit_type");
        parameters.add("horse_variant");
        parameters.add("horse_style");
        parameters.add("horse_color");
        parameters.add("color");
        parameters.add("repeat_random");
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
        } else if (parameterKey.equals("ocelot_type")) {
            for (Ocelot.Type type : Ocelot.Type.values()) {
                examples.add(type.name().toLowerCase());
            }
        } else if (parameterKey.equals("villager_profession")) {
            for (Villager.Profession profession : Villager.Profession.values()) {
                examples.add(profession.name().toLowerCase());
            }
        } else if (parameterKey.equals("rabbit_type")) {
            for (Rabbit.Type type : Rabbit.Type.values()) {
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
        } else if (parameterKey.equals("loot") || parameterKey.equals("baby") || parameterKey.equals("repeat_random")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else if (parameterKey.equals("name")) {
            examples.add("Philbert");
        } else if (parameterKey.equals("speed")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
