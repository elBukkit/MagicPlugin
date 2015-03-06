package com.elmakers.mine.bukkit.api.spell;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Represents a Spell that may be cast by a Mage.
 * 
 * Each Spell is based on a SpellTemplate, which are defined
 * by the spells configuration files.
 * 
 * Every spell uses a specific Class that must extend from
 * com.elmakers.mine.bukkit.plugins.magic.spell.Spell.
 * 
 * To create a new custom spell from scratch, you must also
 * implement the MageSpell interface.
 */
public interface Spell extends SpellTemplate {
    public MageController getController();
    public boolean cast();
    public boolean cast(String[] parameters);
    public boolean cast(String[] parameters, Location defaultLocation);
    public Location getLocation();
    public Location getEyeLocation();
    public void target();
    public Location getTargetLocation();
    public Entity getTargetEntity();
    public Vector getDirection();
    public boolean canTarget(Entity entity);
    public boolean isActive();
    public boolean hasBrushOverride();
    public boolean canCast(Location location);
    public long getRemainingCooldown();
    public CastingCost getRequiredCost();
    public void messageTargets(String messageKey);
    public void playEffects(String effectName);
    public CastContext getCurrentCast();
    public void playEffects(String effectName, CastContext context);
    public void playEffects(String effectName, CastContext context, float scale);

    @Deprecated
    public void playEffects(String effectName, float scale, Location source, Entity sourceEntity, Location target, Entity targetEntity);

}
