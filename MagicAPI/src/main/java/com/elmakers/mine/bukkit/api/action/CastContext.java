package com.elmakers.mine.bukkit.api.action;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MageModifier;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.VariableScope;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.api.wand.Wand;

public interface CastContext extends MageContext {
    @Nullable
    @Override
    Entity getEntity();
    @Nullable
    @Override
    LivingEntity getLivingEntity();
    @Override
    @Nullable
    Location getLocation();
    @Override
    @Nullable
    Location getTargetLocation();
    @Nullable
    Location getTargetCenterLocation();
    void setTargetCenterLocation(Location location);
    @Nullable
    Location getTargetSourceLocation();
    @Nullable
    Vector getDirection();
    BlockFace getFacingDirection();
    void setDirection(Vector direction);
    @Nullable
    World getWorld();
    @Nullable
    Plugin getPlugin();
    @Override
    @Nullable
    Location getEyeLocation();

    /**
     * Currently return the cast source location, but in the future may change
     * to return only the wand location, or null if this spell was not cast with
     * a wand.
     * @return The location
     */
    @Override
    @Nullable
    Location getWandLocation();

    /**
     * Get the source location of this cast.
     * @return The location
     */
    @Override
    @Nullable
    Location getCastLocation();
    @Nullable
    Block getTargetBlock();
    @Nullable
    Block getInteractBlock();
    @Override
    @Nullable
    Entity getTargetEntity();
    void setTargetEntity(Entity targetEntity);
    void setTargetLocation(Location targetLocation);
    void setTargetSourceLocation(Location targetLocation);
    void setTargetBlock(Block targetBlock);
    @Nonnull
    Spell getSpell();
    @Nonnull
    @Override
    Mage getMage();
    @Nullable
    @Override
    Wand getWand();
    @Nullable
    MageClass getMageClass();
    Collection<EffectPlayer> getEffects(String key);
    boolean hasEffects(String key);
    @Override
    @Nonnull
    MageController getController();
    void registerForUndo(Runnable runnable);
    void registerForUndo(Entity entity);
    void registerForUndo(Block block);
    void registerModified(Entity entity);
    void registerDamaged(Entity entity);
    void clearAttachables(Block block);
    void updateBlock(Block block);
    void registerVelocity(Entity entity);
    void registerMoved(Entity entity);
    void registerPotionEffects(Entity entity);
    void registerPotionEffectForRemoval(Entity entity, PotionEffectType potionEffectType);
    void registerModifier(Entity entity, MageModifier modifier);
    void registerModifierForRemoval(Entity entity, String modifierKey);
    void registerBreakable(Block block, double breakable);
    void registerReflective(Block block, double reflectivity);
    double registerBreaking(Block block, double breakAmount);
    void unregisterBreaking(Block block);
    void registerFakeBlock(Block block, Collection<WeakReference<Player>> players);
    Block getPreviousBlock();
    Block getPreviousPreviousBlock();
    void setPreviousBlock(Block previous);
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

    @Override
    void cancelEffects();
    @Override
    Collection<EffectPlay> getCurrentEffects();
    @Nullable
    Location findPlaceToStand(Location target, int verticalSearchDistance);
    @Nullable
    Location findPlaceToStand(Location target, int verticalSearchDistance, boolean goUp);
    void castMessage(String message);
    void sendMessage(String message);
    void castMessageKey(String key, String message);
    void castMessageKey(String key);
    void sendMessageKey(String key, String message);
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
    @Nullable
    MaterialBrush getBrush();
    void setBrush(MaterialBrush brush);
    Collection<Entity> getTargetedEntities();
    void messageTargets(String messageKey);
    Random getRandom();
    UndoList getUndoList();
    void setTargetName(String name);
    String getTargetName();
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
    void setInitialResult(SpellResult result);
    void setResult(SpellResult result);
    void addResult(SpellResult result);
    boolean getTargetsCaster();
    void setTargetsCaster(boolean target);
    TargetType getTargetType();
    boolean canContinue(Location location);
    boolean canCast(Location location);
    String parameterize(String command);
    String parameterizeMessage(String message);
    boolean isConsumeFree();
    void addHandler(ActionHandler handler);
    SpellResult processHandlers();
    boolean hasHandlers();
    void addMessageParameter(String key, String value);
    long getStartTime();
    @Nonnull
    ConfigurationSection getVariables(VariableScope scope);

    /**
     * Returns cast-local variables
     */
    @Nonnull
    ConfigurationSection getVariables();

    /**
     * Returns all variables, this can be an expensive call so avoid using it if possible.
     * This is also not modifiable, the configuration section returned is a copy.
     */
    @Nonnull
    ConfigurationSection getAllVariables();
    ConfigurationSection getWorkingParameters();
    ActionHandler getRootHandler();
    void setCastData(@Nonnull String key, Object value);
    @Nullable
    Object getCastData(@Nonnull String key);
    void setDestructible(MaterialSet destructible);
    void setIndestructible(MaterialSet indestructible);
}
