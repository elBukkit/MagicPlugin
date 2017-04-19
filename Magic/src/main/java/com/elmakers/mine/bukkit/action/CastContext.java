package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

public class CastContext implements com.elmakers.mine.bukkit.api.action.CastContext {
    protected static Random random;

    private final Location location;
    private final Entity entity;
    private Location targetLocation;
    private Location targetSourceLocation;
    private Location targetCenterLocation;
    private Entity targetEntity;
    private UndoList undoList;
    private String targetName = null;
    private SpellResult result = SpellResult.NO_ACTION;
    private SpellResult initialResult = SpellResult.CAST;
    private Vector direction = null;
    private Boolean targetCaster = null;

    private Set<UUID> targetMessagesSent = new HashSet<>();
    private Collection<EffectPlay> currentEffects = new ArrayList<>();

    private Spell spell;
    private BaseSpell baseSpell;
    private BlockSpell blockSpell;
    private BrushSpell brushSpell;
    private TargetingSpell targetingSpell;
    private UndoableSpell undoSpell;
    private MaterialBrush brush;
    private CastContext base;
    private Mage mage;
    private MageClass mageClass;
    private Wand wand;

    private List<ActionHandlerContext> handlers = null;
    private List<ActionHandlerContext> finishedHandlers = null;

    // Base Context
    private int workAllowed = 500;
    private int actionsPerformed;
    private boolean finished = false;

    public CastContext() {
        this.location = null;
        this.entity = null;
        this.base = this;
        this.result = SpellResult.NO_ACTION;
        targetMessagesSent = new HashSet<>();
        currentEffects = new ArrayList<>();
    }

    public CastContext(Mage mage) {
        this.mage = mage;
        this.entity = mage.getEntity();
        this.location = null;
        this.base = this;
        this.result = SpellResult.NO_ACTION;
        targetMessagesSent = new HashSet<>();
        currentEffects = new ArrayList<>();
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy) {
        this(copy, copy.getEntity(), copy instanceof CastContext ? ((CastContext) copy).location : null);
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy, Entity sourceEntity) {
        this(copy, sourceEntity, null);
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy, Location sourceLocation) {
        this(copy, null, sourceLocation);
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy, Entity sourceEntity, Location sourceLocation) {
        this.location = sourceLocation;
        this.entity = sourceEntity;
        this.setSpell(copy.getSpell());
        this.targetEntity = copy.getTargetEntity();
        this.targetLocation = copy.getTargetLocation();
        this.undoList = copy.getUndoList();
        this.targetName = copy.getTargetName();
        this.brush = copy.getBrush();
        this.targetMessagesSent = copy.getTargetMessagesSent();
        this.currentEffects = copy.getCurrentEffects();
        this.result = copy.getResult();
        this.wand = copy.getWand();
        this.mageClass = copy.getMageClass();

        Location centerLocation = copy.getTargetCenterLocation();
        if (centerLocation != null) {
            targetCenterLocation = centerLocation;
        }

        if (copy instanceof CastContext)
        {
            this.base = ((CastContext)copy).base;
            this.initialResult = ((CastContext)copy).initialResult;
            this.direction = ((CastContext)copy).direction;
        }
        else
        {
            this.base = this;
        }
    }

    public void setSpell(Spell spell)
    {
        this.spell = spell;
        if (spell instanceof BaseSpell)
        {
            this.baseSpell = (BaseSpell)spell;
        }
        if (spell instanceof MageSpell)
        {
            MageSpell mageSpell = (MageSpell)spell;
            this.mage = mageSpell.getMage();
            this.wand = mage.getActiveWand();
            this.mageClass = (this.wand == null ? this.mage.getActiveClass() : this.wand.getMageClass());
        }
        if (spell instanceof UndoableSpell)
        {
            this.undoSpell = (UndoableSpell)spell;
            undoList = this.undoSpell.getUndoList();
        }
        if (spell instanceof TargetingSpell)
        {
            this.targetingSpell = (TargetingSpell)spell;
        }
        if (spell instanceof BlockSpell)
        {
            this.blockSpell = (BlockSpell)spell;
        }
        if (spell instanceof BrushSpell)
        {
            this.brushSpell = (BrushSpell)spell;
        }
    }

