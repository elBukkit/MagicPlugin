package com.elmakers.mine.bukkit.protection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlockOwner;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TownyManager {
	private boolean enabled = false;
	private Towny towny = null;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled && towny != null;
	}

	public void initialize(Plugin plugin) {
		if (enabled) {
			try {
				Plugin townyPlugin = plugin.getServer().getPluginManager()
						.getPlugin("Towny");
				if (townyPlugin instanceof Towny) {
					towny = (Towny) townyPlugin;
				}
			} catch (Throwable ex) {
			}

			if (towny == null) {
				plugin.getLogger()
						.info("Towny not found, region protection and pvp checks will not be used.");
			} else {
				plugin.getLogger()
						.info("Towny found, will respect build permissions for construction spells");
			}
		} else {
			plugin.getLogger()
					.info("Towny manager disabled, region protection and pvp checks will not be used.");
			towny = null;
		}
	}

	public boolean isPVPAllowed(Location location) {
		if (!enabled || towny == null || location == null)
			return true;
		return false;
	}

	public boolean hasBuildPermission(Player player, Block block) {
		if (enabled && block != null && towny != null) {
			if (TownyUniverse.isWilderness(block)) {
				return true;
			}
			try {
				TownBlockOwner owner = TownyUniverse.getDataSource()
						.getResident(player.getName());
				return TownyUniverse.getTownBlock(block.getLocation()).isOwner(
						owner);
			} catch (NotRegisteredException e) {
				return false;
			}
		}
		return true;
	}
}
