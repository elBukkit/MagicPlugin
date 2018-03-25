package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class CommitSpell extends TargetingSpell
{
    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        // You should really use /magic commit for this at this point.
        String typeString = parameters.getString("type", "");
        if (typeString.equalsIgnoreCase("all")) {
            return controller.commitAll() ? SpellResult.CAST : SpellResult.FAIL;
        }

        Target target = getTarget();
        Entity targetEntity = target.getEntity();
        if (targetEntity instanceof Player) {
            Mage mage = controller.getMage((Player) targetEntity);
            return mage.commit() ? SpellResult.CAST : SpellResult.FAIL;
        }

        return mage.commit() ? SpellResult.CAST : SpellResult.FAIL;
    }
}
