package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.EntityAction;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class VelocityAction extends BaseSpellAction implements EntityAction
{
    @Override
    public SpellResult perform(ConfigurationSection parameters, Entity entity) {
        if (entity instanceof Hanging)
        {
            return SpellResult.NO_TARGET;
        }

        double magnitude = parameters.getDouble("speed", 1);
        if (entity instanceof LivingEntity && parameters.contains("living_entity_speed")) {
            magnitude = parameters.getDouble("living_entity_speed");
        } else if (entity instanceof Item && parameters.contains("item_speed")) {
            magnitude = parameters.getDouble("item_speed");
        }

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

        // TODO: Fix push = -1? Seems to toggle or something?
        Vector velocity = getDirection();
        if (parameters.contains("push"))
        {
            double direction = parameters.getDouble("push");
            Location to = entity.getLocation();
            Location from = getLocation();
            Vector toVector = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
            Vector fromVector = new Vector(from.getBlockX(), from.getBlockY(), from.getBlockZ());

            velocity = toVector;
            velocity.subtract(fromVector);
            velocity.normalize().multiply(direction);
        }

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

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);

        parameters.add("push");
        parameters.add("speed");
        parameters.add("living_entity_speed");
        parameters.add("item_speed");
        parameters.add("min_speed");
        parameters.add("max_speed");
        parameters.add("max_altitude");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("push")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("speed") || parameterKey.equals("living_entity_speed")
            || parameterKey.equals("item_speed") || parameterKey.equals("min_speed")
            || parameterKey.equals("max_speed") || parameterKey.equals("max_altitude")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }
}
