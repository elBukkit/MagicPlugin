package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import de.slikey.effectlib.util.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class VelocityAction extends BaseSpellAction
{
    private double livingEntitySpeed;
    private double itemSpeed;
    private double defaultSpeed;
    private double minSpeed;
    private double maxSpeed;
    private int maxSpeedAtElevation;
    private double pushDirection;
    private int yOffset;
    private int exemptionDuration;
    private double maxMagnitude;
    private double maxLength;
    private boolean additive;
    private Vector direction;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        defaultSpeed = parameters.getDouble("speed", 1);
        livingEntitySpeed = parameters.getDouble("living_entity_speed", defaultSpeed);
        itemSpeed = parameters.getDouble("item_speed", defaultSpeed);
        maxSpeedAtElevation = parameters.getInt("max_altitude", 64);
        minSpeed = parameters.getDouble("min_speed", 0);
        maxSpeed = parameters.getDouble("max_speed", 0);
        pushDirection = parameters.getDouble("push", 0);
        yOffset = parameters.getInt("y_offset", 0);
        direction = ConfigurationUtils.getVector(parameters, "direction");
        exemptionDuration = parameters.getInt("exemption_duration", (int)(maxSpeed * 2000));
        maxMagnitude = parameters.getDouble("max_magnitude", 0);
        maxLength = maxMagnitude * maxMagnitude;
        additive = parameters.getBoolean("additive", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity entity = context.getTargetEntity();
        if (entity instanceof Hanging)
        {
            return SpellResult.NO_TARGET;
        }
        Entity mountEntity = entity.getVehicle();
        while (mountEntity != null) {
            entity = mountEntity;
            mountEntity = entity.getVehicle();
        }
        double magnitude = defaultSpeed;
        if (entity instanceof LivingEntity) {
            magnitude = livingEntitySpeed;
        } else if (entity instanceof Item) {
            magnitude = itemSpeed;
        }

        if (minSpeed > 0 || maxSpeed > 0)
        {
            int height = 0;
            Block playerBlock = context.getLocation().getBlock();
            while (height < maxSpeedAtElevation && playerBlock.getType() == Material.AIR)
            {
                playerBlock = playerBlock.getRelative(BlockFace.DOWN);
                height++;
            }

            double heightModifier = maxSpeedAtElevation > 0 ? ((double)height / maxSpeedAtElevation) : 1;
            magnitude = (minSpeed + ((maxSpeed - minSpeed) * heightModifier));
        }

        Vector velocity = direction == null ? context.getDirection() : direction.clone();
        if (pushDirection != 0)
        {
            Location to = entity.getLocation();
            Location from = context.getLocation();

            Vector toVector = new Vector(to.getBlockX(), to.getBlockY(), to.getBlockZ());
            Vector fromVector = new Vector(from.getBlockX(), from.getBlockY(), from.getBlockZ());

            velocity = toVector;
            velocity.subtract(fromVector);
            if (velocity.lengthSquared() < Double.MIN_NORMAL)
            {
                velocity = context.getDirection();
            }

            velocity.normalize().multiply(pushDirection);
        }

        if (context.getLocation().getBlockY() >= 256)
        {
            velocity.setY(0);
        }
        else if (yOffset != 0)
        {
            velocity.setY(velocity.getY() + yOffset);
        }

        velocity.multiply(magnitude);

        if (additive) {
            velocity = entity.getVelocity().clone().add(velocity);
        }

        if (maxLength != 0D && velocity.lengthSquared() > maxLength) {
            velocity = velocity.normalize().multiply(maxMagnitude);
            magnitude = maxMagnitude;
        }

        context.registerVelocity(entity);
        context.registerMoved(entity);

        if (exemptionDuration > 0 && entity instanceof Player) {
            context.getController().addFlightExemption((Player)entity, exemptionDuration);
        }
        context.getMage().sendDebugMessage(ChatColor.AQUA + "Applying velocity of " +
                ChatColor.BLUE + velocity +
                ChatColor.AQUA + " to " + ChatColor.DARK_AQUA + entity.getType() +
                ChatColor.AQUA + " from magnitude of " + ChatColor.BLUE + magnitude
                , 11);

        if(!MathUtils.isFinite(velocity.getX()) ||
                !MathUtils.isFinite(velocity.getY()) ||
                !MathUtils.isFinite(velocity.getZ())) {
            context.getMage().sendDebugMessage(ChatColor.AQUA + "Invalid velocity!" +
                ChatColor.BLUE + velocity +
                ChatColor.AQUA + "Context direction: " + ChatColor.DARK_AQUA + context.getDirection() +
                ChatColor.AQUA + "direction: " + ChatColor.DARK_AQUA + direction);
        } else {
            entity.setVelocity(velocity);
        }

        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);

        parameters.add("push");
        parameters.add("speed");
        parameters.add("living_entity_speed");
        parameters.add("item_speed");
        parameters.add("min_speed");
        parameters.add("max_speed");
        parameters.add("max_altitude");
        parameters.add("exemption_duration");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("push")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("speed") || parameterKey.equals("living_entity_speed")
            || parameterKey.equals("item_speed") || parameterKey.equals("min_speed")
            || parameterKey.equals("max_speed") || parameterKey.equals("max_altitude")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
