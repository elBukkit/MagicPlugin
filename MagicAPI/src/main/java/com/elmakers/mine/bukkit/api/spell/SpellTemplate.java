package com.elmakers.mine.bukkit.api.spell;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.wand.Wand;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;

/**
 * A Spell template, as defined in the spells configuration files.
 */
public interface SpellTemplate extends Comparable<SpellTemplate>, CostReducer {
    public String getName();
    public String getAlias();
    public String getDescription();
    public String getExtendedDescription();
    public String getLevelDescription();
    public String getCooldownDescription();
    public String getMageCooldownDescription();
    public String getKey();
    public SpellKey getSpellKey();
    public Color getColor();
    public double getWorth();
    public double getEarns();
    public double getRange();
    public SpellCategory getCategory();
    public long getCastCount();
    public void setCastCount(long count);
    public String getUsage();
    public MaterialAndData getIcon();
    public MaterialAndData getDisabledIcon();
    public String getIconURL();
    public String getDisabledIconURL();
    public boolean hasIcon();
    public boolean hasCastPermission(CommandSender sender);
    public Collection<CastingCost> getCosts();
    public Collection<CastingCost> getActiveCosts();
    public Collection<EffectPlayer> getEffects(SpellResult result);
    public Collection<EffectPlayer> getEffects(String effectsKey);
    public void getParameters(Collection<String> parameters);
    public void getParameterOptions(Collection<String> examples, String parameterKey);
    public long getDuration();
    public long getCooldown();
    public Spell createSpell();
    public void loadTemplate(String key, ConfigurationSection node);
    public void loadPrerequisites(ConfigurationSection node);
    public String getPermissionNode();
    public boolean isHidden();
    public boolean usesBrush();
    public boolean usesBrushSelection();
    public boolean isUndoable();
    public boolean showUndoable();
    public boolean isQuickCast();
    public String getRequiredUpgradePath();
    public Set<String> getRequiredUpgradeTags();
    public long getRequiredUpgradeCasts();
    public Collection<PrerequisiteSpell> getPrerequisiteSpells();

    /**
     * Returns a collection of spell keys of spells that should be removed when this spell is added to a wand.
     */
    public Collection<SpellKey> getSpellsToRemove();
    public String getUpgradeDescription();
    public ConfigurationSection getConfiguration();
    public void addLore(Messages messages, Mage mage, Wand wand, List<String> lore);
    public boolean hasTag(String tag);
    public boolean hasAnyTag(Collection<String> tags);
    public SpellTemplate getUpgrade();

    /**
     * Returns the maximum progress level for this spell.
     * <p/>
     * This may return 0 even though a spell's current progress level is always 1 or greater. A result of 0 indicates
     * that progress levels are not used for this spell.
     */
    public long getMaxProgressLevel();
}
