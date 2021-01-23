package com.elmakers.mine.bukkit.api.action;

import java.util.Collection;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;

/**
 * Action lifecycle:
 * On Config Load:
 *   initialize()
 * On Cast:
 *   start()
 *     -> default implementation calls prepare() for backwards-compatibility
 *   (compound action)
 *      prepare()
 *      reset()
 *      perform()
 *   (parameter update)
 *      prepare()
 */
public interface SpellAction extends Cloneable
{
    SpellResult perform(CastContext context);
    void initialize(Spell spell, ConfigurationSection baseParameters);

    /**
     * This should be used to load any parameters.
     * It is not a good idea to perform any logic or other initialization here,
     * since this method can be called at other times in the spell's cast life cycle (see header comments)
     */
    void prepare(CastContext context, ConfigurationSection parameters);

    /**
     * This is called once per spell cast, at the start of the cast.
     * It is normally not needed, but can be used to reset or initialize any transient data.
     */
    default void start(CastContext context, ConfigurationSection parameters) {
        prepare(context, parameters);
    }
    void finish(CastContext context);
    void reset(CastContext context);
    void getParameterNames(Spell spell, Collection<String> parameters);
    void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples);
    boolean usesBrush();
    boolean isUndoable();
    boolean requiresBuildPermission();
    boolean requiresBreakPermission();
    boolean requiresTarget();
    boolean requiresTargetEntity();
    boolean ignoreResult();
    String transformMessage(String message);
    int getActionCount();
    Object clone();

    /**
     * This mechanism never worked properly and is no longer called.
     * Actions that need to store data should interact with CastContext.getVariables instead.
     */
    @Deprecated
    default void load(Mage mage, ConfigurationSection data) { }

    /**
     * This mechanism never worked properly and is no longer called.
     * Actions that need to store data should interact with CastContext.getVariables instead.
     */
    @Deprecated
    default void save(Mage mage, ConfigurationSection data) { }
}
