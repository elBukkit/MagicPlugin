package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.RegionAssociable;

import java.util.Set;

public interface WorldGuardFlags {
    public String getDestructible(RegionAssociable source, ApplicableRegionSet checkSet);
    public String getReflective(RegionAssociable source, ApplicableRegionSet checkSet);
    public Set<String> getSpellOverrides(RegionAssociable source, ApplicableRegionSet checkSet);
    public Boolean getWandPermission(RegionAssociable source, ApplicableRegionSet checkSet, Wand wand);
    public Boolean getCastPermission(RegionAssociable source, ApplicableRegionSet checkSet, SpellTemplate spell);
}
