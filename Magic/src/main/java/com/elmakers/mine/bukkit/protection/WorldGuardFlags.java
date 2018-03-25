package com.elmakers.mine.bukkit.protection;

import java.util.Set;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;

public interface WorldGuardFlags {
    @Nullable
    public String getDestructible(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    public String getReflective(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    public Set<String> getSpellOverrides(RegionAssociable source, ApplicableRegionSet checkSet);

    @Nullable
    public Boolean getWandPermission(RegionAssociable source, ApplicableRegionSet checkSet, Wand wand);

    @Nullable
    public Boolean getCastPermission(RegionAssociable source, ApplicableRegionSet checkSet, SpellTemplate spell);
}
