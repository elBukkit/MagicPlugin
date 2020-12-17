package com.elmakers.mine.bukkit.action;

import static com.google.common.base.Verify.verifyNotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.ActionHandler;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MageModifier;
import com.elmakers.mine.bukkit.api.magic.VariableScope;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.effect.WandEffectContext;
import com.elmakers.mine.bukkit.materials.MaterialSets;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.base.Preconditions;

public class CastContext extends WandEffectContext implements com.elmakers.mine.bukkit.api.action.CastContext {
    protected static Random random;

    private final WeakReference<Entity> entity;
    private @Nullable WeakReference<Entity> targetEntity;
    private @Nullable Location targetLocation;
    private @Nullable Location targetSourceLocation;
    private @Nullable Location targetCenterLocation;
    private @Nullable Block targetBlock;
    private Block previousBlock;
    private UndoList undoList;
    private String targetName = null;
    private SpellResult result = SpellResult.NO_ACTION;
    private SpellResult initialResult = SpellResult.CAST;
    private SpellResult alternateResult = SpellResult.CAST;
    private Vector direction = null;
    private Boolean targetCaster = null;
    private long startTime;
    private ConfigurationSection workingParameters;
    private Map<String, Object> castData;

    private Set<UUID> targetMessagesSent = null;

    private @Nonnull Spell spell;
    private BaseSpell baseSpell;
    private BlockSpell blockSpell;
    private BrushSpell brushSpell;
    private TargetingSpell targetingSpell;
    private UndoableSpell undoSpell;
    private MaterialBrush brush;
    private CastContext base;
    private MageClass mageClass;
    private ConfigurationSection variables;

    private List<ActionHandlerContext> handlers = null;
    private List<ActionHandlerContext> finishedHandlers = null;
    private Map<String, String> messageParameters = null;
    private ActionHandler rootHandler = null;

    // Base Context
    private int workAllowed = 500;
    private int actionsPerformed;
    private boolean finished = false;

    public CastContext(@Nonnull MageSpell spell, @Nullable Wand wand) {
        super(spell.getMage(), wand);
        this.spell = setSpell(spell);
        this.location = null;
        this.entity = null;
        this.base = this;
        this.result = SpellResult.NO_ACTION;
        this.startTime = System.currentTimeMillis();
        targetMessagesSent = new HashSet<>();
        messageParameters = new HashMap<>();
    }

    public CastContext(@Nonnull MageSpell spell) {
        this(spell, spell.getMage().getActiveWand());
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy) {
        this(copy, copy.getEntity(), copy instanceof CastContext ? ((CastContext) copy).location : null);
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy, Mage sourceMage, Entity sourceEntity, Location sourceLocation) {
        super(sourceMage, sourceMage.getActiveWand());
        this.location = sourceLocation == null ? null : sourceLocation.clone();
        this.entity = new WeakReference<>(sourceEntity);
        copyFrom(copy);
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy, Entity sourceEntity, Location sourceLocation) {
        super(copy.getMage(), copy.getWand());
        this.location = sourceLocation == null ? null : sourceLocation.clone();
        this.entity = new WeakReference<>(sourceEntity);
        copyFrom(copy);
    }

    protected void copyFrom(com.elmakers.mine.bukkit.api.action.CastContext copy) {
        this.spell = setSpell((MageSpell)copy.getSpell());
        this.setTargetEntity(copy.getTargetEntity());
        this.targetLocation = copy.getTargetLocation();
        if (this.targetLocation != null) {
            this.targetLocation = this.targetLocation.clone();
        }
        this.undoList = copy.getUndoList();
        this.targetName = copy.getTargetName();
        this.targetMessagesSent = copy.getTargetMessagesSent();
        this.currentEffects = copy.getCurrentEffects();
        this.result = copy.getResult();
        this.mageClass = copy.getMageClass();
        this.startTime = copy.getStartTime();
        this.workingParameters = copy.getWorkingParameters();
        this.rootHandler = copy.getRootHandler();

        Location centerLocation = copy.getTargetCenterLocation();
        if (centerLocation != null) {
            targetCenterLocation = centerLocation.clone();
        }

        if (copy instanceof CastContext)
        {
            this.variables = ((CastContext)copy).variables;
            this.base = ((CastContext)copy).base;
            this.initialResult = ((CastContext)copy).initialResult;
            this.alternateResult = ((CastContext)copy).alternateResult;
            this.direction = ((CastContext)copy).direction;
            this.messageParameters = ((CastContext)copy).messageParameters;
            this.targetCaster = ((CastContext)copy).targetCaster;
            this.brush = ((CastContext)copy).brush;
            this.previousBlock = ((CastContext)copy).previousBlock;
        }
        else
        {
            this.base = this;
        }
    }

