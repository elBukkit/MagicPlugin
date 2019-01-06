package com.elmakers.mine.bukkit.protection;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

public class WGCustomFlagsManager implements WorldGuardFlags {

    private final WGCustomFlagsPlugin customFlags;

    public static SetFlag<String> ALLOWED_SPELLS = new SetFlag<>("allowed-spells", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELLS = new SetFlag<>("blocked-spells", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> ALLOWED_SPELL_CATEGORIES = new SetFlag<>("allowed-spell-categories", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELL_CATEGORIES = new SetFlag<>("blocked-spell-categories", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> ALLOWED_WANDS = new SetFlag<>("allowed-wands", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_WANDS = new SetFlag<>("blocked-wands", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> SPELL_OVERRIDES = new SetFlag<>("spell-overrides", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> SPAWN_TAGS = new SetFlag<>("spawn-tags", RegionGroup.ALL, new StringFlag(null));
    public static StringFlag DESTRUCTIBLE = new StringFlag("destructible", RegionGroup.ALL);
    public static StringFlag REFLECTIVE = new StringFlag("reflective", RegionGroup.ALL);

    public WGCustomFlagsManager(Plugin wgCustomFlags) {
        customFlags = (WGCustomFlagsPlugin)wgCustomFlags;
        customFlags.addCustomFlag(ALLOWED_SPELLS);
        customFlags.addCustomFlag(BLOCKED_SPELLS);
        customFlags.addCustomFlag(ALLOWED_SPELL_CATEGORIES);
        customFlags.addCustomFlag(BLOCKED_SPELL_CATEGORIES);
        customFlags.addCustomFlag(ALLOWED_WANDS);
        customFlags.addCustomFlag(BLOCKED_WANDS);
        customFlags.addCustomFlag(SPELL_OVERRIDES);
        customFlags.addCustomFlag(DESTRUCTIBLE);
        customFlags.addCustomFlag(REFLECTIVE);
        customFlags.addCustomFlag(SPAWN_TAGS);
    }

    @Nullable
    @Override
    public String getDestructible(RegionAssociable source, ApplicableRegionSet checkSet) {
        return checkSet.queryValue(source, DESTRUCTIBLE);
    }

    @Nullable
    @Override
    public String getReflective(RegionAssociable source, ApplicableRegionSet checkSet) {
        return checkSet.queryValue(source, REFLECTIVE);
    }

    @Nullable
    @Override
    public Set<String> getSpellOverrides(RegionAssociable source, ApplicableRegionSet checkSet) {
        return checkSet.queryValue(source, SPELL_OVERRIDES);
    }

    @Nullable
    @Override
    public Boolean getWandPermission(RegionAssociable source, ApplicableRegionSet checkSet, Wand wand) {
        String wandTemplate = wand.getTemplateKey();
        Set<String> blocked = checkSet.queryValue(source, BLOCKED_WANDS);
        if (blocked != null && blocked.contains(wandTemplate)) return false;

        Set<String> allowed = checkSet.queryValue(source, ALLOWED_WANDS);
        if (allowed != null && (allowed.contains("*") || allowed.contains(wandTemplate))) return true;

        if (blocked != null && blocked.contains("*")) return false;

        return null;
    }

    @Nullable
    @Override
    public Boolean getCastPermission(RegionAssociable source, ApplicableRegionSet checkSet, SpellTemplate spell) {
        String spellKey = spell.getSpellKey().getBaseKey();

        Set<String> blocked = checkSet.queryValue(source, BLOCKED_SPELLS);
        if (blocked != null && blocked.contains(spellKey)) return false;
        Set<String> blockedCategories = checkSet.queryValue(source, BLOCKED_SPELL_CATEGORIES);
        if (blockedCategories != null && spell.hasAnyTag(blockedCategories)) return false;

        Set<String> allowed = checkSet.queryValue(source, ALLOWED_SPELLS);
        if (allowed != null && (allowed.contains("*") || allowed.contains(spellKey))) return true;

        Set<String> allowedCategories = checkSet.queryValue(source, ALLOWED_SPELL_CATEGORIES);
        if (allowedCategories != null && spell.hasAnyTag(allowedCategories)) return true;

        if (blocked != null && blocked.contains("*")) return false;

        return null;
    }

    @Override
    public boolean inTaggedRegion(RegionAssociable source, ApplicableRegionSet checkSet, Set<String> tags) {
        Set<String> regionTags = checkSet.queryValue(source, SPAWN_TAGS);
        if (regionTags == null) {
            return false;
        }
        return regionTags.contains("*") || !Collections.disjoint(regionTags, tags);
    }
}
