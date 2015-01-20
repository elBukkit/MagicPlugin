package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class VelocityAction extends BaseSpellAction implements EntityAction
{
    @Override
    public SpellResult perform(ConfigurationSection parameters, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return SpellResult.LIVING_ENTITY_REQUIRED;
        }

        double magnitude = parameters.getDouble("speed", 1);

        if (parameters.contains("min_speed") || parameters.contains("max_speed"))
        {
            int maxSpeedAtElevation = parameters.getInt("max_altitude", 64);
            double minMagnitude = parameters.getDouble("min_speed", magnitude);
            double maxMagnitude = parameters.getDouble("max_speed", magnitude);

            int height = 0;
            Block playerBlock = getLocation().getBlock();
            while (height < maxSpeedAtElevation && playerBlock.getType() == Material.AIR)
            {
                playerBlock = playerBlock.getRelative(BlockFace.DOWN);
                height++;
            }

            double heightModifier = maxSpeedAtElevation > 0 ? ((double)height / maxSpeedAtElevation) : 1;
            magnitude = (minMagnitude + ((maxMagnitude - minMagnitude) * heightModifier));
        }

        Vector velocity = getDirection();
        if (getLocation().getBlockY() >= 256)
        {
            velocity.setY(0);
        }

        velocity.multiply(magnitude);

        registerVelocity(entity);
        registerMoved(entity);
        entity.setVelocity(velocity);

        return SpellResult.CAST;
    }
}