    @Override
    public Location getCastLocation() {
        if (location != null) {
            return location;
        }
        Location castLocation = wand == null ? null : wand.getLocation();
        if (castLocation == null) {
            castLocation = this.baseSpell != null ? baseSpell.getCastLocation() : getEyeLocation();
        }
        if (castLocation != null && direction != null) {
            castLocation.setDirection(direction);
        }
        return castLocation;
    }

    @Override
    public Location getWandLocation() {
        return getCastLocation();
    }

    @Override
    public Location getEyeLocation() {
        if (location != null) {
            return location;
        }
        if (entity != null) {
            if (entity instanceof  LivingEntity) {
                return ((LivingEntity) entity).getEyeLocation();
            }
            return entity.getLocation();
        }

        return spell.getEyeLocation();
    }

    @Override
    public Entity getEntity() {
        if (entity != null) {
            return entity;
        }

        return spell.getEntity();
    }

    @Override
    public LivingEntity getLivingEntity() {
        Entity entity = getEntity();
        return entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }

    @Override
    public Location getLocation() {
        if (location != null) {
            return location;
        }
        if (entity != null) {
            return entity.getLocation();
        }
        return spell.getLocation();
    }

    @Override
    public Location getTargetLocation() {
        return targetLocation;
    }

    @Override
    public Location getTargetSourceLocation() {
        return targetSourceLocation == null ? targetLocation : targetSourceLocation;
    }

    @Override
    public Block getTargetBlock() {
        return targetLocation == null ? null : targetLocation.getBlock();
    }

    @Override
    public Entity getTargetEntity() {
        return targetEntity;
    }

    @Override
    public Vector getDirection() {
        if (direction != null) {
            return direction.clone();
        }
        return getLocation().getDirection();
    }
    
    @Override
    public BlockFace getFacingDirection() {
        if (baseSpell != null) {
            return BaseSpell.getFacing(getLocation());
        }
        return BlockFace.UP;
    }

    @Override
    public void setDirection(Vector direction) {
        this.direction = direction;
    }

    @Override
    public World getWorld() {
        Location location = getLocation();
        return location == null ? null : location.getWorld();
    }

