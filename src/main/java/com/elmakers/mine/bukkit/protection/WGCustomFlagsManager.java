package com.elmakers.mine.bukkit.protection;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.association.Associables;
import com.sk89q.worldguard.protection.association.RegionAssociable;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;

public class WGCustomFlagsManager {

    private final WGCustomFlagsPlugin customFlags;

    public static SetFlag<String> ALLOWED_SPELLS = new SetFlag<String>("allowed-spells", new StringFlag(null));
    public static SetFlag<String> BLOCKED_SPELLS = new SetFlag<String>("blocked-spells", new StringFlag(null));

    public WGCustomFlagsManager(Plugin wgCustomFlags) {
        customFlags = (WGCustomFlagsPlugin)wgCustomFlags;
        customFlags.addCustomFlag(ALLOWED_SPELLS);
        customFlags.addCustomFlag(BLOCKED_SPELLS);
    }

    public boolean canCast(RegionAssociable source, ApplicableRegionSet checkSet, String spellKey) {
        Set<String> allowed = checkSet.queryValue(source, ALLOWED_SPELLS);
        if (allowed != null && (allowed.contains("*") || allowed.contains(spellKey))) return true;
        Set<String> blocked = checkSet.queryValue(source, BLOCKED_SPELLS);
        if (blocked != null && (blocked.contains("*") || blocked.contains(spellKey))) return false;

        return true;
    }

    public boolean canOverrideCast(RegionAssociable source, ApplicableRegionSet checkSet, String spellKey) {
        Set<String> allowed = checkSet.queryValue(source, ALLOWED_SPELLS);
        return (allowed != null && (allowed.contains("*") || allowed.contains(spellKey)));
    }
}
