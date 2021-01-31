package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

@Deprecated
public class OrientSpell extends TargetingSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target target = getTarget();
        Entity entity = target.getEntity();
        if (entity == null) {
            return SpellResult.NO_TARGET;
        }

        if (entity != mage.getEntity() && isSuperProtected(entity)) {
            return SpellResult.NO_TARGET;
        }

        Location location = entity.getLocation();
        location.setPitch((float)parameters.getDouble("pitch", 0));
        location.setYaw((float)parameters.getDouble("yaw", 0));
        entity.teleport(location);

        return SpellResult.CAST;
    }
}
