package com.elmakers.mine.bukkit.spell;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.MaterialSetManager;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.magic.MaterialSets;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.Target;
import com.elmakers.mine.bukkit.utility.Targeting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nonnull;

public abstract class TargetingSpell extends BaseSpell {
    // This differs from CompatibilityUtils.MAX_ENTITY_RANGE,
    // block targeting can theoretically go farther
    private static final int  MAX_RANGE  = 511;

    private Targeting                           targeting               = new Targeting();

    private Location                            targetLocation          = null;
    protected Location                          targetLocation2         = null;
    private Entity								targetEntity            = null;

    private boolean								targetNPCs				= false;
    private boolean								targetArmorStands		= false;
    private boolean								targetInvisible			= true;
    private boolean								targetVanished			= false;
    private boolean								targetUnknown			= true;
    private String                              targetDisplayName       = null;
    protected Class<?>                          targetEntityType        = null;
    protected Set<EntityType>                   targetEntityTypes       = null;
    protected Set<EntityType>                   ignoreEntityTypes       = null;
    protected Material                          targetContents          = null;
    protected double 		                    targetBreakables	    = 0;
    protected boolean                           instantBlockEffects     = false;
    private double                              range                   = 0;

    private boolean                             checkProtection         = false;
    private int                                 damageResistanceProtection = 0;

    private boolean                             allowMaxRange           = false;

    private @Nonnull MaterialSet                targetThroughMaterials  = MaterialSets.empty();
    private @Nonnull MaterialSet                targetableMaterials     = MaterialSets.empty();
    private @Nonnull MaterialSet                reflectiveMaterials     = MaterialSets.empty();
    private boolean                             reverseTargeting        = false;
    private boolean                             originAtTarget          = false;

    protected void initializeTargeting()
    {
        targeting.reset();
        reverseTargeting = false;
        targetLocation = null;
        targetLocation2 = null;
    }

