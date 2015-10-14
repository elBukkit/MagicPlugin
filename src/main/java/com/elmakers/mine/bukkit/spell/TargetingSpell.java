package com.elmakers.mine.bukkit.spell;

import java.util.*;

import com.elmakers.mine.bukkit.api.action.SpellAction;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.block.MaterialBrush;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;

public abstract class TargetingSpell extends BaseSpell {
    // This differs from CompatibilityUtils.MAX_ENTITY_RANGE,
    // block targeting can theoretically go farther
    private static final int  MAX_RANGE  = 511;

    private Target								target					= null;
    private List<Target>                        targets                 = null;
    private TargetType							targetType				= TargetType.OTHER;
    private boolean								targetNPCs				= false;
    private boolean								targetArmorStands		= false;
    private boolean								targetInvisible			= true;
    private boolean								targetUnknown			= true;
    private boolean                             targetingComplete		= false;
    private boolean                             targetSpaceRequired     = false;
    private int                                 targetMinOffset         = 0;
    protected Class<? extends Entity>           targetEntityType        = null;
    protected Set<EntityType>                   targetEntityTypes       = null;
    protected Material                          targetContents          = null;
    private Location                            targetLocation;
    private Vector								targetLocationOffset;
    private Vector								targetDirectionOverride;
    private String								targetLocationWorldName;
    protected Location                          targetLocation2;
    protected double 		                    targetBreakables	    = 0;
    private Entity								targetEntity            = null;

    protected float                             distanceWeight          = 1;
    protected float                             fovWeight               = 4;
    protected int                               npcWeight               = -1;
    protected int                               mageWeight              = 5;
    protected int                               playerWeight            = 4;
    protected int                               livingEntityWeight      = 3;

    private boolean                             bypassProtection        = false;
    private boolean                             checkProtection         = false;

    private boolean                             allowMaxRange           = false;
    private boolean                             bypassBackfire          = false;

    private boolean                             useHitbox               = false;
    private int                                 range                   = 32;
    private double                              fov                     = 0.3;
    private double                              closeRange              = 1;
    private double                              closeFOV                = 0.5;

    private Set<Material>                       targetThroughMaterials  = new HashSet<Material>();
    private Set<Material>                       targetableMaterials     = null;
    private boolean                             reverseTargeting        = false;
    private boolean                             originAtTarget          = false;
    private boolean                             ignoreBlocks            = false;

    private BlockIterator						blockIterator = null;
    private	Block								currentBlock = null;
    private	Block								previousBlock = null;
    private	Block								previousPreviousBlock = null;

    protected void initializeTargeting()
    {
        clearTarget();
        blockIterator = null;
        currentBlock = null;
        previousBlock = null;
        previousPreviousBlock = null;
        targetSpaceRequired = false;
        reverseTargeting = false;
        targetingComplete = false;
        targetMinOffset = 0;
    }

    public void setTargetType(TargetType t) {
        this.targetType = t;
        if (target != null) {
            target = null;
            initializeTargeting();
        }
    }

    public String getMessage(String messageKey, String def) {
        String message = super.getMessage(messageKey, def);

        // Escape targeting parameters
        String useTargetName = null;
        if (currentCast != null) {
            useTargetName = currentCast.getTargetName();
        }
        if (useTargetName == null) {
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
            message = message.replace("$target", "?");
        } else {
            message = message.replace("$target", useTargetName);
        }

        return message;
    }

    public void clearTargetThrough()
    {
        targetThroughMaterials.clear();
    }

    public void targetThrough(Material mat)
    {
        targetThroughMaterials.add(mat);
    }

    public void targetThrough(Set<Material> mat)
    {
        targetThroughMaterials.clear();
        targetThroughMaterials.addAll(mat);
    }

    public void noTargetThrough(Material mat)
    {
        targetThroughMaterials.remove(mat);
    }

    public boolean isTargetable(Material mat)
    {
        if (!allowPassThrough(mat)) {
            return true;
        }
        boolean targetThrough = targetThroughMaterials.contains(mat);
        if (reverseTargeting)
        {
            return(targetThrough);
        }
        if (!targetThrough && targetableMaterials != null)
        {
            return targetableMaterials.contains(mat);
        }
        return !targetThrough;
    }

