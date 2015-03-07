package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import net.sacredlabyrinth.Phaed.PreciousStones.FieldFlag;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.vectors.Field;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

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

	public boolean isPVPAllowed(Player player, Location location) {
		if (!enabled || preciousStones == null || location == null)
			return true;
        if (player == null && PreciousStones.API().isFieldProtectingArea(FieldFlag.ALL, location))
        {
            return false;
        }
        List<Field> fields = PreciousStones.API().getFieldsProtectingArea(FieldFlag.PREVENT_PVP, location);
		return fields.size() == 0;
	}

	public boolean hasBuildPermission(Player player, Block block) {
		boolean allowed = true;
		if (enabled && block != null && preciousStones != null)
        {
            Location location = block.getLocation();
			if (PreciousStones.API().isFieldProtectingArea(FieldFlag.ALL, location))
            {
                if (player == null)
                {
                    return false;
                }
				allowed = allowed && PreciousStones.API().canBreak(player, location);
				allowed = allowed && PreciousStones.API().canPlace(player, location);
				return allowed;
			}
		}
		return allowed;
	}

    public Boolean getCastPermission(Player player, SpellTemplate spell, Location location) {
        Boolean overridePermission = null;
        if (enabled && location != null && preciousStones != null)
        {
            if (PreciousStones.API().isFieldProtectingArea(FieldFlag.ALL, location))
            {
                if (player == null)
                {
                    return null;
                }
                if (PreciousStones.API().canBreak(player, location) && PreciousStones.API().canPlace(player, location))
                {
                    overridePermission = true;
                }
            }
        }
        return overridePermission;
    }
}
