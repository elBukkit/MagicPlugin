package com.elmakers.mine.bukkit.api.magic;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;

import java.util.Collection;
import java.util.Set;

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

    /**
     * Check to see if a specific spell is available on this exact path.
     *
     * @param spellKey
     * @return
     */
    boolean hasSpell(String spellKey);

    /**
     * Check to see if a specific spell is available on this path or
     * any path inherited by this path.
     *
     * @param spellKey
     * @return
     */
    boolean containsSpell(String spellKey);
}
