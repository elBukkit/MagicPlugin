package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import org.bukkit.plugin.Plugin;

import java.util.Set;

public class WGCustomFlagsManager {

    private final WGCustomFlagsPlugin customFlags;

    public static SetFlag<String> ALLOWED_SPELLS = new SetFlag<String>("allowed-spells", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELLS = new SetFlag<String>("blocked-spells", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> ALLOWED_SPELL_CATEGORIES = new SetFlag<String>("allowed-spell-categories", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELL_CATEGORIES = new SetFlag<String>("blocked-spell-categories", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> ALLOWED_WANDS = new SetFlag<String>("allowed-wands", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_WANDS = new SetFlag<String>("blocked-wands", RegionGroup.ALL, new StringFlag(null));

    public WGCustomFlagsManager(Plugin wgCustomFlags) {
        customFlags = (WGCustomFlagsPlugin)wgCustomFlags;
        customFlags.addCustomFlag(ALLOWED_SPELLS);
        customFlags.addCustomFlag(BLOCKED_SPELLS);
        customFlags.addCustomFlag(ALLOWED_SPELL_CATEGORIES);
        customFlags.addCustomFlag(BLOCKED_SPELL_CATEGORIES);
        customFlags.addCustomFlag(ALLOWED_WANDS);
        customFlags.addCustomFlag(BLOCKED_WANDS);
    }

    public Boolean getWandPermission(RegionAssociable source, ApplicableRegionSet checkSet, Wand wand) {
        String wandTemplate = wand.getTemplateKey();
        Set<String> blocked = checkSet.queryValue(source, BLOCKED_WANDS);
        if (blocked != null && blocked.contains(wandTemplate)) return false;

        Set<String> allowed = checkSet.queryValue(source, ALLOWED_WANDS);
        if (allowed != null && (allowed.contains("*") || allowed.contains(wandTemplate))) return true;

        if (blocked != null && blocked.contains("*")) return false;

        return null;
    }

    public Boolean getCastPermission(RegionAssociable source, ApplicableRegionSet checkSet, SpellTemplate spell) {
        String spellKey = spell.getSpellKey().getBaseKey();
        SpellCategory category = spell.getCategory();

        Set<String> blocked = checkSet.queryValue(source, BLOCKED_SPELLS);
        if (blocked != null && blocked.contains(spellKey)) return false;
        Set<String> blockedCategories = checkSet.queryValue(source, BLOCKED_SPELL_CATEGORIES);
        if (blockedCategories != null && category != null && blockedCategories.contains(category.getKey())) return false;

        Set<String> allowed = checkSet.queryValue(source, ALLOWED_SPELLS);
        if (allowed != null && (allowed.contains("*") || allowed.contains(spellKey))) return true;

        Set<String> allowedCategories = checkSet.queryValue(source, ALLOWED_SPELL_CATEGORIES);
        if (allowedCategories != null && category != null && allowedCategories.contains(category.getKey())) return true;

        if (blocked != null && blocked.contains("*")) return false;

        return null;
    }
}
