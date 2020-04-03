package com.elmakers.mine.bukkit.protection;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class WorldGuardFlagsManager implements WorldGuardFlags {
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

    public WorldGuardFlagsManager(Plugin callingPlugin, WorldGuardPlugin worldGuardPlugin, Object worldGuard) {
        FlagRegistry registry = null;
        if (worldGuard != null) {
            try {
                Method getFlagRegistryMethod = worldGuard.getClass().getMethod("getFlagRegistry");
                registry = (FlagRegistry)getFlagRegistryMethod.invoke(worldGuard);
            } catch (Exception ex) {
                callingPlugin.getLogger().log(Level.WARNING, "An error occurred binding to WorldGuard.getFlagRegistry", ex);
            }
        } else {
            registry = worldGuardPlugin.getFlagRegistry();
        }
        if (registry == null) {
            callingPlugin.getLogger().warning("Failed to find FlagRegistry, custom WorldGuard flags will not work");
        }
        registry.register(ALLOWED_SPELLS);
        registry.register(BLOCKED_SPELLS);
        registry.register(ALLOWED_SPELL_CATEGORIES);
        registry.register(BLOCKED_SPELL_CATEGORIES);
        registry.register(ALLOWED_WANDS);
        registry.register(BLOCKED_WANDS);
        registry.register(SPELL_OVERRIDES);
        registry.register(DESTRUCTIBLE);
        registry.register(REFLECTIVE);
        registry.register(SPAWN_TAGS);
        callingPlugin.getLogger().info("Registered custom WorldGuard flags: allowed-spells, blocked-spells, allowed-spell-categories, blocked-spell-categories, allowed-wands, blocked-wands, spell-overrides, destructible, reflective, spawn-tags");
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
