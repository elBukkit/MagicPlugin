package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.TriggeredCompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class ThrowBlockAction extends TriggeredCompoundAction
{
    private double speedMin;
    private double speedMax;
    private boolean setTarget;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double itemSpeed = parameters.getDouble("speed", 0.6f);
        speedMin = parameters.getDouble("speed_min", itemSpeed);
        speedMax = parameters.getDouble("speed_max", itemSpeed);
        setTarget = parameters.getBoolean("set_target", false);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
		Location location = context.getLocation();
		if (!context.hasBuildPermission(location.getBlock())) {
			return SpellResult.INSUFFICIENT_PERMISSION;
		}

		location.setY(location.getY() - 1);
		MaterialBrush buildWith = context.getBrush();
		buildWith.setTarget(location);

		Material material = buildWith.getMaterial();
		byte data = buildWith.getBlockData();

		Vector direction = context.getDirection();
        double speed = context.getRandom().nextDouble() * (speedMax - speedMin) + speedMin;
        direction.normalize().multiply(speed);
		Vector up = new Vector(0, 1, 0);
		Vector perp = new Vector();
		perp.copy(direction);
		perp.crossProduct(up);

        FallingBlock block = null;
        location = context.getEyeLocation();
        block = context.getWorld().spawnFallingBlock(location, material, data);

        if (block == null)
        {
            return SpellResult.FAIL;
        }
        if (setTarget)
        {
            context.setTargetEntity(block);
        }
        Collection<EffectPlayer> projectileEffects = context.getEffects("projectile");
        for (EffectPlayer effectPlayer : projectileEffects) {
            effectPlayer.start(block.getLocation(), block, null, null);
        }
        ActionHandler.setEffects(block, context, "hit");
        context.registerForUndo(block);
        block.setDropItem(false);
        block.setVelocity(direction);
        ActionHandler.setActions(block, actions, context, parameters, "indirect_player_message");

		return SpellResult.CAST;
	}

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean usesBrush() {
        return true;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("speed");
        parameters.add("speed_min");
        parameters.add("speed_max");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("speed") || parameterKey.equals("speed_max") || parameterKey.equals("speed_min")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
