package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class WorldGuardFlagsManager implements WorldGuardFlags {

    private final WorldGuardPlugin customFlags;

    public static SetFlag<String> ALLOWED_SPELLS = new SetFlag<>("allowed-spells", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELLS = new SetFlag<>("blocked-spells", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> ALLOWED_SPELL_CATEGORIES = new SetFlag<>("allowed-spell-categories", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELL_CATEGORIES = new SetFlag<>("blocked-spell-categories", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> ALLOWED_WANDS = new SetFlag<>("allowed-wands", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> BLOCKED_WANDS = new SetFlag<>("blocked-wands", RegionGroup.ALL, new StringFlag(null));
    public static SetFlag<String> SPELL_OVERRIDES = new SetFlag<>("spell-overrides", RegionGroup.ALL, new StringFlag(null));
    public static StringFlag DESTRUCTIBLE = new StringFlag("destructible", RegionGroup.ALL);
    public static StringFlag REFLECTIVE = new StringFlag("reflective", RegionGroup.ALL);

    public WorldGuardFlagsManager(Plugin callingPlugin, Plugin wgCustomFlags) {
        customFlags = (WorldGuardPlugin)wgCustomFlags;
        FlagRegistry registry = customFlags.getFlagRegistry();
        registry.register(ALLOWED_SPELLS);
        registry.register(BLOCKED_SPELLS);
        registry.register(ALLOWED_SPELL_CATEGORIES);
        registry.register(BLOCKED_SPELL_CATEGORIES);
        registry.register(ALLOWED_WANDS);
        registry.register(BLOCKED_WANDS);
        registry.register(SPELL_OVERRIDES);
        registry.register(DESTRUCTIBLE);
        registry.register(REFLECTIVE);
        callingPlugin.getLogger().info("Registered custom WorldGuard flags: allowed-spells, blocked-spells, allowed-spell-categories, blocked-spell-categories, allowed-wands, blocked-wands, spell-overrides, destructible, reflective");
    }

    public String getDestructible(RegionAssociable source, ApplicableRegionSet checkSet)
    {
        return checkSet.queryValue(source, DESTRUCTIBLE);
    }

    public String getReflective(RegionAssociable source, ApplicableRegionSet checkSet)
    {
        return checkSet.queryValue(source, REFLECTIVE);
    }

    public Set<String> getSpellOverrides(RegionAssociable source, ApplicableRegionSet checkSet)
    {
        return checkSet.queryValue(source, SPELL_OVERRIDES);
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
}
