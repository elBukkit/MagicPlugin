package com.elmakers.mine.bukkit.protection;

import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PreciousStonesManager {
	private boolean enabled = false;
	private PreciousStones preciousStones = null;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled && preciousStones != null;
	}

	public void initialize(Plugin plugin) {
		if (enabled) {
			try {
				Plugin psPlugin = plugin.getServer().getPluginManager()
						.getPlugin("PreciousStones");
				if (psPlugin instanceof PreciousStones) {
					preciousStones = (PreciousStones) psPlugin;
				}
			} catch (Throwable ex) {
			}

			if (preciousStones == null) {
				plugin.getLogger()
						.info("PreciousStones not found, region protection and pvp checks will not be used.");
			} else {
				plugin.getLogger()
						.info("PreciousStones found, will respect build permissions for construction spells");
			}
		} else {
			plugin.getLogger()
					.info("PreciousStones manager disabled, region protection and pvp checks will not be used.");
			preciousStones = null;
		}
	}

	public boolean isPVPAllowed(Location location) {
		if (!enabled || preciousStones == null || location == null)
			return true;
		return !PreciousStones.API().isFieldProtectingArea(
				FieldFlag.PREVENT_PVP, location);
	}

	public boolean hasBuildPermission(Player player, Block block) {
		boolean allowed = true;
		if (enabled && block != null && preciousStones != null)
        {
			if (PreciousStones.API().isFieldProtectingArea(FieldFlag.ALL, block.getLocation()))
            {
                if (player == null)
                {
                    return false;
                }
				allowed = allowed && PreciousStones.API().canBreak(player, block.getLocation());
				allowed = allowed && PreciousStones.API().canPlace(player, block.getLocation());
				return allowed;
			}
		}
		return allowed;
	}
}
