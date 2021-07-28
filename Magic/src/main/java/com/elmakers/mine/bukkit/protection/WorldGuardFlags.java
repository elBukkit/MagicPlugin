package com.elmakers.mine.bukkit.protection;

import java.util.Set;
import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;

interface WorldGuardFlags {
    @Nullable
    String getPortalSpell(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    String getPortalWarp(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    String getDestructible(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    String getReflective(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    Set<String> getSpellOverrides(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    Boolean getWandPermission(RegionAssociable source, ApplicableRegionSet checkSet, Wand wand);

    @Nullable
    Boolean getCastPermission(RegionAssociable source, ApplicableRegionSet checkSet, SpellTemplate spell);

    boolean inTaggedRegion(RegionAssociable source, ApplicableRegionSet checkSet, Set<String> tags);
}