    @Override
    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);

        // Escape targeting parameters
        String useTargetName = null;
        if (currentCast != null) {
            useTargetName = currentCast.getTargetName();
        }
        if (useTargetName == null) {
            Target target = targeting.getTarget();
            if (target != null) {
                if (target.hasEntity() && getTargetType() != TargetType.BLOCK) {
                    useTargetName = controller.getEntityDisplayName(target.getEntity());
                } else if (target.isValid() && getTargetType() != TargetType.OTHER_ENTITY && getTargetType() != TargetType.ANY_ENTITY) {
                    MaterialAndData material = target.getTargetedMaterial();
                    if (material != null)
                    {
                        useTargetName = material.getName();
                    }
                }
            }
        }
        if (useTargetName == null) {
            message = message.replace("$target", "Nothing");
        } else {
            message = message.replace("$target", useTargetName);
        }

        return message;
    }

    @Deprecated // Material
    public boolean isReflective(Material mat) {
        return reflectiveMaterials.testMaterial(mat);
    }

    public boolean isReflective(Block block) {
        return reflectiveMaterials.testBlock(block);
    }

    public boolean isTargetable(CastContext context, Block block) {
        if (targetBreakables > 0 && context.isBreakable(block)) {
            return true;
        }

        return isTargetable(block);
    }

    public boolean isTargetable(Block block) {
        if (!allowPassThrough(block)) {
            return true;
        }

        boolean targetThrough = targetThroughMaterials.testBlock(block);
        if (reverseTargeting) {
            return targetThrough;
        } else {
            return !targetThrough && targetableMaterials.testBlock(block);
        }
    }

    public void setReverseTargeting(boolean reverse)
    {
        reverseTargeting = reverse;
    }

    public void setTargetSpaceRequired()
    {
        targeting.setTargetSpaceRequired(true);
    }

    public void setTargetMinOffset(int offset) {
        targeting.setTargetMinOffset(offset);
    }

    public void setTarget(Location location) {
        targeting.targetBlock(getEyeLocation(), location == null ? null : location.getBlock());
    }

    public void setTargetingHeight(int offset) {
        targeting.setYOffset(offset);
    }

    public TargetType getTargetType()
    {
        return targeting.getTargetType();
    }

    public Block getPreviousBlock() {
        return targeting.getPreviousBlock();
    }

    public void retarget(double range, double fov, double closeRange, double closeFOV, boolean useHitbox, int yOffset, boolean targetSpaceRequired, int targetMinOffset) {
        initializeTargeting();
        this.range = range;
        targeting.setYOffset(yOffset);
        targeting.setTargetSpaceRequired(targetSpaceRequired);
        targeting.setTargetMinOffset(targetMinOffset);
        targeting.setFOV(fov);
        targeting.setCloseRange(closeFOV);
        targeting.setCloseFOV(closeRange);
        targeting.setUseHitbox(useHitbox);
        target();
    }

    public void retarget(CastContext context, double range, double fov, double closeRange, double closeFOV, boolean useHitbox) {
        initializeTargeting();
        this.range = range;
        targeting.setFOV(fov);
        targeting.setCloseRange(closeFOV);
        targeting.setCloseFOV(closeRange);
        targeting.setUseHitbox(useHitbox);
        target(context);
    }

    public void retarget(double range, double fov, double closeRange, double closeFOV, boolean useHitbox) {
        retarget(currentCast, range, fov, closeRange, closeFOV, useHitbox);
    }

    public void target(CastContext castContext) {
        if (!targeting.hasTarget())
        {
            getTarget(castContext);
        }
    }


    @Override
    public void target()
    {
        target(currentCast);
    }

    protected Target processBlockEffects()
    {
        Target target = targeting.getTarget();
        Target originalTarget = target;
        final Block block = target.getBlock();
        Double backfireAmount = currentCast.getReflective(block);
        if (backfireAmount != null) {
            if (random.nextDouble() < backfireAmount) {
                final Entity mageEntity = mage.getEntity();
                final Location location = getLocation();
                final Location originLocation = block.getLocation();
                Vector direction = location.getDirection();
                originLocation.setDirection(direction.multiply(-1));
                this.location = originLocation;
                backfire();
                final Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> effects = getEffects("cast");
                if (effects.size() > 0) {
                    Bukkit.getScheduler().runTaskLater(controller.getPlugin(),
                        new Runnable() {
                            @Override
                            public void run() {
                                for (com.elmakers.mine.bukkit.api.effect.EffectPlayer player : effects) {
                                    player.setMaterial(getEffectMaterial());
                                    player.setColor(mage.getEffectColor());
                                    player.start(originLocation, null, location, mageEntity);
                                }
                            }
                        }, 5l);
                }
                target = new Target(getEyeLocation(), mageEntity);
            }
        }

        if (targetBreakables > 0 && originalTarget.isValid() && block != null && currentCast.isBreakable(block)) {
            targeting.breakBlock(currentCast, block, targetBreakables);
        }

        return target;
    }

    protected Target findTarget(CastContext context)
    {
        Location source = getEyeLocation();
        TargetType targetType = targeting.getTargetType();
        boolean isBlock = targetType == TargetType.BLOCK || targetType == TargetType.SELECT;
        if (!isBlock && targetEntity != null) {
            return targeting.overrideTarget(context, new Target(source, targetEntity));
        }

        if (targetType != TargetType.SELF && targetLocation != null) {
            return targeting.overrideTarget(context, new Target(source, targetLocation.getBlock()));
        }

        Target target = targeting.target(context, getMaxRange());
        return targeting.getResult() == Targeting.TargetingResult.MISS && !allowMaxRange ? new Target(source) : target;
    }

    protected Target getTarget()
    {
        return getTarget(currentCast);
    }

    protected Target getTarget(CastContext context)
    {
        Target target = findTarget(context);

        if (instantBlockEffects)
        {
            target = processBlockEffects();
        }
        if (originAtTarget && target.isValid()) {
            Location previous = this.location;
            if (previous == null && mage != null) {
                previous = mage.getLocation();
            }
            location = target.getLocation().clone();
            if (previous != null) {
                location.setPitch(previous.getPitch());
                location.setYaw(previous.getYaw());
            }
        }

        Entity targetEntity = target != null ? target.getEntity() : null;
        Location targetLocation = target != null ? target.getLocation() : null;
        context.setTargetLocation(targetLocation);
        context.setTargetEntity(targetEntity);

        return target;
    }

    public Target getCurrentTarget()
    {
        return targeting.getOrCreateTarget(getEyeLocation());
    }

    public Block getTargetBlock()
    {
        return getTarget().getBlock();
    }

    public List<Target> getAllTargetEntities() {
        // This target-clearing is a bit hacky, but this is only used when we want to reset
        // targeting.
        targeting.start(currentCast.getEyeLocation());
        return targeting.getAllTargetEntities(currentCast, this.getMaxRange());
    }

    @Override
    public boolean canTarget(Entity entity) {
        return canTarget(entity, null);
    }

    public boolean canTarget(Entity entity, Class<?> targetType) {
        // This is mainly here to ignore pets...
        if (!targetUnknown && entity.getType() == EntityType.UNKNOWN) {
            return false;
        }
        if (entity.hasMetadata("notarget")) return false;
        if (!targetNPCs && controller.isNPC(entity)) return false;
        if (!targetArmorStands && entity instanceof ArmorStand) return false;
        if (ignoreEntityTypes != null && ignoreEntityTypes.contains(entity.getType())) {
            return false;
        }
        if (targetDisplayName != null && (entity.getCustomName() == null || !entity.getCustomName().equals(targetDisplayName))) {
            return false;
        }

        if (damageResistanceProtection > 0 && entity instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity)entity;
            if (living.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
                Collection<PotionEffect> effects = living.getActivePotionEffects();
                for (PotionEffect effect : effects) {
                    if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE) && effect.getAmplifier() >= damageResistanceProtection) {
                        return false;
                    }
                }
            }
        }
        if (entity instanceof Player)
        {
            Player player = (Player)entity;
            if (checkProtection && player.hasPermission("Magic.protected." + this.getKey())) return false;
            if (controller.isMage(entity) && isSuperProtected(controller.getMage(entity))) return false;
        }
        // Ignore invisible entities
        if (!targetInvisible && entity instanceof LivingEntity && ((LivingEntity)entity).hasPotionEffect(PotionEffectType.INVISIBILITY)) return false;
        if (!targetVanished && entity instanceof Player && controller.isVanished(entity)) return false;
        
        if (targetContents != null && entity instanceof ItemFrame)
        {
            ItemFrame itemFrame = (ItemFrame)entity;
            ItemStack item = itemFrame.getItem();
            if (item == null || item.getType() != targetContents) return false;
        }
        if (targetType != null) {
            return targetType.isAssignableFrom(entity.getClass()) && super.canTarget(entity);
        }
        if (targetEntityType == null && targetEntityTypes == null) return super.canTarget(entity);
        if (targetEntityTypes != null) {
            return targetEntityTypes.contains(entity.getType()) && super.canTarget(entity);
        }
        return targetEntityType.isAssignableFrom(entity.getClass()) && super.canTarget(entity);
    }

    public boolean isSuperProtected(Mage mage) {
        return !bypassProtection && !bypassAll && mage.isSuperProtected();
    }

    protected double getMaxRange()
    {
        if (allowMaxRange) return Math.min(MAX_RANGE, range);
        float multiplier = (mage == null) ? 1 : mage.getRangeMultiplier();
        return Math.min(MAX_RANGE, multiplier * range);
    }

    @Override
    public double getRange()
    {
        TargetType targetType = targeting.getTargetType();
        if (targetType == TargetType.NONE || targetType == TargetType.SELF) return 0;
        return getMaxRange();
    }

    protected double getMaxRangeSquared()
    {
        double maxRange = getMaxRange();
        return maxRange * maxRange;
    }

    protected void setMaxRange(double range)
    {
        this.range = range;
    }

    @Deprecated
    public boolean isTransparent(Material material) {
        return targetThroughMaterials.testMaterial(material);
    }

    public boolean isTransparent(Block block) {
        return targetThroughMaterials.testBlock(block);
    }

    public Block getInteractBlock() {
        Location location = getEyeLocation();
        if (location == null) return null;
        Block playerBlock = location.getBlock();
        if (isTargetable(playerBlock)) return playerBlock;
        Vector direction = location.getDirection().normalize();
        return location.add(direction).getBlock();
    }

    public Block findBlockUnder(Block block)
    {
        int depth = 0;
        if (targetThroughMaterials.testBlock(block))
        {
            while (depth < verticalSearchDistance && targetThroughMaterials.testBlock(block))
            {
                depth++;
                block = block.getRelative(BlockFace.DOWN);
            }
        }
        else
        {
            while (depth < verticalSearchDistance && !targetThroughMaterials.testBlock(block))
            {
                depth++;
                block = block.getRelative(BlockFace.UP);
            }
            block = block.getRelative(BlockFace.DOWN);
        }

        return block;
    }

    public Block findSpaceAbove(Block block)
    {
        int depth = 0;
        while (depth < verticalSearchDistance && !targetThroughMaterials.testBlock(block))
        {
            depth++;
            block = block.getRelative(BlockFace.UP);
        }
        return block;
    }

    @Override
    protected void reset()
    {
        super.reset();
        this.initializeTargeting();
    }

    @Override
    protected void loadTemplate(ConfigurationSection node)
    {
        super.loadTemplate(node);

        // Preload some parameters that may appear in spell lore
        ConfigurationSection parameters = node.getConfigurationSection("parameters");
        if (parameters != null)
        {
            processTemplateParameters(parameters);
        }
    }

    protected void processTemplateParameters(ConfigurationSection parameters) {
        range = parameters.getDouble("range", 0);
        boolean hasTargeting = parameters.contains("target");
        targeting.parseTargetType(parameters.getString("target"));

        // If a range was specified but not a target type, default to none
        if (range > 0 && !hasTargeting) {
            targeting.setTargetType(TargetType.OTHER);
        }
        TargetType targetType = targeting.getTargetType();

        // Use default range of 32 for configs that didn't specify range
        // Only when targeting is set to on
        if ((targetType != TargetType.NONE && targetType != TargetType.SELF) && range == 0) {
            range = 32;
        }

        // Re-process targetSelf parameter, defaults to on if targetType is "self"
        targetSelf = (targetType == TargetType.SELF);
        targetSelf = parameters.getBoolean("target_self", targetSelf);
    }

    @Override
    public void processParameters(ConfigurationSection parameters) {
        super.processParameters(parameters);
        targeting.processParameters(parameters);
        processTemplateParameters(parameters);
        allowMaxRange = parameters.getBoolean("allow_max_range", false);
        checkProtection = parameters.getBoolean("check_protection", false);
        damageResistanceProtection = parameters.getInt("damage_resistance_protection", 0);
        targetBreakables = parameters.getDouble("target_breakables", 1);
        reverseTargeting = parameters.getBoolean("reverse_targeting", false);
        instantBlockEffects = parameters.getBoolean("instant_block_effects", false);

        MaterialSetManager materials = controller.getMaterialSetManager();
        targetThroughMaterials = MaterialSets.empty();
        targetThroughMaterials = materials.getMaterialSet("transparent", targetThroughMaterials);
        targetThroughMaterials = materials.fromConfig(parameters.getString("transparent"), targetThroughMaterials);

        targetableMaterials = MaterialSets.empty();
        targetableMaterials = materials.fromConfig(parameters.getString("targetable"), targetableMaterials);

        reflectiveMaterials = MaterialSets.empty();
        reflectiveMaterials = materials.fromConfig(parameters.getString("reflective"), reflectiveMaterials);

        if (parameters.getBoolean("reflective_override", true)) {
            String reflectiveKey = controller.getReflectiveMaterials(mage, mage.getLocation());
            if (reflectiveKey != null) {
                reflectiveMaterials = MaterialSets.union(
                        materials.fromConfigEmpty(reflectiveKey),
                        reflectiveMaterials);
            }
        }

        targetNPCs = parameters.getBoolean("target_npc", false);
        targetArmorStands = parameters.getBoolean("target_armor_stand", false);
        targetInvisible = parameters.getBoolean("target_invisible", true);
        targetVanished = parameters.getBoolean("target_vanished", false);
        targetUnknown = parameters.getBoolean("target_unknown", true);

        if (parameters.contains("target_type")) {
            String entityTypeName = parameters.getString("target_type");
            try {
                targetEntityType = Class.forName("org.bukkit.entity." + entityTypeName);
            } catch (Throwable ex) {
                controller.getLogger().warning("Unknown entity type in target_type of " + getKey() + ": " + entityTypeName);
                targetEntityType = null;
            }
        } else if (parameters.contains("target_types")) {
            targetEntityType = null;
            targetEntityTypes = new HashSet<>();
            Collection<String> typeKeys = ConfigurationUtils.getStringList(parameters, "target_types");
            for (String typeKey : typeKeys) {
                try {
                    EntityType entityType = EntityType.valueOf(typeKey.toUpperCase());
                    targetEntityTypes.add(entityType);
                } catch (Throwable ex) {
                    controller.getLogger().warning("Unknown entity type in target_types of " + getKey() + ": " + typeKey);
                }
            }
        } else {
            targetEntityType = null;
            targetEntityTypes = null;
        }
        if (parameters.contains("ignore_types")) {
            ignoreEntityTypes = new HashSet<>();
            Collection<String> typeKeys = ConfigurationUtils.getStringList(parameters, "ignore_types");
            for (String typeKey : typeKeys) {
                try {
                    EntityType entityType = EntityType.valueOf(typeKey.toUpperCase());
                    ignoreEntityTypes.add(entityType);
                } catch (Throwable ex) {
                    controller.getLogger().warning("Unknown entity type in ignore_types of " + getKey() + ": " + typeKey);
                }
            }
        } else {
            ignoreEntityTypes = null;
        }

        targetDisplayName = parameters.getString("target_name", null);
        targetContents = ConfigurationUtils.getMaterial(parameters, "target_contents", null);
        originAtTarget = parameters.getBoolean("origin_at_target", false);

        Location defaultLocation = getLocation();
        targetLocation = ConfigurationUtils.overrideLocation(parameters, "t", defaultLocation, controller.canCreateWorlds());

        // For two-click construction spells
        defaultLocation = targetLocation == null ? defaultLocation : targetLocation;
        targetLocation2 = ConfigurationUtils.overrideLocation(parameters, "t2", defaultLocation, controller.canCreateWorlds());

        if (parameters.contains("entity") && mage != null) {
            Entity entity = CompatibilityUtils.getEntity(mage.getEntity().getWorld(), UUID.fromString(parameters.getString("entity")));
            if (entity != null) {
                targetLocation = entity.getLocation();
                targetEntity = entity;
            }
        } else if (parameters.contains("player")) {
            Player player = DeprecatedUtils.getPlayer(parameters.getString("player"));
            if (player != null) {
                targetLocation = player.getLocation();
                targetEntity = player;
            }
        } else {
            targetEntity = null;
        }

        // Special hack that should work well in most casts.
        boolean targetUnderwater = parameters.getBoolean("target_underwater", true);
        if (targetUnderwater && isUnderwater()) {
            targetThroughMaterials = MaterialSets.union(
                    targetThroughMaterials, Material.WATER, Material.STATIONARY_WATER);
        }
    }

    @Override
    protected String getDisplayMaterialName()
    {
        Target target = targeting.getTarget();
        if (target != null && target.isValid()) {
            return MaterialBrush.getMaterialName(target.getBlock());
        }

        return super.getDisplayMaterialName();
    }

    @Override
    protected void onBackfire() {
        targeting.setTargetType(TargetType.SELF);
    }

    @Override
    public Location getTargetLocation() {
        Target target = targeting.getTarget();
        if (target != null && target.isValid()) {
            return target.getLocation();
        }

        return null;
    }

    @Override
    public Entity getTargetEntity() {
        Target target = targeting.getTarget();
        if (target != null && target.isValid()) {
            return target.getEntity();
        }

        return null;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
        Target target = targeting.getTarget();
        if (target != null && target.isValid()) {
            Block block = target.getBlock();
            MaterialAndData targetMaterial = new MaterialAndData(block);
            if (targetMaterial.getMaterial() == Material.AIR) {
                targetMaterial.setMaterial(DEFAULT_EFFECT_MATERIAL);
            }
            return targetMaterial;
        }
        return super.getEffectMaterial();
    }

    public Class<?> getTargetEntityType() {
        return targetEntityType;
    }
}
