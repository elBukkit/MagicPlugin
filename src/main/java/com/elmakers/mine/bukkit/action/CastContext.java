package com.elmakers.mine.bukkit.action;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BlockSpell;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.spell.UndoableSpell;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Logger;

public class CastContext implements com.elmakers.mine.bukkit.api.action.CastContext {
    protected static Random random;

    private final Location location;
    private final Entity entity;
    private Location targetLocation;
    private Entity targetEntity;
    private UndoList undoList;
    private String targetName = null;

    private Collection<Entity> targetedEntities = Collections.newSetFromMap(new WeakHashMap<Entity, Boolean>());
    private Set<UUID> targetMessagesSent = new HashSet<UUID>();

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

    public CastContext(Spell spell) {
        this.setSpell(spell);
        this.location = null;
        this.entity = null;
        this.base = this;
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
        this.targetedEntities = copy.getTargetEntities();
        this.undoList = copy.getUndoList();
        this.targetName = copy.getTargetName();
        this.brush = copy.getBrush();
        if (copy instanceof CastContext)
        {
            this.base = ((CastContext)copy).base;
            this.targetMessagesSent = ((CastContext)copy).targetMessagesSent;
        } else {
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
    public Block getTargetBlock() {
        return targetLocation == null ? null : targetLocation.getBlock();
    }

    @Override
    public Entity getTargetEntity() {
        return targetEntity;
    }

    @Override
    public Vector getDirection() {
        return getLocation().getDirection();
    }

    @Override
    public World getWorld() {
        Location location = getLocation();
        return location == null ? null : location.getWorld();
    }

    @Override
    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
        addTargetEntity(targetEntity);
    }

    @Override
    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
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
    public Collection<EffectPlayer> getEffects(String key) {
        return spell.getEffects(key);
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
    public void registerForUndo(Block block, boolean addNeighbors)
    {
        addWork(10);
        if (undoList != null)
        {
            undoList.add(block, addNeighbors);
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
        return blockSpell != null ? blockSpell.hasBuildPermission(block) : false;
    }

    @Override
    public void playEffects(String key)
    {
        if (baseSpell != null)
        {
            baseSpell.playEffects(key, this);
        }
    }

    @Override
    public String getMessage(String key) {
        return getMessage(key, key);
    }

    @Override
    public String getMessage(String key, String def) {
        return baseSpell != null ? baseSpell.getMessage(key, def) : def;
    }

    @Override
    public Location tryFindPlaceToStand(Location location) {
        return baseSpell != null ? baseSpell.tryFindPlaceToStand(location) : location;
    }

    @Override
    public void castMessage(String message)
    {
        if (baseSpell != null)
        {
            baseSpell.castMessage(getMessage(message));
        }
    }

    @Override
    public void sendMessage(String message)
    {
        if (baseSpell != null)
        {
            baseSpell.sendMessage(getMessage(message));
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
    public boolean isDestructible(Block block)
    {
        if (blockSpell != null)
        {
            return blockSpell.isDestructible(block);
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
    public boolean canTarget(Entity entity) {
        return targetingSpell != null ? true : targetingSpell.canTarget(entity);
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
    public void addTargetEntity(Entity entity)
    {
        if (entity != null)
        {
            targetedEntities.add(entity);
        }
    }

    @Override
    public Collection<Entity> getTargetEntities()
    {
        return targetedEntities;
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
            Collection<Entity> targets = getTargetEntities();
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
        if (undoSpell != null && undoSpell.isUndoable())
        {
            undoSpell.registerForUndo();
        }
        castMessage("cast_finish");
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
    public com.elmakers.mine.bukkit.api.action.CastContext getBaseContext()
    {
        return base;
    }
}
