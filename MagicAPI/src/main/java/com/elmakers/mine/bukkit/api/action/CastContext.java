package com.elmakers.mine.bukkit.api.action;

import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.api.wand.Wand;

public interface CastContext {
    @Nullable
    Entity getEntity();
    LivingEntity getLivingEntity();
    @Nullable
    Location getLocation();
    @Nullable
    Location getTargetLocation();
    @Nullable
    Location getTargetCenterLocation();
    void setTargetCenterLocation(Location location);
    @Nullable
    Location getTargetSourceLocation();
    Vector getDirection();
    BlockFace getFacingDirection();
    void setDirection(Vector direction);
    @Nullable
    World getWorld();
    @Nullable
    Plugin getPlugin();
    Location getEyeLocation();

    /**
     * Currently return the cast source location, but in the future may change
     * to return only the wand location, or null if this spell was not cast with
     * a wand.
     * @return
     */
    @Nullable
    Location getWandLocation();

    /**
     * Get the source location of this cast.
     * @return
     */
    @Nullable
    Location getCastLocation();
    @Nullable
    Block getTargetBlock();
    @Nullable
    Block getInteractBlock();
    @Nullable
    Entity getTargetEntity();
    void setTargetEntity(Entity targetEntity);
    void setTargetLocation(Location targetLocation);
    void setTargetSourceLocation(Location targetLocation);
    Spell getSpell();
    Mage getMage();
    Wand getWand();
    @Nullable
    MageClass getMageClass();
    Collection<EffectPlayer> getEffects(String key);
    boolean hasEffects(String key);
    @Nullable
    MageController getController();
    void registerForUndo(Runnable runnable);
    void registerModified(Entity entity);
    void registerDamaged(Entity entity);
    void registerForUndo(Entity entity);
    void registerForUndo(Block block);
    void clearAttachables(Block block);
    void updateBlock(Block block);
    void registerVelocity(Entity entity);
    void registerMoved(Entity entity);
    void registerPotionEffects(Entity entity);
    void registerBreakable(Block block, double breakable);
    void registerReflective(Block block, double reflectivity);
    double registerBreaking(Block block, double breakAmount);
    void unregisterBreaking(Block block);
    Block getPreviousBlock();
    boolean isIndestructible(Block block);
    boolean hasBuildPermission(Block block);
    boolean hasBreakPermission(Block block);
    boolean isBreakable(Block block);
    boolean isReflective(Block block);
    @Nullable
    Double getBreakable(Block block);
    @Nullable
    Double getReflective(Block block);
    void clearBreakable(Block block);
    void clearReflective(Block block);
    void playEffects(String key);
    void playEffects(String key, float scale);
    void playEffects(String key, float scale, Block sourceBlock);
    void playEffects(String effectName, float scale, Location source, Entity sourceEntity, Location target, Entity targetEntity);
    void playEffects(String effectName, float scale, Location source, Entity sourceEntity, Location target, Entity targetEntity, Block sourceBlock);
    void cancelEffects();
    String getMessage(String key);
    String getMessage(String key, String def);
    @Nullable
    Location findPlaceToStand(Location target, int verticalSearchDistance);
    @Nullable
    Location findPlaceToStand(Location target, int verticalSearchDistance, boolean goUp);
    void castMessage(String message);
    void sendMessage(String message);
    void castMessageKey(String key);
    void sendMessageKey(String key);
    void showMessage(String message);
    void showMessage(String key, String def);
    void setTargetedLocation(Location location);
    Block findBlockUnder(Block block);
    Block findSpaceAbove(Block block);
    @Deprecated
    boolean isTransparent(Material material);
    boolean isTransparent(Block block);
    @Deprecated
    boolean isPassthrough(Material material);
    boolean isPassthrough(Block block);
    boolean isDestructible(Block block);
    boolean areAnyDestructible(Block block);
    boolean isTargetable(Block block);
    boolean canTarget(Entity entity);
    boolean canTarget(Entity entity, Class<?> targetType);
    @Nullable
    MaterialBrush getBrush();
    void setBrush(MaterialBrush brush);
    Collection<Entity> getTargetedEntities();
    void messageTargets(String messageKey);
    Random getRandom();
    UndoList getUndoList();
    void setTargetName(String name);
    String getTargetName();
    Logger getLogger();
    int getWorkAllowed();
    void setWorkAllowed(int work);
    void addWork(int work);
    void performedActions(int count);
    int getActionsPerformed();
    void finish();
    void retarget(double range, double fov, double closeRange, double closeFOV, boolean useHitbox);
    void retarget(double range, double fov, double closeRange, double closeFOV, boolean useHitbox, int yOffset, boolean targetSpaceRequired, int targetMinOffset);
    CastContext getBaseContext();
    Set<UUID> getTargetMessagesSent();
    Collection<EffectPlay> getCurrentEffects();
    boolean teleport(final Entity entity, final Location location, final int verticalSearchDistance);
    boolean teleport(final Entity entity, final Location location, final int verticalSearchDistance, boolean preventFall);
    boolean teleport(final Entity entity, final Location location, final int verticalSearchDistance, boolean preventFall, boolean safe);
    @Deprecated
    boolean allowPassThrough(Material material);
    boolean allowPassThrough(Block block);
    int getVerticalSearchDistance();
    @Deprecated
    boolean isOkToStandIn(Material mat);
    boolean isOkToStandIn(Block block);
    boolean isWater(Material mat);
    @Deprecated
    boolean isOkToStandOn(Material mat);
    boolean isOkToStandOn(Block block);
    @Nullable
    @Deprecated
    Set<Material> getMaterialSet(String key);
    void setSpellParameters(ConfigurationSection parameters);
    SpellResult getResult();
    void setResult(SpellResult result);
    void addResult(SpellResult result);
    boolean getTargetsCaster();
    void setTargetsCaster(boolean target);
    TargetType getTargetType();
    boolean canCast(Location location);
    String parameterize(String command);
    boolean isConsumeFree();
    void addHandler(ActionHandler handler);
    SpellResult processHandlers();
    boolean hasHandlers();
    CasterProperties getActiveProperties();
    void addMessageParameter(String key, String value);
}
