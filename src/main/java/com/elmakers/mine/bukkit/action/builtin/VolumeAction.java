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

public class VolumeAction extends CompoundAction
{
	public static final float DEGTORAD = 0.017453293F;
	private static final int DEFAULT_RADIUS	= 2;
    protected boolean autoOrient;
	protected int radius;
    protected int currentRadius;
    protected float centerProbability;
    protected float outerProbability;
    protected int xSize;
    protected int ySize;
    protected int zSize;
    private int dy;
	private int dx;
	private int dz;
	private int xDirection;
	private int zDirection;
    private int startRadius;
	private boolean checked;

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		radius = parameters.getInt("radius", DEFAULT_RADIUS);
        xSize = parameters.getInt("x_size", radius);
        ySize = parameters.getInt("y_size", radius);
        zSize = parameters.getInt("z_size", radius);
        int thickness = parameters.getInt("thickness", 0);
		autoOrient = parameters.getBoolean("orient", false);
		centerProbability = (float)parameters.getDouble("probability", 1);
		outerProbability = (float)parameters.getDouble("probability", 1);
		centerProbability = (float)parameters.getDouble("center_probability", centerProbability);
		outerProbability = (float)parameters.getDouble("outer_probability", outerProbability);
        xSize = (int)(context.getMage().getRadiusMultiplier() * this.xSize);
        ySize = (int)(context.getMage().getRadiusMultiplier() * this.ySize);
        zSize = (int)(context.getMage().getRadiusMultiplier() * this.zSize);
        radius = Math.max(xSize, zSize);
        if (thickness > 0) {
            startRadius = radius - thickness;
        } else {
            startRadius = 0;
        }
	}

	@Override
	public void reset(CastContext context) {
		super.reset(context);
        createActionContext(context);
		currentRadius = startRadius;
		dx = -startRadius;
        dy = -ySize;
		dz = -startRadius;
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
		// This mainly used for planes, that have no z-components.
        // Needs additional testing for volumes, probably. See below.
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

		if (radius < 1 && ySize < 1)
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
				checked = containsPoint(dx, dy, dz);
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
                    // Intentionally flipped axes ..
                    // May need to re-evaluate with volumes
					offset.setX(dx);
					offset.setY(dz);
					offset.setZ(dy);
					offset = rotate(location.getYaw(), location.getPitch(), offset.getX(), offset.getY(), offset.getZ());
				} else {
					offset.setX(dx);
					offset.setY(dy);
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

            dy++;
            if (dy < startRadius && dy >= -startRadius) {
                dy = startRadius;
            }
            if (dy > ySize) {
                dy = -ySize;
                int nextX = dx + xDirection;
                int nextZ = dz + zDirection;
                int endX = Math.min(currentRadius, xSize);
                int endZ = Math.min(currentRadius, zSize);
                if ((xDirection == 0 && zDirection == -1 && nextX == -endX && nextZ == -endZ) || currentRadius == 0) {
                    currentRadius++;
                    dx = -currentRadius;
                    dz = -currentRadius;
                    xDirection = 1;
                    zDirection = 0;
                } else if (nextX > currentRadius || nextZ > endZ || nextX < -endX || nextZ < -endZ) {
                    if (xDirection == 1 && zDirection == 0) {
                        xDirection = 0;
                        zDirection = 1;
                        dz += zDirection;
                    } else if (xDirection == 0 && zDirection == 1) {
                        xDirection = -1;
                        zDirection = 0;
                        dx += xDirection;
                    } else {
                        xDirection = 0;
                        zDirection = -1;
                        dz += zDirection;
                    }
                } else {
                    dx = nextX;
                    dz = nextZ;
                }
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

	protected boolean containsPoint(int x, int y, int z)
	{
		return true;
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
		int volume = (1 + xSize * 2) * (1 + ySize * 2) * (1 + zSize * 2);
		return volume * actions.getActionCount();
	}
}
