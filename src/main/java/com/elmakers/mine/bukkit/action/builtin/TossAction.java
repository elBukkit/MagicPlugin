package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.ActionHandler;
import com.elmakers.mine.bukkit.action.DelayedCompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class TossAction extends DelayedCompoundAction
{
    private double speed;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        speed = (float)parameters.getDouble("speed", 0.6f);
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
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("speed");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("speed")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        }
    }
}