    public void setReverseTargeting(boolean reverse)
    {
        reverseTargeting = reverse;
    }

    public boolean isReverseTargeting()
    {
        return reverseTargeting;
    }

    public boolean isOriginAtTarget() { return originAtTarget; }

    public void setTargetSpaceRequired()
    {
        targetSpaceRequired = true;
    }

    public void setTargetMinOffset(int offset) {
        targetMinOffset = offset;
    }

    public void setTarget(Location location) {
        target = new Target(getEyeLocation(), location == null ? null : location.getBlock());
    }

    public void offsetTarget(int dx, int dy, int dz) {
        Location location = getLocation();
        if (location == null) {
            return;
        }
        location.add(dx, dy, dz);
        initializeBlockIterator(location);
    }

    protected boolean initializeBlockIterator(Location location) {
        if (blockIterator != null) {
            return true;
        }
        if (location.getBlockY() < 0) {
            location = location.clone();
            location.setY(0);
        }
        int maxHeight = CompatibilityUtils.getMaxHeight(location.getWorld());
        if (location.getBlockY() > maxHeight) {
            location = location.clone();
            location.setY(maxHeight);
        }

        try {
            blockIterator = new BlockIterator(location, VIEW_HEIGHT, getMaxRange());
        } catch (Exception ex) {
            // This seems to happen randomly, like when you use the same target.
            // Very annoying, and I now kind of regret switching to BlockIterator.
            // At any rate, we're going to just re-use the last target block and
            // cross our fingers!
            return false;
        }

        return true;
    }

    /**
     * Move "steps" forward along line of vision and returns the block there
     *
     * @return The block at the new location
     */
    protected Block getNextBlock()
    {
        previousPreviousBlock = previousBlock;
        previousBlock = currentBlock;
        if (blockIterator == null || !blockIterator.hasNext()) {
            currentBlock = null;
        } else {
            currentBlock = blockIterator.next();
        }
        return currentBlock;
    }

    /**
     * Returns the current block along the line of vision
     *
     * @return The block
     */
    public Block getCurBlock()
    {
        return currentBlock;
    }

    /**
     * Returns the previous block along the line of vision
     *
     * @return The block
     */
    public Block getPreviousBlock()
    {
        return previousBlock;
    }

    public TargetType getTargetType()
    {
        return targetType;
    }

