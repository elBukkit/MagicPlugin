package com.elmakers.mine.bukkit.protection;

import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;

import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.field.FieldFlag;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class PreciousStonesManager implements BlockBuildManager, BlockBreakManager, PVPManager {
	private boolean enabled = false;
    private boolean override = true;
	private PreciousStones preciousStones = null;

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

    public void setOverride(boolean override) {
        this.override = override;
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
        if (override && enabled && location != null && preciousStones != null)
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

    @Override
    public boolean hasBreakPermission(Player player, Block block) {
        return hasBuildPermission(player, block);
    }

	public boolean canTarget(Entity source, Entity target ) {
		if (!enabled || target == null)
		{
			return true;
		}

		Player player = (source instanceof Player) ? (Player)source : null;
		if (target instanceof Ageable)
		{
			Field field = preciousStones.getForceFieldManager().getEnabledSourceField(target.getLocation(), FieldFlag.PROTECT_ANIMALS);
			if (field == null) return true;
			if (player != null)
			{
				if (FieldFlag.PROTECT_ANIMALS.applies(field, player))
				{
					return false;
				}
			}
			else
			{
				if (field.hasFlag(FieldFlag.PROTECT_ANIMALS))
				{
					return false;
				}
			}
		}
		else if (target instanceof Villager)
		{
			Field field = preciousStones.getForceFieldManager().getEnabledSourceField(target.getLocation(), FieldFlag.PROTECT_VILLAGERS);
			if (field == null) return true;
			if (player != null)
			{
				if (FieldFlag.PROTECT_VILLAGERS.applies(field, player))
				{
					return false;
				}
			}
			else
			{
				if (field.hasFlag(FieldFlag.PROTECT_VILLAGERS))
				{
					return false;
				}
			}
		}
		else if (target instanceof Monster || target instanceof Golem || target instanceof WaterMob)
		{
			Field field = preciousStones.getForceFieldManager().getEnabledSourceField(target.getLocation(), FieldFlag.PROTECT_MOBS);
			if (field == null) return true;
			if (player != null)
			{
				if (FieldFlag.PROTECT_MOBS.applies(field, player))
				{
					return false;
				}
			}
			else
			{
				if (field.hasFlag(FieldFlag.PROTECT_MOBS))
				{
					return false;
				}
			}
		}

		return true;
	}
}
