package com.elmakers.mine.bukkit.api.spell;

import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.MageController;

/**
 * Represents a Spell that may be cast by a Mage.
 *
 * <p>Each Spell is based on a SpellTemplate, which are defined
 * by the spells configuration files.
 *
 * <p>Every spell uses a specific Class that must extend from
 * com.elmakers.mine.bukkit.plugins.magic.spell.Spell.
 *
 * <p>To create a new custom spell from scratch, you must also
 * implement the MageSpell interface.
 */
public interface Spell extends SpellTemplate {
    MageController getController();
    boolean cast();
    boolean cast(@Nullable String[] parameters);
    boolean cast(@Nullable String[] parameters, @Nullable Location defaultLocation);
    boolean cast(@Nullable ConfigurationSection parameters, @Nullable Location defaultLocation);
    boolean cast(@Nullable ConfigurationSection parameters);
    @Nullable
    Location getLocation();
    Entity getEntity();
    Location getEyeLocation();
    void target();
    @Nullable
    Location getTargetLocation();
    @Nullable
    Entity getTargetEntity();
    Vector getDirection();
    boolean canTarget(Entity entity);
    boolean isActive();
    boolean hasBrushOverride();
    boolean canCast(Location location);
    void clearCooldown();
    void reduceRemainingCooldown(long ms);
    void setRemainingCooldown(long ms);
    long getRemainingCooldown();
    @Nullable
    CastingCost getRequiredCost();
    void messageTargets(String messageKey);
    CastContext getCurrentCast();
    void playEffects(String effectName);
    void playEffects(String effectName, CastContext context);
    void playEffects(String effectName, CastContext context, float scale);
    boolean requiresBuildPermission();
    boolean requiresBreakPermission();
    boolean isPvpRestricted();
    boolean isDisguiseRestricted();
    void sendMessage(String message);
    void castMessage(String message);
    MaterialAndData getEffectMaterial();
    @Nullable
    String getEffectParticle();
    @Nullable
    Color getEffectColor();
    @Nullable
    MaterialBrush getBrush();
    boolean brushIsErase();
    boolean isCancellable();
    ConfigurationSection getWorkingParameters();
    void finish(CastContext context);
    double cancelOnDamage();
    boolean cancelOnCastOther();
    boolean cancelOnDeath();
    String getMessage(String messageKey);
    boolean hasHandlerParameters(String handlerKey);
    @Nullable
    ConfigurationSection getHandlerParameters(String handlerKey);
    long getProgressLevel();
    boolean cancelOnNoPermission();
    boolean cancelOnNoWand();

    /**
     * Signal that this spell was cancelled. Will send cancel messages
     * and play cancel FX.
     *
     * <p>This will not actually cancel any pending spell casts (batches) of this spell,
     * for that you will need Mage.cancelPending
     *
     * @return true (for legacy reasons)
     */
    boolean cancel();

    /**
     * Cancel a selection in-progress for a two-click selection spell (like Architect magic)
     *
     * <p>Will call cancel() if selection was cancelled.
     *
     * @return true if the spell was in the middle of selection and was cancelled.
     */
    boolean cancelSelection();
}
