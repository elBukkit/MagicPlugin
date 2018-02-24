package com.elmakers.mine.bukkit.api.spell;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A Spell template, as defined in the spells configuration files.
 */
public interface SpellTemplate extends Comparable<SpellTemplate>, CostReducer {
    String getName();
    String getAlias();
    String getDescription();
    String getExtendedDescription();
    String getLevelDescription();
    String getCooldownDescription();
    String getMageCooldownDescription();
    String getKey();
    SpellKey getSpellKey();
    Color getColor();
    double getWorth();
    double getEarns();
    double getRange();
    SpellCategory getCategory();
    long getCastCount();
    void setCastCount(long count);
    String getUsage();
    MaterialAndData getIcon();
    MaterialAndData getDisabledIcon();
    String getIconURL();
    String getDisabledIconURL();
    boolean hasIcon();
    boolean hasCastPermission(CommandSender sender);
    Collection<CastingCost> getCosts();
    Collection<CastingCost> getActiveCosts();
    Collection<EffectPlayer> getEffects(SpellResult result);
    Collection<EffectPlayer> getEffects(String effectsKey);
    long getDuration();
    long getCooldown();
    Spell createSpell();
    void loadTemplate(String key, ConfigurationSection node);
    void loadPrerequisites(ConfigurationSection node);
    String getPermissionNode();
    boolean isHidden();
    boolean usesBrush();
    boolean usesBrushSelection();
    boolean isUndoable();
    boolean showUndoable();
    boolean isQuickCast();
    String getRequiredUpgradePath();
    Set<String> getRequiredUpgradeTags();
    long getRequiredUpgradeCasts();
    Collection<PrerequisiteSpell> getPrerequisiteSpells();
    String getRequiredSkillapiClass();
    String getRequiredSkillapiSkill();
    /**
     * Returns a collection of spell keys of spells that should be removed when this spell is added to a wand.
     */
    Collection<SpellKey> getSpellsToRemove();
    String getUpgradeDescription();
    ConfigurationSection getConfiguration();
    void addLore(Messages messages, Mage mage, Wand wand, List<String> lore);
    boolean hasTag(String tag);
    boolean hasAnyTag(Collection<String> tags);
    SpellTemplate getUpgrade();

    /**
     * Returns the maximum progress level for this spell.
     *
     * This may return 0 even though a spell's current progress level is always 1 or greater. A result of 0 indicates
     * that progress levels are not used for this spell.
     */
    long getMaxProgressLevel();

    /**
     * Retrieve the parameters as configured in this spell's parameters section.
     *
     * @return The configured spell parameters
     */
    ConfigurationSection getSpellParameters();

    /**
     * Add this spell's possible parameter keys to a collection. Used for tab-completion.
     *
     * @param parameters A non-null collection of spell parameters to add to.
     */
    void getParameters(Collection<String> parameters);

    /**
     * Add examples of possible values for a given parameter to this spell.
     * Used for tab-completion.
     *
     * @param examples A collection of strings to add possible parameter values to
     * @param parameterKey The parameter for which to retrieve example options.
     */
    void getParameterOptions(Collection<String> examples, String parameterKey);
}
