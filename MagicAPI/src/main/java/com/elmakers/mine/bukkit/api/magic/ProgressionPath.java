package com.elmakers.mine.bukkit.api.magic;

import java.util.Collection;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.wand.Wand;

public interface ProgressionPath {
    String getKey();
    String getName();
    MaterialAndData getIcon();
    boolean requiresSpell(String spellKey);
    boolean hasExtraSpell(String spellKey);
    String getDescription();
    boolean hasPath(String pathName);
    boolean hasUpgrade();
    Set<String> getTags();
    boolean hasTag(String tag);
    boolean hasAnyTag(Collection<String> tagSet);
    boolean hasAllTags(Collection<String> tagSet);
    Set<String> getMissingTags(Collection<String> tagSet);
    String translatePath(String path);
    Collection<String> getSpells();
    Collection<String> getExtraSpells();
    Collection<String> getRequiredSpells();
    boolean earnsSP();
    boolean canProgress(CasterProperties properties);
    @Nullable
    ProgressionPath getNextPath();
    boolean checkUpgradeRequirements(CasterProperties caster, boolean quiet);
    boolean checkUpgradeRequirements(Wand wand, Mage mage);
    void upgrade(@Nonnull Mage mage, @Nullable Wand wand);
    ConfigurationSection getProperties();

    /**
     * Check to see if a specific spell is available on this exact path.
     */
    boolean hasSpell(String spellKey);

    /**
     * Check to see if a specific spell is available on this path or
     * any path inherited by this path.
     */
    boolean containsSpell(String spellKey);

    /**
     * Check to see if a specific brush is available on this exact path.
     */
    boolean hasBrush(String brushKey);

    /**
     * Check to see if a specific brush is available on this path or
     * any path inherited by this path.
     */
    boolean containsBrush(String brushKey);
}