    public void retarget(int range, double fov, double closeRange, double closeFOV, boolean useHitbox, Vector offset, boolean targetSpaceRequired, int targetMinOffset) {
        initializeTargeting();
        if (offset != null)
        {
            offsetTarget(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
        }
        this.targetSpaceRequired = targetSpaceRequired;
        this.targetMinOffset = targetMinOffset;
        this.range = range;
        this.fov = fov;
        this.closeRange = closeRange;
        this.closeFOV = closeFOV;
        this.useHitbox = useHitbox;
        target();
    }

    public void retarget(int range, double fov, double closeRange, double closeFOV, boolean useHitbox)
    {
        retarget(range, fov, closeRange, closeFOV, useHitbox, null, targetSpaceRequired, targetMinOffset);
    }

    @Override
    public void target()
    {
        if (target == null)
        {
            getTarget();
        }
    }

    protected Target getTarget()
    {
        target = findTarget();

        final Block block = target.getBlock();
        if (block != null && !bypassBackfire && block.hasMetadata("backfire")) {
            List<MetadataValue> metadata = block.getMetadata("backfire");
            for (MetadataValue value : metadata) {
                if (value.getOwningPlugin().equals(controller.getPlugin())) {
                    if (random.nextDouble() < value.asDouble()) {
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
            }
        }

        if (targetBreakables > 0 && target.isValid()) {
            if (block != null && block.hasMetadata("breakable")) {
                int breakable = (int)(targetBreakables > 1 ? targetBreakables :
                        (random.nextDouble() < targetBreakables ? 1 : 0));
                if (breakable > 0) {
                    List<MetadataValue> metadata = block.getMetadata("breakable");
                    for (MetadataValue value : metadata) {
                        if (value.getOwningPlugin().equals(controller.getPlugin())) {
                            breakBlock(block, value.asInt() + breakable - 1);
                            break;
                        }
                    }
                }
            }
        }

        if (targetLocationOffset != null) {
            target.add(targetLocationOffset);
        }
        if (targetDirectionOverride != null) {
            target.setDirection(targetDirectionOverride);
        }
        if (targetLocationWorldName != null && targetLocationWorldName.length() > 0) {
            Location location = target.getLocation();
            if (location != null) {
                World targetWorld = location.getWorld();
                target.setWorld(ConfigurationUtils.overrideWorld(targetLocationWorldName, targetWorld, controller.canCreateWorlds()));
            }
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

        if (currentCast != null)
        {
            Entity targetEntity = target != null ? target.getEntity() : null;
            Location targetLocation = target != null ? target.getLocation() : null;
            currentCast.setTargetLocation(targetLocation);
            currentCast.setTargetEntity(targetEntity);
        }

        return target;
    }

    /**
     * Returns the block at the cursor, or null if out of range
     *
     * @return The target block
     */
    public Target findTarget()
    {
        final Location location = getEyeLocation();
        if (targetType == TargetType.NONE) {
            return new Target(location);
        }
        if (targetType != TargetType.BLOCK && targetEntity != null) {
            return new Target(location, targetEntity);
        }

        final Entity mageEntity = mage.getEntity();
        if (targetType == TargetType.SELF && mageEntity != null) {
            return new Target(location, mageEntity);
        }

        CommandSender sender = mage.getCommandSender();
        if (targetType == TargetType.SELF && mageEntity == null && sender != null && (sender instanceof BlockCommandSender)) {
            BlockCommandSender commandBlock = (BlockCommandSender)mage.getCommandSender();
            return new Target(commandBlock.getBlock().getLocation(), commandBlock.getBlock());
        }

        if (targetType == TargetType.SELF && location != null) {
            return new Target(location, location.getBlock());
        }

        if (targetType == TargetType.SELF) {
            return new Target(location);
        }

        if (targetLocation != null) {
            return new Target(location, targetLocation.getBlock());
        }

        Block block = null;
        if (!ignoreBlocks) {
            findTargetBlock();
            block = getCurBlock();
        }

        if (targetType == TargetType.BLOCK) {
            return new Target(location, block);
        }

        Target targetBlock = block == null ? null : new Target(location, block);
        Target entityTarget = getEntityTarget();

        // Don't allow targeting entities in an area you couldn't cast the spell in
        if (entityTarget != null && !canCast(entityTarget.getLocation())) {
            entityTarget = null;
        }
        if (targetBlock != null && !canCast(targetBlock.getLocation())) {
            targetBlock = null;
        }

        if (targetType == TargetType.OTHER_ENTITY && entityTarget == null) {
            return new Target(location);
        }

        if (targetType == TargetType.ANY_ENTITY && entityTarget == null) {
            return new Target(location, mageEntity);
        }

        if (entityTarget == null && targetType == TargetType.ANY && mageEntity != null) {
            return new Target(location, mageEntity, targetBlock == null ? null : targetBlock.getBlock());
        }

        if (targetBlock != null && entityTarget != null) {
            if (targetBlock.getDistanceSquared() < entityTarget.getDistanceSquared()) {
                entityTarget = null;
            } else {
                targetBlock = null;
            }
        }

        if (entityTarget != null) {
            return entityTarget;
        } else if (targetBlock != null) {
            return targetBlock;
        }

        return new Target(location);
    }

    public Target getCurrentTarget()
    {
        if (target == null) {
            target = new Target(getEyeLocation());
        }
        return target;
    }

    public void clearTarget()
    {
        target = null;
        targets = null;
        targetLocation = null;
    }

    public Block getTargetBlock()
    {
        return getTarget().getBlock();
    }

    protected Target getEntityTarget()
    {
        List<Target> scored = getAllTargetEntities();
        if (scored.size() <= 0) return null;
        return scored.get(0);
    }

    public List<Target> getAllTargetEntities() {
        return getAllTargetEntities(this.getMaxRange());
    }

    protected List<Target> getAllTargetEntities(double range) {

        return getAllTargetEntities(getEyeLocation(), mage.getEntity(), range, fov, closeRange, closeFOV, useHitbox);
    }

    public List<Target> getAllTargetEntities(Location sourceLocation, Entity sourceEntity, double range, double fov, double closeRange, double closeFOV, boolean useHitbox) {
        if (targets != null) {
            return targets;
        }
        targets = new ArrayList<Target>();

        if (currentBlock != null && sourceLocation != null && sourceLocation.getWorld().equals(currentBlock.getWorld()))
        {
            range = Math.min(range, sourceLocation.distance(currentBlock.getLocation()));
        }

        int rangeSquared = (int)Math.floor(range * range);
        List<Entity> entities = null;
        range = Math.min(range, CompatibilityUtils.MAX_ENTITY_RANGE);
        if (sourceLocation == null && sourceEntity != null) {
            entities = sourceEntity.getNearbyEntities(range, range, range);
            sourceLocation = sourceEntity.getLocation();
        } else if (sourceLocation != null) {
            entities = CompatibilityUtils.getNearbyEntities(sourceLocation, range, range, range);
        }

        if (entities == null) return targets;
        for (Entity entity : entities)
        {
            if (entity == mage.getEntity()) continue;
            if (!targetUnknown && entity.getType() == EntityType.UNKNOWN) continue;
            if (!targetNPCs && controller.isNPC(entity)) continue;
            if (!targetArmorStands && entity instanceof ArmorStand) continue;
            if (entity.hasMetadata("notarget")) continue;
            Location entityLocation = entity.getLocation();
            if (!entityLocation.getWorld().equals(sourceLocation.getWorld())) continue;
            if (entityLocation.distanceSquared(sourceLocation) > rangeSquared) continue;

            // Special check for Elementals
            if (!controller.isElemental(entity) && !canTarget(entity)) continue;

            // check for Superprotected Mages
            if (isSuperProtected(entity)) continue;

            // Ignore invisible entities
            if (!targetInvisible && entity instanceof LivingEntity && ((LivingEntity)entity).hasPotionEffect(PotionEffectType.INVISIBILITY)) continue;

            Target newScore = null;
            if (useHitbox) {
                newScore = new Target(sourceLocation, entity, (int)range, useHitbox);
            } else {
                newScore = new Target(sourceLocation, entity, (int)range, fov, closeRange, closeFOV,
                    distanceWeight, fovWeight, mageWeight, npcWeight, playerWeight, livingEntityWeight);
            }
            if (newScore.getScore() > 0)
            {
                if (mage != null && mage.getDebugLevel() > 1)
                {
                    mage.sendDebugMessage("Target " + entity.getType() + ": " + newScore.getScore());
                }

                targets.add(newScore);
            }
        }

        Collections.sort(targets);
        return targets;
    }

    @Override
    public boolean canTarget(Entity entity) {
        // This is mainly here to ignore pets...
        if (!targetUnknown && entity.getType() == EntityType.UNKNOWN) {
            return false;
        }
        if (!targetNPCs && controller.isNPC(entity)) return false;
        if (!targetArmorStands && entity instanceof ArmorStand) return false;
        if (entity instanceof Player)
        {
            Player player = (Player)entity;
            if (checkProtection && player.hasPermission("Magic.protected." + this.getKey())) return false;
            if (controller.isMage(entity) && controller.getMage(entity).isSuperProtected()) return false;
        }

        if (targetContents != null && entity instanceof ItemFrame)
        {
            ItemFrame itemFrame = (ItemFrame)entity;
            ItemStack item = itemFrame.getItem();
            if (item == null || item.getType() != targetContents) return false;
        }
        if (targetEntityType == null && targetEntityTypes == null) return super.canTarget(entity);
        if (targetEntityTypes != null) {
            return targetEntityTypes.contains(entity.getType()) && super.canTarget(entity);
        }
        return targetEntityType.isAssignableFrom(entity.getClass()) && super.canTarget(entity);
    }

    public boolean isSuperProtected(Mage mage) {
        return !bypassProtection && mage.isSuperProtected();
    }

    protected boolean isSuperProtected(Entity entity) {
        if (bypassProtection || !controller.isMage(entity)) {
            return false;
        }

        Mage targetMage = controller.getMage(entity);
        return isSuperProtected(targetMage);
    }

    protected int getMaxRange()
    {
        if (allowMaxRange) return Math.min(MAX_RANGE, range);
        float multiplier = (mage == null) ? 1 : mage.getRangeMultiplier();
        return Math.min(MAX_RANGE, (int)(multiplier * range));
    }

    @Override
    public int getRange()
    {
        if (targetType == TargetType.NONE || targetType == TargetType.SELF) return 0;
        return getMaxRange();
    }

    protected int getMaxRangeSquared()
    {
        int maxRange = getMaxRange();
        return maxRange * maxRange;
    }

    protected void setMaxRange(int range, boolean allow)
    {
        this.range = range;
        this.allowMaxRange = allow;
    }

    protected void setMaxRange(int range)
    {
        this.range = range;
    }

    public boolean isTransparent(Material material)
    {
        return targetThroughMaterials.contains(material);
    }

    protected void findTargetBlock()
    {
        Location location = getLocation();
        if (location == null)
        {
            return;
        }
        if (targetingComplete)
        {
            return;
        }
        if (!initializeBlockIterator(location))
        {
            return;
        }
        currentBlock = null;
        previousBlock = null;
        previousPreviousBlock = null;

        Block block = getNextBlock();
        while (block != null)
        {
            if (targetMinOffset <= 0) {
                if (targetSpaceRequired) {
                    if (!allowPassThrough(block.getType())) {
                        break;
                    }
                    if (isOkToStandIn(block.getType()) && isOkToStandIn(block.getRelative(BlockFace.UP).getType())) {
                        break;
                    }
                } else {
                    if (!bypassProtection && block.hasMetadata("breakable")) {
                        break;
                    }
                    if (isTargetable(block.getType())) {
                        break;
                    }
                }
            } else {
                targetMinOffset--;
            }
            block = getNextBlock();
        }
        if (block == null && allowMaxRange) {
            currentBlock = previousBlock;
            previousBlock = previousPreviousBlock;
        }
        targetingComplete = true;
    }

    public Block getInteractBlock() {
        Location location = getEyeLocation();
        if (location == null) return null;
        Block playerBlock = location.getBlock();
        if (isTargetable(playerBlock.getType())) return playerBlock;
        Vector direction = location.getDirection().normalize();
        return location.add(direction).getBlock();
    }

    public Block findBlockUnder(Block block)
    {
        int depth = 0;
        if (targetThroughMaterials.contains(block.getType()))
        {
            while (depth < verticalSearchDistance && targetThroughMaterials.contains(block.getType()))
            {
                depth++;
                block = block.getRelative(BlockFace.DOWN);
            }
        }
        else
        {
            while (depth < verticalSearchDistance && !targetThroughMaterials.contains(block.getType()))
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
        while (depth < verticalSearchDistance && !targetThroughMaterials.contains(block.getType()))
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

    protected void breakBlock(Block block, int recursion) {
        if (!block.hasMetadata("breakable")) return;

        Location blockLocation = block.getLocation();
        Location effectLocation = blockLocation.add(0.5, 0.5, 0.5);
        effectLocation.getWorld().playEffect(effectLocation, Effect.STEP_SOUND, block.getType().getId());
        UndoList undoList = com.elmakers.mine.bukkit.block.UndoList.getUndoList(blockLocation);
        if (undoList != null) {
            undoList.add(block);
        }
        block.removeMetadata("breakable", mage.getController().getPlugin());
        block.removeMetadata("backfire", mage.getController().getPlugin());
        block.setType(Material.AIR);

        if (--recursion > 0) {
            breakBlock(block.getRelative(BlockFace.UP), recursion);
            breakBlock(block.getRelative(BlockFace.DOWN), recursion);
            breakBlock(block.getRelative(BlockFace.EAST), recursion);
            breakBlock(block.getRelative(BlockFace.WEST), recursion);
            breakBlock(block.getRelative(BlockFace.NORTH), recursion);
            breakBlock(block.getRelative(BlockFace.SOUTH), recursion);
        }
    }


    @SuppressWarnings("unchecked")
    protected void loadTemplate(ConfigurationSection node)
    {
        super.loadTemplate(node);

        // Preload some parameters that may appear in spell lore
        ConfigurationSection parameters = node.getConfigurationSection("parameters");
        if (parameters != null)
        {
            range = parameters.getInt("range", 32);
            if (parameters.contains("target")) {
                String targetTypeName = parameters.getString("target");
                try {
                    targetType = TargetType.valueOf(targetTypeName.toUpperCase());
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid target_type: " + targetTypeName);
                    targetType = TargetType.OTHER;
                }
            } else {
                targetType = TargetType.OTHER;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processParameters(ConfigurationSection parameters) {
        super.processParameters(parameters);
        useHitbox = parameters.getBoolean("hitbox", false);
        range = parameters.getInt("range", 32);
        fov = parameters.getDouble("fov", 0.3);
        closeRange = parameters.getDouble("close_range", 1);
        closeFOV = parameters.getDouble("close_fov", 0.5);
        allowMaxRange = parameters.getBoolean("allow_max_range", false);
        bypassBackfire = parameters.getBoolean("bypass_backfire", false);
        bypassProtection = parameters.getBoolean("bypass_protection", false);
        bypassProtection = parameters.getBoolean("bp", bypassProtection);
        checkProtection = parameters.getBoolean("check_protection", false);
        targetBreakables = parameters.getDouble("target_breakables", 0);
        reverseTargeting = parameters.getBoolean("reverse_targeting", false);

        distanceWeight = (float)parameters.getDouble("distance_weight", 1);
        fovWeight = (float)parameters.getDouble("fov_weight", 4);
        npcWeight = parameters.getInt("npc_weight", -1);
        playerWeight = parameters.getInt("player_weight", 4);
        livingEntityWeight = parameters.getInt("entity_weight", 3);

        if (parameters.contains("transparent")) {
            targetThroughMaterials.clear();
            targetThroughMaterials.addAll(controller.getMaterialSet(parameters.getString("transparent")));
        } else {
            targetThroughMaterials.clear();
            targetThroughMaterials.addAll(controller.getMaterialSet("transparent"));
        }

        if (parameters.contains("targetable")) {
            targetableMaterials = new HashSet<Material>();
            targetableMaterials.addAll(controller.getMaterialSet(parameters.getString("targetable")));
        } else {
            targetableMaterials = null;
        }

        targetMinOffset = parameters.getInt("target_min_offset", 0);
        targetMinOffset = parameters.getInt("tmo", targetMinOffset);

        if (parameters.contains("target")) {
            String targetTypeName = parameters.getString("target");
            try {
                 targetType = TargetType.valueOf(targetTypeName.toUpperCase());
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid target_type: " + targetTypeName);
                targetType = TargetType.OTHER;
            }
        } else {
            targetType = TargetType.OTHER;
        }

        targetNPCs = parameters.getBoolean("target_npc", false);
        targetArmorStands = parameters.getBoolean("target_armor_stand", false);
        targetInvisible = parameters.getBoolean("target_invisible", true);
        targetUnknown = parameters.getBoolean("target_unknown", true);

        if (parameters.contains("target_type")) {
            String entityTypeName = parameters.getString("target_type");
            try {
                 Class<?> typeClass = Class.forName("org.bukkit.entity." + entityTypeName);
                 if (Entity.class.isAssignableFrom(typeClass)) {
                     targetEntityType = (Class<? extends Entity>)typeClass;
                 } else {
                     controller.getLogger().warning("Entity type: " + entityTypeName + " not assignable to Entity");
                 }
            } catch (Throwable ex) {
                controller.getLogger().warning("Unknown entity type in target_type of " + getKey() + ": " + entityTypeName);
                targetEntityType = null;
            }
        } else if (parameters.contains("target_types")) {
            targetEntityType = null;
            targetEntityTypes = new HashSet<EntityType>();
            Collection<String> typeKeys = ConfigurationUtils.getStringList(parameters, "target_types");
            for (String typeKey : typeKeys) {
                try {
                    EntityType entityType = EntityType.valueOf(typeKey.toUpperCase());
                    if (entityType == null) {
                        throw new Exception("Bad entity type");
                    }
                    targetEntityTypes.add(entityType);
                } catch (Throwable ex) {
                    controller.getLogger().warning("Unknown entity type in target_types of " + getKey() + ": " + typeKey);
                }
            }
        } else {
            targetEntityType = null;
            targetEntityTypes = null;
        }

        targetContents = ConfigurationUtils.getMaterial(parameters, "target_contents", null);
        originAtTarget = parameters.getBoolean("origin_at_target", false);
        ignoreBlocks = parameters.getBoolean("ignore_blocks", false);

        Location defaultLocation = getLocation();
        targetLocation = ConfigurationUtils.overrideLocation(parameters, "t", defaultLocation, controller.canCreateWorlds());
        targetLocationOffset = null;
        targetDirectionOverride = null;

        Double otxValue = ConfigurationUtils.getDouble(parameters, "otx", null);
        Double otyValue = ConfigurationUtils.getDouble(parameters, "oty", null);
        Double otzValue = ConfigurationUtils.getDouble(parameters, "otz", null);
        if (otxValue != null || otzValue != null || otyValue != null) {
            targetLocationOffset = new Vector(
                    (otxValue == null ? 0 : otxValue),
                    (otyValue == null ? 0 : otyValue),
                    (otzValue == null ? 0 : otzValue));
        }
        targetLocationWorldName = parameters.getString("otworld");

        Double tdxValue = ConfigurationUtils.getDouble(parameters, "otdx", null);
        Double tdyValue = ConfigurationUtils.getDouble(parameters, "otdy", null);
        Double tdzValue = ConfigurationUtils.getDouble(parameters, "otdz", null);
        if (tdxValue != null || tdzValue != null || tdyValue != null) {
            targetDirectionOverride = new Vector(
                    (tdxValue == null ? 0 : tdxValue),
                    (tdyValue == null ? 0 : tdyValue),
                    (tdzValue == null ? 0 : tdzValue));
        }

        // For two-click construction spells
        defaultLocation = targetLocation == null ? defaultLocation : targetLocation;
        targetLocation2 = ConfigurationUtils.overrideLocation(parameters, "t2", defaultLocation, controller.canCreateWorlds());

        if (parameters.contains("player")) {
            Player player = controller.getPlugin().getServer().getPlayer(parameters.getString("player"));
            if (player != null) {
                targetLocation = player.getLocation();
                targetEntity = player;
            }
        } else {
            targetEntity = null;
        }

        // Special hack that should work well in most casts.
        if (isUnderwater()) {
            targetThroughMaterials.add(Material.WATER);
            targetThroughMaterials.add(Material.STATIONARY_WATER);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected String getDisplayMaterialName()
    {
        if (target != null && target.isValid()) {
            return MaterialBrush.getMaterialName(target.getBlock());
        }

        return super.getDisplayMaterialName();
    }

    @Override
    protected void onBackfire() {
        targetType = TargetType.SELF;
    }

    @Override
    public Location getTargetLocation() {
        if (target != null && target.isValid()) {
            return target.getLocation();
        }

        return null;
    }

    @Override
    public Entity getTargetEntity() {
        if (target != null && target.isValid()) {
            return target.getEntity();
        }

        return null;
    }

    @Override
    public com.elmakers.mine.bukkit.api.block.MaterialAndData getEffectMaterial()
    {
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

    public Class<? extends Entity> getTargetEntityType() {
        return targetEntityType;
    }
}
