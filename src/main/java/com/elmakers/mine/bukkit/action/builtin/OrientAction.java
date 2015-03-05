package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.Arrays;
import java.util.Collection;

public class OrientAction extends BaseSpellAction implements EntityAction {
    @Override
    public SpellResult perform(ConfigurationSection parameters, Entity target) {
        Mage mage = getMage();
        Entity entity = mage.getEntity();
        if (entity == null) {
            return SpellResult.ENTITY_REQUIRED;
        }

        registerMoved(entity);
        Location location = entity.getLocation();
        if (parameters.contains("pitch") || parameters.contains("yaw"))
        {
            float pitch = location.getPitch();
            float yaw = location.getPitch();
            location.setPitch((float)parameters.getDouble("pitch", pitch));
            location.setYaw((float)parameters.getDouble("yaw", yaw));
        }
        else
        {
            Location direction = target.getLocation().subtract(location);
            location.setDirection(direction.toVector());
        }
        entity.teleport(location);

        return SpellResult.CAST;
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("pitch");
        parameters.add("yaw");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("pitch") || parameterKey.equals("yaw")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }
}