    @Nonnull
    private MageSpell setSpell(MageSpell spell) {
        Preconditions.checkNotNull(spell);
        this.spell = spell;
        this.mageClass = (this.wand == null ? this.mage.getActiveClass() : this.wand.getMageClass());
        if (spell instanceof BaseSpell)
        {
            this.baseSpell = (BaseSpell)spell;
        }
        if (spell instanceof UndoableSpell)
        {
            this.undoSpell = (UndoableSpell)spell;
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
        return spell;
    }

    /**
     * This is an unfortunate necessity to deal with a circular dependency issue.
     * UndoList keeps a reference to the current CastContext, so that it can play undo effects.
     * CastContext keeps a reference to the UndoList for its cast
     * Both are created on demand, so creating one causes the creation of the other, but this means
     * UndoList tries to get a reference to CastContext before it has been assigned to its spell.
     */
    public void initialize() {
        if (undoSpell != null) {
            undoList = undoSpell.createUndoList();
        }
    }

    @Nullable
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
    public Location getEyeLocation() {
        if (location != null) {
            return location;
        }

        Entity entity = getContextEntity();
        if (entity != null) {
            if (entity instanceof  LivingEntity) {
                return ((LivingEntity) entity).getEyeLocation();
            }
            return entity.getLocation();
        }

        return spell.getEyeLocation();
    }

    @Nullable
    protected Entity getContextEntity() {
        return entity == null ? null : entity.get();
    }

    protected boolean hasContextEntity() {
        return entity != null;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        if (hasContextEntity()) {
            return getContextEntity();
        }

        return spell.getEntity();
    }

    @Nullable
    @Override
    public LivingEntity getLivingEntity() {
        Entity entity = getEntity();
        return entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }

    @Nullable
    @Override
    public Location getLocation() {
        if (location != null) {
            return location;
        }
        Entity entity = getContextEntity();
        if (entity != null) {
            return entity.getLocation();
        }
        return spell.getLocation();
    }

    @Nullable
    public Location getSelectedLocation() {
        if (targetingSpell == null) {
            return null;
        }
        return targetingSpell.getSelectedLocation();
    }

    @Nullable
    @Override
    public Location getTargetLocation() {
        return targetLocation;
    }

    @Nullable
    @Override
    public Location getTargetSourceLocation() {
        return targetSourceLocation == null ? targetLocation : targetSourceLocation;
    }

    @Nullable
    @Override
    public Block getTargetBlock() {
        if (targetBlock != null) {
            return targetBlock;
        }
        return targetLocation == null ? null : targetLocation.getBlock();
    }

    @Nullable
    @Override
    public Entity getTargetEntity() {
        return targetEntity == null ? null : targetEntity.get();
    }

    @Nullable
    @Override
    public Vector getDirection() {
        if (direction != null) {
            return direction.clone();
        }
        Location location = getLocation();
        if (location == null) {
            return null;
        }
        return location.getDirection();
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

    @Nullable
    @Override
    public World getWorld() {
        Location location = getLocation();
        return location == null ? null : location.getWorld();
    }

    @Override
    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = new WeakReference<>(targetEntity);
    }

    @Override
    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public void setTargetBlock(Block targetBlock) {
        this.targetBlock = targetBlock;
    }

    @Override
    public void setTargetSourceLocation(Location targetLocation) {
        targetSourceLocation = targetLocation;
    }

    @Override
    public Spell getSpell() {
        return spell;
    }

    @Nullable
    @Override
    public MageClass getMageClass() {
        return this.mageClass;
    }

    @Override
    @Nonnull
    public CasterProperties getActiveProperties() {
        if (wand != null) {
            return verifyNotNull(wand);
        }
        return mage.getActiveProperties();
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
    public void registerForUndo(Entity entity)
    {
        addWork(5);
        if (undoList != null)
        {
            undoList.add(entity);
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
    public void clearAttachables(Block block)
    {
        addWork(50);
        if (undoList != null)
        {
            undoList.clearAttachables(block);
        }
    }

    @Override
    public void updateBlock(Block block) {
        MageController controller = getController();
        controller.updateBlock(block);
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
    public void registerPotionEffectForRemoval(Entity entity, PotionEffectType effectType) {
        addWork(5);
        if (undoList != null) {
            undoList.addPotionEffectForRemoval(entity, effectType);
        }
    }

    @Override
    public void registerModifier(Entity entity, MageModifier modifier) {
        addWork(5);
        if (undoList != null) {
            undoList.addModifier(entity, modifier);
        }
    }

    @Override
    public void registerModifierForRemoval(Entity entity, String modifierKey) {
        addWork(5);
        if (undoList != null) {
            undoList.addModifierForRemoval(entity, modifierKey);
        }
    }

    @Nullable
    @Override
    public Block getPreviousBlock() {
        if (previousBlock != null) return previousBlock;
        return targetingSpell != null ? targetingSpell.getPreviousBlock() : null;
    }

    @Nullable
    @Override
    public Block getPreviousPreviousBlock() {
        return targetingSpell != null ? targetingSpell.getPreviousPreviousBlock() : null;
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

    private void multiplyParameter(String key, ConfigurationSection parameterMap, ConfigurationSection configuration, ConfigurationSection baseConfiguration) {
        String multiplyKey = key + "_multiplier";
        if (configuration.contains(multiplyKey)) {
            double baseValue = baseConfiguration != null ? baseConfiguration.getInt(key) : 0;
            double value = configuration.getDouble(key, baseValue);
            double multiplier = configuration.getDouble(multiplyKey, 1);
            if (multiplier != 1) {
                value = multiplier * value;
            }
            parameterMap.set("$duration", value);
        }
    }

    private void addParameters(ConfigurationSection parameterMap, ConfigurationSection configuration, ConfigurationSection baseConfiguration) {
        if (configuration == null) return;
        Collection<String> keys = configuration.getKeys(false);
        for (String key : keys) {
            parameterMap.set("$" + key, configuration.get(key));
        }

        // Some specific special cases in here to support multipliers used in headshots and upgrades
        multiplyParameter("duration", parameterMap, configuration, baseConfiguration);
        multiplyParameter("damage", parameterMap, configuration, baseConfiguration);
    }

    @Override
    public Collection<EffectPlayer> getEffects(String effectKey) {
        Collection<EffectPlayer> effects = spell.getEffects(effectKey);
        if (effects.size() == 0) return effects;

        // Create parameter map
        ConfigurationSection parameterMap = null;
        ConfigurationSection handlerParameters = spell.getHandlerParameters(effectKey);
        if (handlerParameters != null || workingParameters != null) {
            if (workingParameters != null) {
                parameterMap = ConfigurationUtils.cloneEmptyConfiguration(workingParameters);
            } else {
                parameterMap = ConfigurationUtils.cloneEmptyConfiguration(handlerParameters);
            }
            addParameters(parameterMap, workingParameters, null);
            addParameters(parameterMap, handlerParameters, workingParameters);
        }

        for (EffectPlayer player : effects)
        {
            // Track effect plays for cancelling
            trackEffects(player);

            // Set material and color
            player.setMaterial(brush != null ? brush : spell.getEffectMaterial());
            player.setColor(getEffectColor());
            player.setParticleOverride(getEffectParticle());

            // Set parameters
            player.setParameterMap(parameterMap);
        }

        return effects;
    }

    @Override
    @Nullable
    public Color getEffectColor() {
        Color color = wand == null ? null : wand.getEffectColor();
        if (color == null) {
            color = spell.getEffectColor();
        }
        return color;
    }

    @Override
    @Nullable
    public String getEffectParticle() {
        String particle = wand == null ? null : wand.getEffectParticleName();
        if (particle == null) {
            particle = spell.getEffectParticle();
        }
        return particle;
    }

    @Override
    public void playEffects(String key)
    {
        playEffects(key, 1.0f);
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
                // Set selected location
                player.setSelection(getSelectedLocation());

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
                    target = player.getTargetLocation(this);
                }
                if (sourceBlock != null) {
                    player.setMaterial(sourceBlock);
                }
                player.start(source, sourceEntity, target, targetEntity, targeted);
            }
        }
    }

    @Override
    public String getMessage(String key) {
        return getMessage(key, "");
    }

    @Override
    public String getMessage(String key, String def) {
        return baseSpell != null ? baseSpell.getMessage(key, def) : def;
    }

    @Nullable
    @Override
    public Location findPlaceToStand(Location target, int verticalSearchDistance, boolean goUp) {
        return baseSpell != null ? baseSpell.findPlaceToStand(target, goUp, verticalSearchDistance) : location;
    }

    @Nullable
    @Override
    public Location findPlaceToStand(Location targetLoc, int verticalSearchDistance) {
        return baseSpell != null ? baseSpell.findPlaceToStand(targetLoc, verticalSearchDistance, verticalSearchDistance) : location;
    }

    @Override
    public int getVerticalSearchDistance()  {
        return baseSpell != null ? baseSpell.getVerticalSearchDistance() : 4;
    }

    @Override
    @Deprecated
    public boolean isOkToStandIn(Material material) {
        return baseSpell == null || baseSpell.isOkToStandIn(material);
    }

    @Override
    public boolean isOkToStandIn(Block block) {
        return baseSpell == null || baseSpell.isOkToStandIn(block);
    }

    @Override
    public boolean isWater(Material mat)
    {
        return DefaultMaterials.isWater(mat);
    }

    // This is primarily deprecated for API consistency
    // And possibly doing this via the material system in the future
    @Override
    @Deprecated
    public boolean isOkToStandOn(Material material) {
        return isOkToStandOn0(material);
    }

    @Override
    public boolean isOkToStandOn(Block block) {
        return isOkToStandOn0(block.getType());
    }

    private boolean isOkToStandOn0(Material material) {
        return material != Material.AIR && !DefaultMaterials.isLava(material);
    }

    @Override
    @Deprecated
    public boolean allowPassThrough(Material material) {
        return baseSpell == null || baseSpell.allowPassThrough(material);
    }

    @Override
    public boolean allowPassThrough(Block block) {
        return baseSpell == null || baseSpell.allowPassThrough(block);
    }

    @Override
    public void castMessageKey(String key, String message) {
        if (baseSpell != null) {
            baseSpell.castMessageKey(key, message);
        }
    }

    @Override
    public void castMessageKey(String key) {
        castMessageKey(key, null);
    }

    @Override
    public void sendMessageKey(String key, String message) {
        if (baseSpell != null) {
            baseSpell.sendMessageKey(mage, key, message);
        }
    }

    @Override
    public void sendMessageKey(String key) {
        sendMessageKey(key, null);
    }

    @Override
    public void showMessage(String key, String def) {
        Mage mage = getMage();
        mage.sendMessage(getMessage(key, def));
    }

    @Override
    public void showMessage(String message) {
        Mage mage = getMage();
        mage.sendMessage(message);
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
            baseSpell.sendMessage(mage, message);
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
    @Deprecated
    public boolean isTransparent(Material material) {
        if (targetingSpell != null) {
            return targetingSpell.isTransparent(material);
        }

        return material.isTransparent();
    }

    @Override
    public boolean isTransparent(Block block) {
        if (targetingSpell != null) {
            return targetingSpell.isTransparent(block);
        }

        return block.getType().isTransparent();
    }

    @Override
    @Deprecated
    public boolean isPassthrough(Material material) {
        if (baseSpell != null) {
            return baseSpell.isPassthrough(material);
        }

        return material.isTransparent();
    }

    @Override
    public boolean isPassthrough(Block block) {
        if (baseSpell != null) {
            return baseSpell.isPassthrough(block);
        }

        return block.getType().isTransparent();
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
        return canTarget(entity, null);
    }

    @Override
    public boolean canTarget(Entity entity, Class<?> targetType) {
        if (!getTargetsCaster() && entity != null && getEntity() != null && getEntity().equals(entity)) return false;
        return targetingSpell == null ? true : targetingSpell.canTarget(entity, targetType);
    }

    @Nullable
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
            playerMessage = parameterizeMessage(playerMessage);
            for (Entity target : targets)
            {
                UUID targetUUID = target.getUniqueId();
                if (target instanceof Player && target != sourceEntity && !targetMessagesSent.contains(targetUUID))
                {
                    targetMessagesSent.add(targetUUID);
                    Mage targetMage = controller.getRegisteredMage(target);
                    if (targetMage != null) {
                        targetMage.sendMessage(playerMessage);
                    }
                }
            }
        }
    }

    @Nullable
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

        mage.sendDebugMessage(ChatColor.WHITE + "Finish " + ChatColor.GOLD + spell.getName() + ChatColor.WHITE  + ": " + ChatColor.AQUA + result.name().toLowerCase(), 2);
        spell.finish(this);

        String resultName = result.name().toLowerCase();
        castMessageKey(resultName + "_finish");
        playEffects(resultName + "_finish");
    }

    @Override
    public void retarget(double range, double fov, double closeRange, double closeFOV, boolean useHitbox) {
        if (targetingSpell != null)
        {
            targetingSpell.retarget(this, range, fov, closeRange, closeFOV, useHitbox);
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

    @Nullable
    @Override
    public Location getTargetCenterLocation() {
        return targetCenterLocation == null ? targetLocation : targetCenterLocation;
    }

    @Override
    public void setTargetCenterLocation(Location location) {
        this.targetCenterLocation = location;
    }

    @Override
    public Set<UUID> getTargetMessagesSent() {
        return targetMessagesSent;
    }

    @Nullable
    @Override
    public Plugin getPlugin() {
        MageController controller = getController();
        return controller.getPlugin();
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
            // We should check that it is loaded before we get here, ideally
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

    @Nullable
    @Override
    @Deprecated
    public Set<Material> getMaterialSet(String key) {
        return MaterialSets.toLegacy(
                getController().getMaterialSetManager().fromConfig(key));
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
        if (result != SpellResult.PENDING && result != SpellResult.NO_ACTION) {
            this.result = this.result.min(result);
        }
    }

    @Override
    public void setInitialResult(SpellResult result) {
        initialResult = result;
    }

    public SpellResult getInitialResult() {
        return initialResult;
    }

    public void setAlternateResult(SpellResult result) {
        alternateResult = result;
    }

    public SpellResult getAlternateResult() {
        return alternateResult;
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
    public boolean canContinue(Location location)
    {
        if (baseSpell != null) {
            return baseSpell.canContinue(location);
        }
        return true;
    }

    @Override
    public boolean isBreakable(Block block) {
        return com.elmakers.mine.bukkit.block.UndoList.getRegistry().isBreakable(block);
    }

    @Nullable
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
        if (targetingSpell != null && targetingSpell.isReflective(block)) {
            return true;
        }
        return com.elmakers.mine.bukkit.block.UndoList.getRegistry().isReflective(block);
    }

    @Nullable
    @Override
    public Double getReflective(Block block) {
        if (block == null) return null;

        if (targetingSpell != null && targetingSpell.isReflective(block)) {
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
        undoList.addDamage(block, addAmount);
        return breakAmount;
    }

    @Override
    public void registerFakeBlock(Block block, Collection<WeakReference<Player>> players) {
        undoList.registerFakeBlock(block, players);
    }

    @Override
    public void unregisterBreaking(Block block) {
        com.elmakers.mine.bukkit.block.UndoList.getRegistry().unregisterBreaking(block);
    }

    public String parameterizeMessage(String message) {
        return parameterize(message, "$");
    }

    @Override
    public String parameterize(String command) {
        return parameterize(command, "@");
    }

    private String parameterize(String command, String prefix) {
        if (command == null || command.isEmpty()) return "";
        Location location = getLocation();
        Mage mage = getMage();
        MageController controller = getController();

        ConfigurationSection variables = getAllVariables();
        List<String> keys = new ArrayList<>(variables.getKeys(false));
        Collections.sort(keys, (o1, o2) -> o2.length() - o1.length());
        for (String key : keys) {
            command = command.replace("$" + key, variables.getString(key));
        }
        for (String key : keys) {
            command = command.replace("@" + key, Integer.toString(variables.getInt(key)));
        }

        List<String> attributes = new ArrayList<>(controller.getAttributes());
        Collections.sort(attributes, (o1, o2) -> o2.length() - o1.length());
        for (String attribute : attributes) {
            Double value = mage.getAttribute(attribute);
            command = command.replace("$" + attribute, value == null ? "?" : Double.toString(value));
        }
        for (String attribute : attributes) {
            Double value = mage.getAttribute(attribute);
            command = command.replace("@" + attribute, value == null ? "?" : Integer.toString((int)(double)value));
        }

        for (Map.Entry<String, String> entry : messageParameters.entrySet()) {
            command = command.replace(prefix + entry.getKey(), entry.getValue());
        }

        command = command
                .replace(prefix + "_", " ")
                .replace(prefix + "spell", getSpell().getName())
                .replace(prefix + "pd", mage.getDisplayName())
                .replace(prefix + "pn", mage.getName())
                .replace(prefix + "uuid", mage.getId())
                .replace(prefix + "p", mage.getName());

        if (location != null) {
            command = command
                    .replace(prefix + "world", location.getWorld().getName())
                    .replace("$x", Double.toString(location.getX()))
                    .replace("$y", Double.toString(location.getY()))
                    .replace("$z", Double.toString(location.getZ()))
                    .replace("@x", Integer.toString(location.getBlockX()))
                    .replace("@y", Integer.toString(location.getBlockY()))
                    .replace("@z", Integer.toString(location.getBlockZ()));
        }

        Location targetLocation = getTargetLocation();
        if (targetLocation != null) {
            command = command
                    .replace("@tworld", targetLocation.getWorld().getName())
                    .replace("$tx", Double.toString(targetLocation.getX()))
                    .replace("$ty", Double.toString(targetLocation.getY()))
                    .replace("$tz", Double.toString(targetLocation.getZ()))
                    .replace("@tx", Integer.toString(targetLocation.getBlockX()))
                    .replace("@ty", Integer.toString(targetLocation.getBlockY()))
                    .replace("@tz", Integer.toString(targetLocation.getBlockZ()));
        }

        Entity targetEntity = getTargetEntity();
        if (targetEntity != null) {
            if (controller.isMage(targetEntity)) {
                Mage targetMage = controller.getMage(targetEntity);
                command = command
                        .replace(prefix + "d", targetMage.getDisplayName())
                        .replace(prefix + "tn", targetMage.getName())
                        .replace(prefix + "tuuid", targetMage.getId())
                        .replace(prefix + "t", targetMage.getName());
            } else {
                command = command
                        .replace(prefix + "td", controller.getEntityDisplayName(targetEntity))
                        .replace(prefix + "tn", controller.getEntityName(targetEntity))
                        .replace(prefix + "tuuid", targetEntity.getUniqueId().toString())
                        .replace(prefix + "t", controller.getEntityName(targetEntity));
            }
        }

        return ChatColor.translateAlternateColorCodes('&', command);
    }

    @Override
    public void addHandler(ActionHandler handler) {
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

    @Override
    public void setPreviousBlock(Block block) {
        this.previousBlock = block;
    }

    @Override
    public void addMessageParameter(String key, String value) {
        messageParameters.put(key, value);
    }

    @Override
    public ConfigurationSection getAllVariables() {
        ConfigurationSection combinedVariables = new MemoryConfiguration();
        if (variables != null) {
            ConfigurationUtils.addConfigurations(combinedVariables, variables, false);
        }
        if (baseSpell != null) {
            ConfigurationUtils.addConfigurations(combinedVariables, baseSpell.getVariables(), false);
        }
        if (mage != null) {
            ConfigurationUtils.addConfigurations(combinedVariables, mage.getVariables(), false);
        }
        return combinedVariables;
    }

    @Override
    public ConfigurationSection getVariables() {
        if (variables == null) {
            variables = new MemoryConfiguration();
        }
        return variables;
    }

    @Override
    @Nonnull
    public ConfigurationSection getVariables(VariableScope scope) {
        switch (scope) {
            case SPELL:
                return baseSpell == null ? getVariables() : baseSpell.getVariables();
            case MAGE:
                return mage == null ? getVariables() : mage.getVariables();
            case CAST:
            default:
                return getVariables();
        }
    }

    @Override
    @Nullable
    public Double getVariable(String variable) {
        if (variables != null && variables.contains(variable)) {
            return variables.getDouble(variable);
        }
        if (baseSpell != null) {
            ConfigurationSection spellVariables = baseSpell.getVariables();
            if (spellVariables != null && spellVariables.contains(variable)) {
                return spellVariables.getDouble(variable);
            }
        }
        if (mage != null) {
            ConfigurationSection mageVariables = mage.getVariables();
            if (mageVariables != null && mageVariables.contains(variable)) {
                return mageVariables.getDouble(variable);
            }
        }
        return null;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public ConfigurationSection getWorkingParameters() {
        return workingParameters;
    }

    public void setWorkingParameters(ConfigurationSection workingParameters) {
        this.workingParameters = workingParameters;
    }

    public void setRootHandler(ActionHandler handler) {
        rootHandler = handler;
    }

    @Override
    public ActionHandler getRootHandler() {
        return rootHandler;
    }

    @Override
    public void setCastData(@Nonnull String key, Object value) {
        if (castData == null) {
            castData = new HashMap<>();
        }
        castData.put(key, value);
    }

    @Override
    @Nullable
    public Object getCastData(@Nonnull String key) {
        return castData == null ? null : castData.get(key);
    }
}
