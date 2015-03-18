package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;

public class DiscAction extends CompoundAction
{
	public static final float DEGTORAD = 0.017453293F;
	private static final int DEFAULT_RADIUS	= 2;
	private boolean autoOrient;
	private int radius;
	private int currentRadius;
	private float centerProbability;
	private float outerProbability;
	private int dx;
	private int dz;
	private int xDirection;
	private int zDirection;
	private boolean checked;

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		radius = parameters.getInt("radius", DEFAULT_RADIUS);
		autoOrient = parameters.getBoolean("orient", false);
		centerProbability = (float)parameters.getDouble("probability", 1);
		outerProbability = (float)parameters.getDouble("probability", 1);
		centerProbability = (float)parameters.getDouble("center_probability", centerProbability);
		outerProbability = (float)parameters.getDouble("outer_probability", outerProbability);
		radius = (int)(context.getMage().getRadiusMultiplier() * this.radius);
	}

	@Override
	public void reset(CastContext context) {
		super.reset(context);
		currentRadius = 0;
		dx = 0;
		dz = 0;
		xDirection = 1;
		zDirection = 0;
		checked = false;
	}

	public static Vector rotate(float yaw, float pitch, double x, double y, double z){
		float angle;
		angle = -yaw * DEGTORAD;
		double sinYaw = Math.sin(angle);
		double cosYaw = Math.cos(angle);
		angle = pitch * DEGTORAD;
		double sinPitch = Math.sin(angle);
		double cosPitch = Math.cos(angle);

		// X-axis rotation
		// The way the plane is built, we don't need to worry about Z-axis rotation
		double pitchedX = x;
		double pitchedY = y * cosPitch - z * sinPitch;
		double pitchedZ = y * sinPitch + z * cosPitch;

		// Z-axis rotation
		// Here for posterity. If there were any z-components to this shape
		// we might need to use this.
		// pitchedX = x * cosPitch - y * sinPitch;
		// pitchedY = x * sinPitch + y * cosPitch;

		// Rotate around Y
		double finalX = pitchedX * cosYaw + pitchedZ * sinYaw;
		double finalY = pitchedY; // y
		double finalZ = -pitchedX * sinYaw + pitchedZ * cosYaw;

		return new Vector(finalX + 0.5, finalY + 0.5, finalZ + 0.5);
	}

	@Override
	public SpellResult perform(CastContext context) {
		Block block = context.getTargetBlock();

		if (radius < 1)
		{
			if (!checked && centerProbability < 1 && context.getRandom().nextDouble() <= centerProbability)
			{
				return SpellResult.NO_ACTION;
			}
			checked = true;
			actionContext.setTargetLocation(context.findBlockUnder(block).getLocation());
			return performActions(actionContext);
		}

		Location location = context.getLocation();
		SpellResult result = SpellResult.NO_ACTION;
		Vector offset = new Vector();
		while (currentRadius <= radius)
		{
			if (!checked) {
				checked = isInCircle(dx, dz, radius);
				float probability = centerProbability;
				if (centerProbability != outerProbability) {
					float weight = Math.abs((float) dx + dz) / ((float) radius * 2);
					probability = RandomUtils.lerp(centerProbability, outerProbability, weight);
				}
				checked = checked && (probability >= 1 || context.getRandom().nextDouble() <= probability);
			}
			if (checked)
			{
				if (autoOrient) {
					offset.setX(dx);
					offset.setY(dz);
					offset.setZ(0);
					offset = rotate(location.getYaw(), location.getPitch(), offset.getX(), offset.getY(), offset.getZ());
				} else {
					offset.setX(dx);
					offset.setY(0);
					offset.setZ(dz);
				}
				Block targetBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
				actionContext.setTargetLocation(targetBlock.getLocation());

				SpellResult actionResult = performActions(actionContext);
				result = result.min(actionResult);
				if (actionResult == SpellResult.PENDING) {
					break;
				}
			}
			else
			{
				skippedActions(context);
			}
			int nextX = dx + xDirection;
			int nextZ = dz + zDirection;
			if ((xDirection == 0 && zDirection == -1 && nextX == -currentRadius && nextZ == -currentRadius) || currentRadius == 0) {
				currentRadius++;
				dx = -currentRadius;
				dz = -currentRadius;
				xDirection = 1;
				zDirection = 0;
			}
			else if (nextX > currentRadius || nextZ > currentRadius || nextX < -currentRadius || nextZ < -currentRadius) {
				if (xDirection == 1 && zDirection == 0)  {
					xDirection = 0;
					zDirection = 1;
					dz += zDirection;
				} else if (xDirection == 0 && zDirection == 1)  {
					xDirection = -1;
					zDirection = 0;
					dx += xDirection;
				} else  {
					xDirection = 0;
					zDirection = -1;
					dz += zDirection;
				}
			} else {
				dx = nextX;
				dz = nextZ;
			}
			checked = false;
			super.reset(context);
		}

		return result;
	}

	@Override
	public boolean requiresTarget() {
		return true;
	}

	protected boolean isInCircle(int x, int z, int R)
	{
		return ((x * x) +  (z * z) - (R * R)) <= 0;
	}

	@Override
	public void getParameterNames(Collection<String> parameters)
	{
		super.getParameterNames(parameters);
		parameters.add("radius");
		parameters.add("probability");
		parameters.add("center_probability");
		parameters.add("outer_probability");
	}

	@Override
	public void getParameterOptions(Collection<String> examples, String parameterKey)
	{
		super.getParameterOptions(examples, parameterKey);

		if (parameterKey.equals("radius")) {
			examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
		} else if (parameterKey.equals("probability") || parameterKey.equals("center_probability") || parameterKey.equals("outer_probability")) {
			examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
		}
	}

	@Override
	public int getActionCount() {
		int diameter = 1 + radius * 2;
		return diameter * diameter * actions.getActionCount();
	}
}
