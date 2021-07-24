package com.elmakers.mine.bukkit.api.spell;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.item.Cost;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.magic.Trigger;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.wand.Wand;

/**
 * A Spell template, as defined in the spells configuration files.
 */
public interface SpellTemplate extends Comparable<SpellTemplate>, CostReducer {
    String getName();
    String getAlias();
    String getDescription();
    String getExtendedDescription();
    String getLevelDescription();
    @Nullable
    String getCooldownDescription();
    @Nullable
    String getMageCooldownDescription();
    int getMaxCharges();
    double getChargeRegeneration();
    String getKey();
    SpellKey getSpellKey();
    @Nullable
    Color getColor();

    /**
     * The cost to purchase this spell.
     *
     * @return The cost of this spell, or null if this spell is free.
     */
    @Nullable
    Cost getCost();

    /**
     * How much this spell is worth. This will be the same as the amount provided by getCost, or zero
     * if this spell is free.
     *
     * @return How much this spell is worth, in whatever currency the spellshop is configured to work in.
     */
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

    /**
     * The costs to cast this spell.
     *
     * @return A list of casting costs, or null if this spell is free to cast.
     */
    @Nullable
    Collection<CastingCost> getCosts();
    @Nullable
    Collection<CastingCost> getActiveCosts();
    Collection<EffectPlayer> getEffects(SpellResult result);
    Collection<EffectPlayer> getEffects(String effectsKey);
    long getDuration();
    @Nullable
    String getDurationDescription(Messages messages);
    long getCooldown();
    @Nullable
    @Deprecated
    Spell createSpell();
    @Nullable
    MageSpell createMageSpell(Mage mage);
    void loadTemplate(String key, ConfigurationSection node);
    void loadPrerequisites(ConfigurationSection node);
    String getPermissionNode();
    @Nullable
    String getCategoryPermissionNode();
    boolean isHidden();
    boolean usesBrush();
    boolean usesBrushSelection();
    boolean showBrush();
    boolean isUndoable();
    boolean showUndoable();
    boolean isScheduledUndo();
    boolean isQuickCast();
    boolean disableManaRegenerationWhenActive();
    String getRequiredUpgradePath();
    Set<String> getRequiredUpgradeTags();
    long getRequiredUpgradeCasts();
    Collection<PrerequisiteSpell> getPrerequisiteSpells();

    /**
     * Returns a collection of spell keys of spells that should be removed when this spell is added to a wand.
     */
    Collection<SpellKey> getSpellsToRemove();
    String getUpgradeDescription();
    ConfigurationSection getConfiguration();
    void addLore(Messages messages, Mage mage, Wand wand, List<String> lore);
    boolean hasTag(String tag);
    boolean hasAnyTag(Collection<String> tags);
    @Nullable
    SpellTemplate getUpgrade();

    /**
     * Returns the maximum progress level for this spell.
     *
     * <p>This may return 0 even though a spell's current progress level is always 1 or greater. A result of 0 indicates
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

    Collection<Requirement> getRequirements();

    /**
     * This can hold the UUID of the owner of this spell, when using an editor
     * that supports saving owned spells.
     *
     * @return The id of the creator of this spell
     */
    @Nullable
    String getCreatorId();
    @Nullable
    String getCreator();

    boolean isToggleable();
    boolean isPassive();

    @Nullable
    Collection<Trigger> getTriggers();
}
