package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.Spell;
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
	protected int radiusSquared;
    protected int currentRadius;
    protected float centerProbability;
    protected float outerProbability;
    protected int xSize;
    protected int ySize;
    protected int zSize;
	protected int thickness;
    private int dy;
	private int dx;
	private int dz;
	private int xDirection;
	private int zDirection;
    private int startRadius;
	private boolean checked;
	private Vector min;
	private Vector max;
	private VolumeType volumeType;

	private enum VolumeType {
		SPIRAL,
		YZX,
		YXZ
	}

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		radius = parameters.getInt("radius", DEFAULT_RADIUS);
        xSize = parameters.getInt("x_size", radius);
        ySize = parameters.getInt("y_size", radius);
        zSize = parameters.getInt("z_size", radius);
        thickness = parameters.getInt("thickness", 0);
		autoOrient = parameters.getBoolean("orient", false);
		centerProbability = (float)parameters.getDouble("probability", 1);
		outerProbability = (float)parameters.getDouble("probability", 1);
		centerProbability = (float)parameters.getDouble("center_probability", centerProbability);
		outerProbability = (float)parameters.getDouble("outer_probability", outerProbability);
		String typeString = parameters.getString("volume_type");
		if (typeString != null) {
			try {
				volumeType = VolumeType.valueOf(typeString.toUpperCase());
			} catch (Exception ex) {
				volumeType = VolumeType.SPIRAL;
			}
 		} else {
			volumeType = VolumeType.SPIRAL;
		}
		calculateSize(context);
	}

	protected void calculateSize(CastContext context) {
		if (parameters.getBoolean("use_brush_size", false)) {
			MaterialBrush brush = context.getBrush();
			if (!brush.isReady()) {
				long timeout = System.currentTimeMillis() + 10000;
				while (System.currentTimeMillis() < timeout) {
					try {
						Thread.sleep(500);
						if (brush.isReady()) {
							break;
						}
					} catch (InterruptedException ex) {
						break;
					}
				}
			}
			if (brush.isReady()) {
				Vector bounds = brush.getSize();
				xSize = (int)Math.ceil(bounds.getX() / 2) + 1;
				ySize = (int)Math.ceil(bounds.getY() / 2) + 1;
				zSize = (int)Math.ceil(bounds.getZ() / 2) + 1;
				if (volumeType == VolumeType.SPIRAL) {
					xSize = Math.max(xSize, zSize);
					zSize = Math.max(xSize, zSize);
				}
			}
		} else {
			xSize = (int) (context.getMage().getRadiusMultiplier() * this.xSize);
			ySize = (int) (context.getMage().getRadiusMultiplier() * this.ySize);
			zSize = (int) (context.getMage().getRadiusMultiplier() * this.zSize);
		}
		if (volumeType == VolumeType.SPIRAL && xSize != zSize) {
			volumeType = VolumeType.YZX;
		}
		if (volumeType != VolumeType.SPIRAL) {
			min = new Vector(-xSize, -ySize, -zSize);
			max = new Vector(xSize, ySize, zSize);
		}
		radius = Math.max(xSize, zSize);
		radiusSquared = radius * radius;
		startRadius = getStartRadius();
	}

	protected int getStartRadius() {
		return 0;
	}

	@Override
	public void reset(CastContext context) {
		super.reset(context);
        createActionContext(context);
		if (volumeType == VolumeType.SPIRAL) {
			currentRadius = startRadius;
			dx = -Math.min(startRadius, xSize);
			dy = -ySize;
			dz = -Math.min(startRadius, zSize);
			xDirection = 1;
			zDirection = 0;
		} else {
			dx = min.getBlockX();
			dy = min.getBlockY();
			dz = min.getBlockZ();
		}
		checked = false;
        MaterialBrush brush = context.getBrush();
        brush.setTarget(context.getTargetLocation());
	}

	public static Vector rotate(float yaw, float pitch, double x, double y, double z){
		float angle;
		angle = -yaw * DEGTORAD;
		double sinYaw = Math.sin(angle);
		double cosYaw = Math.cos(angle);
		angle = pitch * DEGTORAD;
		double sinPitch = Math.sin(angle);
		double cosPitch = Math.cos(angle);

		double pitchedZ = y * sinPitch + z * cosPitch;

		double finalX = x * cosYaw + pitchedZ * sinYaw;
		double finalY = y * cosPitch - z * sinPitch;
		double finalZ = -x * sinYaw + pitchedZ * cosYaw;

		return new Vector(finalX + 0.5, finalY + 0.5, finalZ + 0.5);
	}

	protected SpellResult checkPoint(CastContext context) {
		SpellResult result = SpellResult.NO_ACTION;
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
			Location location = context.getLocation();
			Block block = context.getTargetBlock();
			Vector offset = new Vector();
			if (autoOrient) {
				offset.setX(dx);
				offset.setY(dy);
				offset.setZ(dz);
				Block originalBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
				actionContext.setTargetSourceLocation(originalBlock.getLocation());
				offset = rotate(location.getYaw(), location.getPitch(), offset.getX(), offset.getY(), offset.getZ());
			} else {
				offset.setX(dx);
				offset.setY(dy);
				offset.setZ(dz);
			}
			Block targetBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
			actionContext.setTargetLocation(targetBlock.getLocation());

			SpellResult actionResult = super.perform(actionContext);
			result = result.min(actionResult);
		}
		else
		{
			skippedActions(context);
		}

		return result;
	}

	protected SpellResult performYZX(CastContext context) {
		SpellResult result = SpellResult.NO_ACTION;
		while (dx <= max.getBlockX() && dy <= max.getBlockY() && dz <= max.getBlockZ())
		{
			result = checkPoint(context);
			if (result == SpellResult.PENDING) {
				break;
			}

			dy++;
			if (dy > max.getBlockY()) {
				dy = min.getBlockY();
				dz++;
				if (dz > max.getBlockZ()) {
					dz = min.getBlockZ();
					dx++;
				}
			}
			checked = false;
			super.reset(context);
		}

		return result;
	}

	protected SpellResult performYXZ(CastContext context) {
		SpellResult result = SpellResult.NO_ACTION;
		while (dx <= max.getBlockX() && dy <= max.getBlockY() && dz <= max.getBlockZ())
		{
			result = checkPoint(context);
			if (result == SpellResult.PENDING) {
				break;
			}

			dy++;
			if (dy > max.getBlockY()) {
				dy = min.getBlockY();
				dx++;
				if (dx > max.getBlockX()) {
					dx = min.getBlockX();
					dz++;
				}
			}
			checked = false;
			super.reset(context);
		}

		return result;
	}

	protected SpellResult performSpiral(CastContext context) {
		SpellResult result = SpellResult.NO_ACTION;
		while (currentRadius <= radius)
		{
			result = checkPoint(context);
			if (result == SpellResult.PENDING) {
				break;
			}

			dy++;
			if (dy > ySize) {
				dy = -ySize;
				int nextX = dx + xDirection;
				int nextZ = dz + zDirection;
				int endX = Math.min(currentRadius, xSize);
				int endZ = Math.min(currentRadius, zSize);
				if ((xDirection == 0 && zDirection == -1 && nextX <= -endX && nextZ <= -endZ) || currentRadius == 0) {
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

	public SpellResult performSingle(CastContext context) {
		if (!checked && centerProbability < 1 && context.getRandom().nextDouble() <= centerProbability)
		{
			return SpellResult.NO_ACTION;
		}
		Block block = context.getTargetBlock();
		checked = true;
		actionContext.setTargetLocation(block.getLocation());
		return super.perform(actionContext);
	}

	@Override
	public SpellResult perform(CastContext context) {
		if (radius < 1 && ySize < 1)
		{
			return performSingle(context);
		}

		SpellResult result = SpellResult.NO_ACTION;
		switch (volumeType) {
			case SPIRAL:
				result = performSpiral(context);
				break;
			case YZX:
				result = performYZX(context);
				break;
			case YXZ:
				result = performYXZ(context);
				break;
		}

		return result;
	}

	@Override
	public boolean requiresTarget() {
		return true;
	}

	protected boolean containsPoint(int x, int y, int z)
	{
		return thickness == 0 || x > radius - thickness || y > radius - thickness || z > radius - thickness;
	}

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters)
	{
		super.getParameterNames(spell, parameters);
		parameters.add("radius");
		parameters.add("probability");
		parameters.add("center_probability");
		parameters.add("outer_probability");
		parameters.add("use_brush_size");
		parameters.add("thickness");
		parameters.add("orient");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
	{
		super.getParameterOptions(spell, parameterKey, examples);

		if (parameterKey.equals("radius") || parameterKey.equals("thickness")) {
			examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
		} else if (parameterKey.equals("probability") || parameterKey.equals("center_probability") || parameterKey.equals("outer_probability")) {
			examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_PERCENTAGES));
		} else if (parameterKey.equals("orient") || parameterKey.equals("use_brush_size")) {
			examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
		}
	}

	@Override
	public int getActionCount() {
		int volume = (1 + xSize * 2) * (1 + ySize * 2) * (1 + zSize * 2);
		return volume * actions.getActionCount();
	}
}
