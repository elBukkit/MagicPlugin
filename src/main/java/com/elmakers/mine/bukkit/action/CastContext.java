package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import com.elmakers.mine.bukkit.utility.Target;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private Entity targetEntity;
    private UndoList undoList;
    private String targetName = null;
    private SpellResult result = SpellResult.NO_ACTION;
    private SpellResult initialResult = SpellResult.CAST;
    private Vector direction = null;

    private Set<UUID> targetMessagesSent = new HashSet<UUID>();
    private Collection<EffectPlay> currentEffects = new ArrayList<EffectPlay>();

    private Spell spell;
    private BaseSpell baseSpell;
    private BlockSpell blockSpell;
    private MageSpell mageSpell;
    private BrushSpell brushSpell;
    private TargetingSpell targetingSpell;
    private UndoableSpell undoSpell;
    private MaterialBrush brush;
    private CastContext base;

    // Base Context
    private int workAllowed = 500;
    private int actionsPerformed;

    public CastContext() {
        this.location = null;
        this.entity = null;
        this.base = this;
        this.result = SpellResult.NO_ACTION;
        targetMessagesSent = new HashSet<UUID>();
        currentEffects = new ArrayList<EffectPlay>();

    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy) {
        this(copy, copy.getEntity(), copy.getLocation());
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
        if (copy instanceof CastContext)
        {
            this.base = ((CastContext)copy).base;
            this.initialResult = ((CastContext)copy).initialResult;
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
            this.mageSpell = (MageSpell)spell;
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
    public Location getWandLocation() {
        Mage mage = getMage();
        Location wandLocation = mage == null ? getEyeLocation() : mage.getWandLocation();
        if (wandLocation != null && direction != null) {
            wandLocation.setDirection(direction);
        }
        return wandLocation;
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
        return this.mageSpell == null ? null : this.mageSpell.getMage();
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
        ConfigurationSection workingParameters = spell != null ? spell.getWorkingParameters() : null;
        if (workingParameters != null) {
            Collection<String> keys = workingParameters.getKeys(false);
            parameterMap = new HashMap<String, String>();
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
            player.setColor(spell.getEffectColor());
            String overrideParticle = spell.getEffectParticle();
            player.setParticleOverride(overrideParticle);

            // Set parameters
            player.setParameterMap(parameterMap);
        }

        return effects;
    }

    @Override
    public void playEffects(String effectName, float scale)
    {
        Collection<EffectPlayer> effects = getEffects(effectName);
        if (effects.size() > 0)
        {
            Location wand = null;
            Location eyeLocation = getEyeLocation();
            Location location = getLocation();
            Collection<Entity> targeted = getTargetedEntities();
            Entity sourceEntity = getEntity();
            Entity targetEntity = getTargetEntity();
            Location targetLocation = getTargetLocation();

            for (EffectPlayer player : effects)
            {
                // Set scale
                player.setScale(scale);

                Mage mage = getMage();
                boolean useWand = mage != null && mage.getEntity() == sourceEntity && player.shouldUseWandLocation();
                Location source = player.shouldUseEyeLocation() ? eyeLocation : location;
                if (useWand) {
                    if (wand == null) {
                        wand = mage.getWandLocation();
                    }
                    source = wand;
                }
                Location target = targetLocation;
                if (!player.shouldUseHitLocation() && targetEntity != null) {
                    if (targetEntity instanceof LivingEntity) {
                        target = ((LivingEntity)targetEntity).getEyeLocation();
                    } else {
                        target = targetEntity.getLocation();
                    }
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
    public boolean isTargetable(Material material)
    {
        if (targetingSpell != null)
        {
            return targetingSpell.isTargetable(material);
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
        if (baseSpell != null) {
            return baseSpell.getTargetsCaster();
        }
        return false;
    }

    @Override
    public void setTargetsCaster(boolean target) {
        if (baseSpell != null) {
            baseSpell.setTargetsCaster(target);
        }
    }

    @Override
    public boolean canTarget(Entity entity) {
        return targetingSpell == null ? true : targetingSpell.canTarget(entity);
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
            return new ArrayList<Entity>();
        }

        return undoList.getAllEntities();
    }

    @Override
    public void getTargetEntities(int targetCount, Collection<WeakReference<Entity>> entities)
    {
        if (targetingSpell == null)
        {
            return;
        }
        List<Target> candidates = ((TargetingSpell)spell).getAllTargetEntities();
        if (targetCount < 0) {
            targetCount = entities.size();
        }

        for (int i = 0; i < targetCount && i < candidates.size(); i++) {
            Target target = candidates.get(i);
            entities.add(new WeakReference<Entity>(target.getEntity()));
        }
    }

    @Override
    public void messageTargets(String messageKey)
    {
        Mage mage = getMage();
        MageController controller = getController();
        LivingEntity sourceEntity = mage == null ? null : mage.getLivingEntity();
        String playerMessage = getMessage(messageKey);
        if (!mage.isStealth() && playerMessage.length() > 0)
        {
            Collection<Entity> targets = getTargetedEntities();
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
        if (isTargetable(playerBlock.getType())) return playerBlock;
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
        Mage mage = getMage();

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
            mage.sendDebugMessage(ChatColor.WHITE + "Finish " + ChatColor.GOLD + spell.getName() + ChatColor.WHITE  + ": " + ChatColor.AQUA + result, 2);
            spell.finish(this);
        }
        String resultName = result.name().toLowerCase();
        castMessageKey(resultName + "_finish");
        playEffects(resultName + "_finish");
    }

    @Override
    public void retarget(int range, double fov, double closeRange, double closeFOV, boolean useHitbox) {
        if (targetingSpell != null)
        {
            targetingSpell.retarget(range, fov, closeRange, closeFOV, useHitbox);
            setTargetEntity(targetingSpell.getTargetEntity());
            setTargetLocation(targetingSpell.getTargetLocation());
        }
    }

    @Override
    public void retarget(int range, double fov, double closeRange, double closeFOV, boolean useHitbox, int yOffset, boolean targetSpaceRequired, int targetMinOffset) {
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
    public Set<UUID> getTargetMessagesSent() {
        return targetMessagesSent;
    }

    @Override
    public Collection<EffectPlay> getCurrentEffects() {
        return currentEffects;
    }

    @Override
    public void registerBreakable(Block block, int breakable) {
        if (block == null || block.getType() == Material.AIR) return;
        MageController controller = getController();
        if (breakable > 0) {
            block.setMetadata("breakable", new FixedMetadataValue(controller.getPlugin(), breakable));
        } else {
            block.removeMetadata("breakable", controller.getPlugin());
        }
        undoList.setUndoBreakable(true);
    }

    @Override
    public void registerReflective(Block block, double reflectivity) {
        if (block == null || block.getType() == Material.AIR) return;
        MageController controller = getController();
        if (reflectivity > 0) {
            block.setMetadata("backfire", new FixedMetadataValue(controller.getPlugin(), reflectivity));
        } else {
            block.removeMetadata("backfire", controller.getPlugin());
        }
        undoList.setUndoReflective(true);
    }

    @Override
    public Plugin getPlugin() {
        MageController controller = getController();
        return controller == null ? null : controller.getPlugin();
    }

    @Override
    public void teleport(final Entity entity, final Location location, final int verticalSearchDistance)
    {
        Plugin plugin = getPlugin();
        TeleportTask task = new TeleportTask(getController(), entity, location, verticalSearchDistance, this);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, 1);
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
}
