package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import org.bukkit.plugin.Plugin;

import java.util.Set;

public class WGCustomFlagsManager {

    private final WGCustomFlagsPlugin customFlags;

    public static SetFlag<String> ALLOWED_SPELLS = new SetFlag<String>("allowed-spells", new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELLS = new SetFlag<String>("blocked-spells", new StringFlag(null));
    public static SetFlag<String> ALLOWED_SPELL_CATEGORIES = new SetFlag<String>("allowed-spell-categories", new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELL_CATEGORIES = new SetFlag<String>("blocked-spell-categories", new StringFlag(null));

    public WGCustomFlagsManager(Plugin wgCustomFlags) {
        customFlags = (WGCustomFlagsPlugin)wgCustomFlags;
        customFlags.addCustomFlag(ALLOWED_SPELLS);
        customFlags.addCustomFlag(BLOCKED_SPELLS);
        customFlags.addCustomFlag(ALLOWED_SPELL_CATEGORIES);
        customFlags.addCustomFlag(BLOCKED_SPELL_CATEGORIES);
    }

    public Boolean getCastPermission(RegionAssociable source, ApplicableRegionSet checkSet, SpellTemplate spell) {
        String spellKey = spell.getSpellKey().getBaseKey();
        SpellCategory category = spell.getCategory();

        Set<String> blocked = checkSet.queryValue(source, BLOCKED_SPELLS);
        if (blocked != null && (blocked.contains("*") || blocked.contains(spellKey))) return false;
        Set<String> blockedCategories = checkSet.queryValue(source, BLOCKED_SPELL_CATEGORIES);
        if (blockedCategories != null && category != null && blockedCategories.contains(category.getKey())) return false;

        Set<String> allowed = checkSet.queryValue(source, ALLOWED_SPELLS);
        if (allowed != null && (allowed.contains("*") || allowed.contains(spellKey))) return true;

        Set<String> allowedCategories = checkSet.queryValue(source, ALLOWED_SPELL_CATEGORIES);
        if (allowedCategories != null && category != null && allowedCategories.contains(category.getKey())) return true;

        return null;
    }
}