    @Override
    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }

    @Override
    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public void setTargetSourceLocation(Location targetLocation) {
        targetSourceLocation = targetLocation;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    @Override
    public Mage getMage() {
        return this.mage;
    }

    @Override
    public @Nullable MageClass getMageClass() {
        return this.mageClass;
    }

    @Override
    public Wand getWand() {
        return wand;
    }

    @Override
    public MageController getController() {
        Mage mage = getMage();
        return mage == null ? null : mage.getController();
    }

    @Override
    public void registerForUndo(Runnable runnable)
    {
        addWork(1);
        if (undoList != null)
        {
            undoList.add(runnable);
        }
    }

    @Override
    public void registerModified(Entity entity)
    {
        addWork(5);
        if (undoList != null)
        {
            undoList.modify(entity);
        }
    }

    @Override
    public void registerDamaged(Entity entity)
    {
        addWork(5);
        if (undoList != null)
        {
            undoList.damage(entity);
        }
    }

    @Override
    public void registerForUndo(Entity entity)
    {
        addWork(5);
        if (undoList != null)
        {
            undoList.add(entity);
        }
    }
    
    @Override
    public void clearAttachables(Block block)
    {
        addWork(50);
        if (undoList != null)
        {
            undoList.clearAttachables(block);
        }
    }

    @Override
    public void registerForUndo(Block block)
    {
        addWork(10);
        if (undoList != null)
        {
            undoList.add(block);
        }
    }

    @Override
    public void updateBlock(Block block)
    {
        MageController controller = getController();
        if (controller != null)
        {
            controller.updateBlock(block);
        }
    }

    @Override
    public void registerVelocity(Entity entity)
    {
        addWork(5);
        if (undoList != null)
        {
            undoList.modifyVelocity(entity);
        }
    }

    @Override
    public void registerMoved(Entity entity)
    {
        addWork(5);
        if (undoList != null)
        {
            undoList.move(entity);
        }
    }

    @Override
    public void registerPotionEffects(Entity entity)
    {
        addWork(5);
        if (undoList != null)
        {
            undoList.addPotionEffects(entity);
        }
    }

    @Override
    public Block getPreviousBlock()
    {
        return targetingSpell != null ? targetingSpell.getPreviousBlock() : null;
    }

    @Override
    public boolean isIndestructible(Block block) {
        return blockSpell != null ? blockSpell.isIndestructible(block) : true;
    }

    @Override
    public boolean hasBuildPermission(Block block) {
        return baseSpell != null ? baseSpell.hasBuildPermission(block) : false;
    }

    @Override
    public boolean hasBreakPermission(Block block) {
        return baseSpell != null ? baseSpell.hasBreakPermission(block) : false;
    }

    @Override
    public boolean hasEffects(String key) {
        return baseSpell != null ? baseSpell.hasEffects(key) : false;
    }

    @Override
    public void playEffects(String key)
    {
        playEffects(key, 1.0f);
    }

    @Override
    public Collection<EffectPlayer> getEffects(String effectKey) {
        Collection<EffectPlayer> effects = spell.getEffects(effectKey);
        if (effects.size() == 0) return effects;

        // Create parameter map
        Map<String, String> parameterMap = null;
        ConfigurationSection workingParameters = spell.getWorkingParameters();
        if (workingParameters != null) {
            Collection<String> keys = workingParameters.getKeys(false);
            parameterMap = new HashMap<>();
            for (String key : keys) {
                parameterMap.put("$" + key, workingParameters.getString(key));
            }
        }

        for (EffectPlayer player : effects)
        {
            // Track effect plays for cancelling
            player.setEffectPlayList(currentEffects);

            // Set material and color
            player.setMaterial(spell.getEffectMaterial());
            player.setColor(getEffectColor());
            player.setParticleOverride(getEffectParticle());

            // Set parameters
            player.setParameterMap(parameterMap);
        }

        return effects;
    }

    public Color getEffectColor() {
        Color color = wand == null ? null : wand.getEffectColor();
        if (color == null) {
            color = spell.getEffectColor();
        }
        return color;
    }

    public String getEffectParticle() {
        String particle = wand == null ? null : wand.getEffectParticleName();
        if (particle == null) {
            particle = spell.getEffectParticle();
        }
        return particle;
    }

    @Override
    public void playEffects(String effectName, float scale)
    {
        playEffects(effectName, scale, null, getEntity(), null, getTargetEntity());
    }

    @Override
    public void playEffects(String effectName, float scale, Block sourceBlock)
    {
        playEffects(effectName, scale, null, getEntity(), null, getTargetEntity(), sourceBlock);
    }

    @Override
    public void playEffects(String effectName, float scale, Location sourceLocation, Entity sourceEntity, Location targetLocation, Entity targetEntity)
    {
        playEffects(effectName, scale, sourceLocation, sourceEntity, targetLocation, targetEntity, null);
    }

    @Override
    public void playEffects(String effectName, float scale, Location sourceLocation, Entity sourceEntity, Location targetLocation, Entity targetEntity, Block sourceBlock)
    {
        if (targetEntity != null) {
            String entityKey = effectName + "_" + targetEntity.getType().name().toLowerCase();
            if (baseSpell != null && baseSpell.hasEffects(entityKey)) {
                effectName = entityKey;
            }
        }
        Collection<EffectPlayer> effects = getEffects(effectName);
        if (effects.size() > 0)
        {
            Location location = getLocation();
            Collection<Entity> targeted = getTargetedEntities();

            for (EffectPlayer player : effects)
            {
                // Set scale
                player.setScale(scale);

                Mage mage = getMage();
                Location source = sourceLocation;
                if (source == null) {
                    if (mage.getEntity() == sourceEntity && player.playsAtOrigin()) {
                        source = player.getSourceLocation(this);
                    } else {
                        source = location;
                    }
                }
                Location target = targetLocation;
                if (target == null) {
                    target = getTargetLocation();
                    if (player.shouldUseBlockLocation() && target != null) {
                        target = target.getBlock().getLocation();
                    } else if (!player.shouldUseHitLocation() && targetEntity != null) {
                        if (targetEntity instanceof LivingEntity) {
                            target = ((LivingEntity)targetEntity).getEyeLocation();
                        } else {
                            target = targetEntity.getLocation();
                        }
                    }
                }
                if (sourceBlock != null) {
                    player.setMaterial(sourceBlock);
                }
                player.start(source, sourceEntity, target, targetEntity, targeted);
            }
        }
    }

    @Override
    public void cancelEffects() {
        for (EffectPlay player : currentEffects) {
            player.cancel();
        }
        currentEffects.clear();
    }

    @Override
    public String getMessage(String key) {
        return getMessage(key, "");
    }

    @Override
    public String getMessage(String key, String def) {
        return baseSpell != null ? baseSpell.getMessage(key, def) : def;
    }

    @Override
    public Location findPlaceToStand(Location target, int verticalSearchDistance, boolean goUp) {
        return baseSpell != null ? baseSpell.findPlaceToStand(target, goUp, verticalSearchDistance) : location;
    }

    @Override
    public Location findPlaceToStand(Location targetLoc, int verticalSearchDistance) {
        return baseSpell != null ? baseSpell.findPlaceToStand(targetLoc, verticalSearchDistance, verticalSearchDistance) : location;
    }

    @Override
    public int getVerticalSearchDistance()  {
        return baseSpell != null ? baseSpell.getVerticalSearchDistance() : 4;
    }

    @Override
    public boolean isOkToStandIn(Material material)
    {
        return baseSpell != null ? baseSpell.isOkToStandIn(material) : true;
    }

    @Override
    public boolean isWater(Material mat)
    {
        return (mat == Material.WATER || mat == Material.STATIONARY_WATER);
    }

    @Override
    public boolean isOkToStandOn(Material material)
    {
        return (material != Material.AIR && material != Material.LAVA && material != Material.STATIONARY_LAVA);
    }

    @Override
    public boolean allowPassThrough(Material material)
    {
        return baseSpell != null ? baseSpell.allowPassThrough(material) : true;
    }

    @Override
    public void castMessageKey(String key)
    {
        if (baseSpell != null)
        {
            baseSpell.castMessage(getMessage(key));
        }
    }

    @Override
    public void sendMessageKey(String key)
    {
        if (baseSpell != null)
        {
            baseSpell.sendMessage(getMessage(key));
        }
    }

    @Override
    public void showMessage(String key, String def)
    {
        Mage mage = getMage();
        if (mage != null)
        {
            mage.sendMessage(getMessage(key, def));
        }
    }

    @Override
    public void showMessage(String message)
    {
        Mage mage = getMage();
        if (mage != null)
        {
            mage.sendMessage(message);
        }
    }

    @Override
    public void castMessage(String message)
    {
        if (baseSpell != null)
        {
            baseSpell.castMessage(message);
        }
    }

    @Override
    public void sendMessage(String message)
    {
        if (baseSpell != null)
        {
            baseSpell.sendMessage(message);
        }
    }

    @Override
    public void setTargetedLocation(Location location)
    {
        if (targetingSpell != null)
        {
            targetingSpell.setTarget(location);
        }
    }

    @Override
    public Block findBlockUnder(Block block)
    {
        if (targetingSpell != null)
        {
            block = targetingSpell.findBlockUnder(block);
        }
        return block;
    }

    @Override
    public Block findSpaceAbove(Block block)
    {
        if (targetingSpell != null)
        {
            block = targetingSpell.findSpaceAbove(block);
        }
        return block;
    }

    @Override
    public boolean isTransparent(Material material)
    {
        if (targetingSpell != null)
        {
            return targetingSpell.isTransparent(material);
        }
        return material.isTransparent();
    }

    @Override
    public boolean isPassthrough(Material material)
    {
        if (baseSpell != null)
        {
            return baseSpell.isPassthrough(material);
        }
        return material.isTransparent();
    }

    @Override
    public boolean isDestructible(Block block)
    {
        if (blockSpell != null)
        {
            return blockSpell.isDestructible(block);
        }
        return true;
    }

    @Override
    public boolean areAnyDestructible(Block block)
    {
        if (blockSpell != null)
        {
            return blockSpell.areAnyDestructible(block);
        }
        return true;
    }

    @Override
    public boolean isTargetable(Block block)
    {
        if (targetingSpell != null)
        {
            return targetingSpell.isTargetable(this, block);
        }
        return true;
    }

    @Override
    public TargetType getTargetType()
    {
        TargetType targetType = TargetType.NONE;

        if (targetingSpell != null)
        {
            targetType = targetingSpell.getTargetType();
        }
        return targetType;
    }

    @Override
    public boolean getTargetsCaster() {
        if (targetCaster != null) {
            return targetCaster;
        }
        if (baseSpell != null) {
            return baseSpell.getTargetsCaster();
        }
        return false;
    }

    @Override
    public void setTargetsCaster(boolean target) {
        targetCaster = target;
    }

    @Override
    public boolean isConsumeFree() {
        if (baseSpell != null) {
            return baseSpell.getConsumeReduction() >= 1;
        }
        return false;
    }

    @Override
    public boolean canTarget(Entity entity) {
        return targetingSpell == null ? true : targetingSpell.canTarget(entity);
    }

    @Override
    public boolean canTarget(Entity entity, Class<?> targetType) {
        return targetingSpell == null ? true : targetingSpell.canTarget(entity, targetType);
    }

    @Override
    public MaterialBrush getBrush() {
        if (brush != null) {
            return brush;
        }
        return brushSpell == null ? null : brushSpell.getBrush();
    }

    @Override
    public void setBrush(MaterialBrush brush) {
        this.brush = brush;
    }

    @Override
    public Collection<Entity> getTargetedEntities()
    {
        if (undoList == null)
        {
            return new ArrayList<>();
        }

        return undoList.getAllEntities();
    }

    @Override
    public void messageTargets(String messageKey)
    {
        Mage mage = getMage();
        if (mage.isStealth()) return;
        Collection<Entity> targets = getTargetedEntities();
        if (targets == null || targets.isEmpty()) return;

        MageController controller = getController();
        LivingEntity sourceEntity = mage.getLivingEntity();
        String playerMessage = getMessage(messageKey);
        if (playerMessage.length() > 0)
        {
            for (Entity target : targets)
            {
                UUID targetUUID = target.getUniqueId();
                if (target instanceof Player && target != sourceEntity && !targetMessagesSent.contains(targetUUID))
                {
                    targetMessagesSent.add(targetUUID);
                    playerMessage = playerMessage.replace("$spell", spell.getName());
                    Mage targetMage = controller.getMage(target);
                    targetMage.sendMessage(playerMessage);
                }
            }
        }
    }

    @Override
    public Block getInteractBlock() {
        Location location = getEyeLocation();
        if (location == null) return null;
        Block playerBlock = location.getBlock();
        if (isTargetable(playerBlock)) return playerBlock;
        Vector direction = location.getDirection().normalize();
        return location.add(direction).getBlock();
    }

    @Override
    public Random getRandom() {
        if (random == null) {
            random = new Random();
        }
        return random;
    }

    @Override
    public UndoList getUndoList() {
        return undoList;
    }

    @Override
    public String getTargetName() {
        return targetName;
    }

    @Override
    public void setTargetName(String name) {
        targetName = name;
    }

    @Override
    public Logger getLogger() {
        return getController().getLogger();
    }

    @Override
    public int getWorkAllowed() {
        return this.base.workAllowed;
    }

    @Override
    public void setWorkAllowed(int work) {
        this.base.workAllowed = work;
    }

    @Override
    public void addWork(int work) {
        this.base.workAllowed -= work;
    }

    @Override
    public void performedActions(int count) {
        this.base.actionsPerformed += count;
    }

    @Override
    public int getActionsPerformed() {
        return base.actionsPerformed;
    }
    
    @Override
    public void finish() {
        if (finished) return;
        finished = true;
        Mage mage = getMage();

        if (finishedHandlers != null) {
            for (ActionHandlerContext context : finishedHandlers) {
                context.finish();
            }
            finishedHandlers = null;
        }
        if (undoSpell != null && undoSpell.isUndoable())
        {
            if (!undoList.isScheduled())
            {
                getController().update(undoList);
            }
            mage.registerForUndo(undoList);
        }
        result = result.max(initialResult);
        if (spell != null) {
            mage.sendDebugMessage(ChatColor.WHITE + "Finish " + ChatColor.GOLD + spell.getName() + ChatColor.WHITE  + ": " + ChatColor.AQUA + result.name().toLowerCase(), 2);
            spell.finish(this);
        }
        String resultName = result.name().toLowerCase();
        castMessageKey(resultName + "_finish");
        playEffects(resultName + "_finish");
    }

    @Override
    public void retarget(double range, double fov, double closeRange, double closeFOV, boolean useHitbox) {
        if (targetingSpell != null)
        {
            targetingSpell.retarget(range, fov, closeRange, closeFOV, useHitbox);
            setTargetEntity(targetingSpell.getTargetEntity());
            setTargetLocation(targetingSpell.getTargetLocation());
        }
    }

    @Override
    public void retarget(double range, double fov, double closeRange, double closeFOV, boolean useHitbox, int yOffset, boolean targetSpaceRequired, int targetMinOffset) {
        if (targetingSpell != null)
        {
            targetingSpell.retarget(range, fov, closeRange, closeFOV, useHitbox, yOffset, targetSpaceRequired, targetMinOffset);
            setTargetEntity(targetingSpell.getTargetEntity());
            setTargetLocation(targetingSpell.getTargetLocation());
        }
    }

    @Override
    public com.elmakers.mine.bukkit.api.action.CastContext getBaseContext()
    {
        return base;
    }

    @Override
    public Location getTargetCenterLocation()
    {
        return targetCenterLocation == null ? targetLocation : targetCenterLocation;
    }

    @Override
    public Set<UUID> getTargetMessagesSent() {
        return targetMessagesSent;
    }

    @Override
    public Collection<EffectPlay> getCurrentEffects() {
        return currentEffects;
    }

    @Override
    public Plugin getPlugin() {
        MageController controller = getController();
        return controller == null ? null : controller.getPlugin();
    }
    @Override
    public boolean teleport(final Entity entity, final Location location, final int verticalSearchDistance, boolean preventFall)
    {
        return teleport(entity, location, verticalSearchDistance, preventFall, true);
    }

    @Override
    public boolean teleport(final Entity entity, final Location location, final int verticalSearchDistance, boolean preventFall, boolean safe)
    {
        Chunk chunk = location.getBlock().getChunk();
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }

        Location targetLocation = findPlaceToStand(location, verticalSearchDistance);
        if (targetLocation == null && !preventFall) {
            Block block = location.getBlock();
            Block blockOneUp = block.getRelative(BlockFace.UP);
            if (!safe || (isOkToStandIn(blockOneUp.getType()) && isOkToStandIn(block.getType())))
            {
                targetLocation = location;
            }
        }
        if (targetLocation != null) {
            targetLocation.setX(location.getX() - location.getBlockX() + targetLocation.getBlockX());
            targetLocation.setZ(location.getZ() - location.getBlockZ() + targetLocation.getBlockZ());
            registerMoved(entity);

            // Hacky double-teleport to work-around vanilla suffocation checks
            boolean isWorldChange = !targetLocation.getWorld().equals(entity.getWorld());
            entity.teleport(targetLocation);
            if (isWorldChange) {
                entity.teleport(targetLocation);
            }
            setTargetLocation(targetLocation);
            castMessageKey("teleport");
            playEffects("teleport");
        } else {
            castMessageKey("teleport_failed");
            playEffects("teleport_failed");
            return false;
        }

        return true;
    }

    @Override
    public boolean teleport(final Entity entity, final Location location, final int verticalSearchDistance)
    {
        return teleport(entity, location, verticalSearchDistance, true);
    }

    @Override
    public void setSpellParameters(ConfigurationSection parameters) {
        if (baseSpell != null) {
            baseSpell.processParameters(parameters);
        }
    }

    @Override
    public Set<Material> getMaterialSet(String key) {
        return getController().getMaterialSet(key);
    }

    @Override
    public SpellResult getResult() {
        return this.result;
    }

    @Override
    public void setResult(SpellResult result) {
        this.result = result;
    }

    @Override
    public void addResult(SpellResult result) {
        if (result != SpellResult.PENDING) {
            this.result = this.result.min(result);
        }
    }

    public void setInitialResult(SpellResult result) {
        initialResult = result;
    }

    @Override
    public boolean canCast(Location location)
    {
        if (baseSpell != null) {
            return baseSpell.canCast(location);
        }
        return true;
    }

    @Override
    public boolean isBreakable(Block block) {
        return com.elmakers.mine.bukkit.block.UndoList.getRegistry().isBreakable(block);
    }

    @Override
    public Double getBreakable(Block block) {
        return com.elmakers.mine.bukkit.block.UndoList.getRegistry().getBreakable(block);
    }

    @Override
    public void clearBreakable(Block block) {
        com.elmakers.mine.bukkit.block.UndoList.getRegistry().unregisterBreakable(block);
    }

    @Override
    public void clearReflective(Block block) {
        com.elmakers.mine.bukkit.block.UndoList.getRegistry().unregisterReflective(block);
    }

    @Override
    public boolean isReflective(Block block) {
        if (block == null) return false;
        if (targetingSpell != null && targetingSpell.isReflective(block.getType())) {
            return true;
        }
        return com.elmakers.mine.bukkit.block.UndoList.getRegistry().isReflective(block);
    }

    @Override
    public Double getReflective(Block block) {
        if (block == null) return null;

        if (targetingSpell != null && targetingSpell.isReflective(block.getType())) {
            return 1.0;
        }

        return com.elmakers.mine.bukkit.block.UndoList.getRegistry().getReflective(block);
    }

    @Override
    public void registerBreakable(Block block, double breakable) {
        com.elmakers.mine.bukkit.block.UndoList.getRegistry().registerBreakable(block, breakable);
        undoList.setUndoBreakable(true);
    }

    @Override
    public void registerReflective(Block block, double reflectivity) {
        com.elmakers.mine.bukkit.block.UndoList.getRegistry().registerReflective(block, reflectivity);
        undoList.setUndoReflective(true);
    }

    @Override
    public double registerBreaking(Block block, double addAmount) {
        double breakAmount = com.elmakers.mine.bukkit.block.UndoList.getRegistry().registerBreaking(block, addAmount);
        undoList.setUndoBreaking(true);
        return breakAmount;
    }

    @Override
    public void unregisterBreaking(Block block) {
        com.elmakers.mine.bukkit.block.UndoList.getRegistry().unregisterBreaking(block);
    }

    @Override
    public String parameterize(String command) {
        Location location = getLocation();
        Mage mage = getMage();
        MageController controller = getController();

        command = command
                .replace("@_", " ")
                .replace("@spell", getSpell().getName())
                .replace("@pd", mage.getDisplayName())
                .replace("@pn", mage.getName())
                .replace("@uuid", mage.getId())
                .replace("@p", mage.getName());

        if (location != null) {
            command = command
                    .replace("@world", location.getWorld().getName())
                    .replace("@x", Double.toString(location.getX()))
                    .replace("@y", Double.toString(location.getY()))
                    .replace("@z", Double.toString(location.getZ()));
        }

        Location targetLocation = getTargetLocation();
        if (targetLocation != null) {
            command = command
                    .replace("@tworld", targetLocation.getWorld().getName())
                    .replace("@tx", Double.toString(targetLocation.getX()))
                    .replace("@ty", Double.toString(targetLocation.getY()))
                    .replace("@tz", Double.toString(targetLocation.getZ()));
        }

        Entity targetEntity = getTargetEntity();
        if (targetEntity != null) {
            if (controller.isMage(targetEntity)) {
                Mage targetMage = controller.getMage(targetEntity);
                command = command
                        .replace("@td", targetMage.getDisplayName())
                        .replace("@tn", targetMage.getName())
                        .replace("@tuuid", targetMage.getId())
                        .replace("@t", targetMage.getName());
            } else {
                command = command
                        .replace("@td", controller.getEntityDisplayName(targetEntity))
                        .replace("@tn", controller.getEntityName(targetEntity))
                        .replace("@tuuid", targetEntity.getUniqueId().toString())
                        .replace("@t", controller.getEntityName(targetEntity));
            }
        }

        return ChatColor.translateAlternateColorCodes('&', command);
    }
    
    @Override
    public void addHandler(com.elmakers.mine.bukkit.api.action.ActionHandler handler) {
        if (base.handlers == null) {
            base.handlers = new ArrayList<>();
        }
        base.handlers.add(new ActionHandlerContext(handler, this));
    }
    
    @Override 
    public boolean hasHandlers() {
        return handlers != null;
    }

    @Override
    public SpellResult processHandlers() {
        SpellResult result = SpellResult.NO_ACTION;
        if (handlers == null) return result;

        if (finishedHandlers == null) {
            finishedHandlers = new ArrayList<>();
        }
        int startingWork = getWorkAllowed();
        int splitWork = Math.max(1, startingWork / handlers.size());
        for (Iterator<ActionHandlerContext> iterator = handlers.iterator(); iterator.hasNext();) {
            ActionHandlerContext handler = iterator.next();
            handler.setWorkAllowed(splitWork);
            SpellResult actionResult = handler.perform();
            if (actionResult != SpellResult.PENDING) {
                result = result.min(actionResult);
                finishedHandlers.add(handler);
                iterator.remove();
            }
        }
        
        if (handlers.isEmpty()) {
            handlers = null;
            return result;
        }

        return SpellResult.PENDING;
    }
}
