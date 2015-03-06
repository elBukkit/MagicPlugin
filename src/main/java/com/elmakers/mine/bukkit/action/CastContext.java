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

public class CastContext implements com.elmakers.mine.bukkit.api.action.CastContext {
    protected static Random random;

    private final Location location;
    private final Location eyeLocation;
    private final Entity entity;
    private Location targetLocation;
    private Entity targetEntity;
    private UndoList undoList;

    private Collection<Entity> targetedEntities = Collections.newSetFromMap(new WeakHashMap<Entity, Boolean>());
    private Set<UUID> targetMessagesSent = new HashSet<UUID>();

    private Spell spell;
    private BaseSpell baseSpell;
    private BlockSpell blockSpell;
    private MageSpell mageSpell;
    private BrushSpell brushSpell;
    private TargetingSpell targetingSpell;
    private UndoableSpell undoSpell;

    public CastContext(Spell spell) {
        this.setSpell(spell);
        this.location = spell.getLocation();
        this.eyeLocation = spell.getEyeLocation();
        Mage mage = this.getMage();
        this.entity = mage != null ? mage.getEntity() : null;
    }

    public CastContext(Spell spell, Entity sourceEntity, Location sourceLocation) {
        this.setSpell(spell);
        this.entity = sourceEntity;
        this.location = sourceLocation == null ? spell.getLocation() : sourceLocation;
        this.eyeLocation = (sourceEntity != null && sourceEntity instanceof LivingEntity) ?
            ((LivingEntity)sourceEntity).getEyeLocation() :
            (sourceLocation == null ? spell.getEyeLocation() : sourceLocation);
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy) {
        this.setSpell(copy.getSpell());
        this.entity = copy.getEntity();
        this.targetEntity = copy.getTargetEntity();
        this.location = copy.getLocation();
        this.eyeLocation = copy.getEyeLocation();
        this.targetLocation = copy.getTargetLocation();
        this.targetedEntities = copy.getTargetEntities();
        this.undoList = copy.getUndoList();
        if (copy instanceof CastContext)
        {
            this.targetMessagesSent = ((CastContext)copy).targetMessagesSent;
        }
    }

    public CastContext(com.elmakers.mine.bukkit.api.action.CastContext copy, Entity sourceEntity, Location sourceLocation) {
        this.setSpell(copy.getSpell());
        this.targetEntity = copy.getTargetEntity();
        this.targetLocation = copy.getTargetLocation();
        this.location = sourceLocation;
        this.entity = sourceEntity;
        this.eyeLocation = (sourceEntity != null && sourceEntity instanceof LivingEntity) ?
                ((LivingEntity)sourceEntity).getEyeLocation() :
                (sourceLocation == null ? spell.getEyeLocation() : sourceLocation);
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
        return eyeLocation;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public Location getLocation() {
        return location;
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
        return location.getDirection();
    }

    @Override
    public World getWorld() {
        return location.getWorld();
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
        if (undoList != null)
        {
            undoList.add(runnable);
        }
    }

    @Override
    public void registerModified(Entity entity)
    {
        if (undoList != null)
        {
            undoList.modify(entity);
        }
    }

    @Override
    public void registerForUndo(Entity entity)
    {
        if (undoList != null)
        {
            undoList.add(entity);
        }
    }

    @Override
    public void registerForUndo(Block block)
    {
        if (undoList != null)
        {
            undoList.add(block);
        }
    }

    @Override
    public void registerForUndo(Block block, boolean addNeighbors)
    {
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
        if (undoList != null)
        {
            undoList.modifyVelocity(entity);
        }
    }

    @Override
    public void registerMoved(Entity entity)
    {
        if (undoList != null)
        {
            undoList.move(entity);
        }
    }

    @Override
    public void registerPotionEffects(Entity entity)
    {
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
        return brushSpell == null ? null : brushSpell.getBrush();
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
}
