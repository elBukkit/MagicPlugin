package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseProjectileAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class ThrowBlockAction extends BaseProjectileAction
{
    private double speedMin;
    private double speedMax;
    private float fallDamage;
    private int maxDamage;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double itemSpeed = parameters.getDouble("speed", 0.6f);
        speedMin = parameters.getDouble("speed_min", itemSpeed);
        speedMax = parameters.getDouble("speed_max", itemSpeed);
        int damage = parameters.getInt("damage", 0);
        fallDamage = (float)parameters.getDouble("fall_damage", damage);
        maxDamage = parameters.getInt("max_damage", damage);
    }

	@Override
	public SpellResult start(CastContext context)
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

        location = context.getEyeLocation();
        tracking = context.getWorld().spawnFallingBlock(location, material, data);

        if (tracking == null || !(tracking instanceof FallingBlock))
        {
            tracking = null;
            return SpellResult.FAIL;
        }
        FallingBlock falling = (FallingBlock)tracking;
        playProjectileEffects(context);
        context.registerForUndo(tracking);
        falling.setDropItem(false);
        tracking.setVelocity(direction);
        if (maxDamage > 0 && fallDamage > 0) {
            CompatibilityUtils.setFallingBlockDamage(falling, fallDamage, maxDamage);
        }

        return checkTracking(context);
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
        parameters.add("damage");
        parameters.add("max_damage");
        parameters.add("fall_damage");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("speed") || parameterKey.equals("speed_max") || parameterKey.equals("speed_min")
            || parameterKey.equals("damage") || parameterKey.equals("max_damage") || parameterKey.equals("fall_damage")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
