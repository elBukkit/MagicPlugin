package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.TargetingSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class MountSpell extends TargetingSpell {

    @Override
    public SpellResult onCast(ConfigurationSection parameters) {
        LivingEntity player = mage.getLivingEntity();
        if (player == null) {
            return SpellResult.LIVING_ENTITY_REQUIRED;
        }

        // Make it so this spell can be used to get someone off of you
        if (isLookingUp()) {
            player.eject();
        }

        Entity current = player.getVehicle();
        if (current != null) {
            current.eject();
        }
        Entity targetEntity = getTarget().getEntity();
        if (targetEntity == null) {
            return SpellResult.NO_TARGET;
        }

        CompatibilityLib.getInstance().setPassenger(targetEntity, player);

        return SpellResult.CAST;
    }

}
