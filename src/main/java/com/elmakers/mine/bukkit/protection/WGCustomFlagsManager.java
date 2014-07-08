package com.elmakers.mine.bukkit.protection;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;

import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
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

    public boolean canCast(ApplicableRegionSet checkSet, String spellKey) {
        Set<String> allowed = checkSet.getFlag(ALLOWED_SPELLS);
        if (allowed != null && (allowed.contains("*") || allowed.contains(spellKey))) return true;
        Set<String> blocked = checkSet.getFlag(BLOCKED_SPELLS);
        if (blocked != null && (blocked.contains("*") || blocked.contains(spellKey))) return false;

        return true;
    }
}
