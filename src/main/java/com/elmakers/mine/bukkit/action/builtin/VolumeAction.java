package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.RandomUtils;
import org.bukkit.ChatColor;
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
	protected double radius;
	protected double radiusSquared;
    protected int currentRadius;
    protected float centerProbability;
    protected float outerProbability;
    protected double xSize;
    protected double ySize;
    protected double zSize;
	protected int xSizeCeil;
	protected int ySizeCeil;
	protected int zSizeCeil;
	protected double thickness;
	protected int yStart;
	protected int yEnd;
	private int xOffset;
	private int zOffset;
    private int dy;
	private int dx;
	private int dz;
	private int xDirection;
	private int zDirection;
    private int startRadius;
	private Vector min;
	private Vector max;
	private VolumeType volumeType;
	private boolean useBrushSize;
	private boolean centerY;
	private boolean centerX;
	private boolean centerZ;

	private enum VolumeType {
		SPIRAL,
		YZX,
		YXZ
	}

	@Override
	public void prepare(CastContext context, ConfigurationSection parameters) {
		super.prepare(context, parameters);
		radius = parameters.getDouble("radius", DEFAULT_RADIUS);
        xSize = parameters.getDouble("x_size", radius);
        ySize = parameters.getDouble("y_size", radius);
        zSize = parameters.getDouble("z_size", radius);
		centerY = parameters.getBoolean("center_y", true);
		centerX = parameters.getBoolean("center_x", true);
		centerZ = parameters.getBoolean("center_z", true);
        thickness = parameters.getDouble("thickness", 0);
		autoOrient = parameters.getBoolean("orient", false);
		centerProbability = (float)parameters.getDouble("probability", 1);
		outerProbability = (float)parameters.getDouble("probability", 1);
		centerProbability = (float)parameters.getDouble("center_probability", centerProbability);
		outerProbability = (float)parameters.getDouble("outer_probability", outerProbability);
		useBrushSize = parameters.getBoolean("use_brush_size", false);
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
	}

	protected boolean calculateSize(CastContext context) {
		if (useBrushSize) {
			MaterialBrush brush = context.getBrush();
			if (!brush.isReady()) {
				return false;
			}
			Vector bounds = brush.getSize();
			xSize = (int)Math.ceil(bounds.getX() / 2) + 1;
			ySize = (int)Math.ceil(bounds.getY() / 2) + 1;
			zSize = (int)Math.ceil(bounds.getZ() / 2) + 1;
			if (volumeType == VolumeType.SPIRAL) {
				xSize = Math.max(xSize, zSize);
				zSize = Math.max(xSize, zSize);
			}
			centerY = false;
			context.getMage().sendDebugMessage(ChatColor.GREEN + "Brush Size: " + ChatColor.GRAY + xSize + "," + ySize + "," + zSize, 2);
		} else {
			xSize = context.getMage().getRadiusMultiplier() * this.xSize;
			ySize = context.getMage().getRadiusMultiplier() * this.ySize;
			zSize = context.getMage().getRadiusMultiplier() * this.zSize;
		}
		if (volumeType == VolumeType.SPIRAL && xSize != zSize) {
			volumeType = VolumeType.YZX;
		}

		xSizeCeil = (int)Math.ceil(xSize);
		ySizeCeil = (int)Math.ceil(ySize);
		zSizeCeil = (int)Math.ceil(zSize);
		if (centerY) {
			yStart = -ySizeCeil;
			yEnd = ySizeCeil;
		} else {
			yStart = 0;
			yEnd = ySizeCeil * 2;
		}
		if (!centerX) {
			xOffset = xSizeCeil;
		} else {
			xOffset = 0;
		}
		if (!centerZ) {
			zOffset = zSizeCeil;
		} else {
			zOffset = 0;
		}

		if (volumeType != VolumeType.SPIRAL) {
			min = new Vector(-xSizeCeil, yStart, -zSizeCeil);
			max = new Vector(xSizeCeil, yEnd, zSizeCeil);
		}

		radius = Math.max(xSize, zSize);
		radiusSquared = radius * radius;
		startRadius = getStartRadius();

		return true;
	}

	protected int getStartRadius() {
		return 0;
	}

	@Override
	public void reset(CastContext context) {
		super.reset(context);
        MaterialBrush brush = context.getBrush();
        brush.setTarget(context.getTargetLocation());
	}

	protected void resetCounters() {
		if (volumeType == VolumeType.SPIRAL) {
			currentRadius = startRadius;
			dx = -Math.min(startRadius, xSizeCeil);
			dy = yStart;
			dz = -Math.min(startRadius, zSizeCeil);
			xDirection = 1;
			zDirection = 0;
		} else {
			dx = min.getBlockX();
			dy = min.getBlockY();
			dz = min.getBlockZ();
		}
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

		double finalX = x * cosYaw + pitchedZ * sinYaw + 0.5;
		double finalY = y * cosPitch - z * sinPitch + 0.5;
		double finalZ = -x * sinYaw + pitchedZ * cosYaw + 0.5;

		return new Vector(finalX, finalY, finalZ);
	}

	protected boolean nextYZX(CastContext context) {
		dy++;
		if (dy > max.getBlockY()) {
			dy = min.getBlockY();
			dz++;
			if (dz > max.getBlockZ()) {
				dz = min.getBlockZ();
				dx++;
			}
		}
		return (dx <= max.getBlockX() && dy <= max.getBlockY() && dz <= max.getBlockZ());
	}

	protected boolean nextYXZ(CastContext context) {
		dy++;
		if (dy > max.getBlockY()) {
			dy = min.getBlockY();
			dx++;
			if (dx > max.getBlockX()) {
				dx = min.getBlockX();
				dz++;
			}
		}
		return (dx <= max.getBlockX() && dy <= max.getBlockY() && dz <= max.getBlockZ());
	}

	protected boolean nextSpiral(CastContext context) {
		dy++;
		if (dy > yEnd) {
			dy = yStart;
			int nextX = dx + xDirection;
			int nextZ = dz + zDirection;
			int endX = Math.min(currentRadius, xSizeCeil);
			int endZ = Math.min(currentRadius, zSizeCeil);
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
		return currentRadius <= radius;
	}

	@Override
	public SpellResult start(CastContext context) {
		if (!calculateSize(context)) {
			return SpellResult.PENDING;
		}
		resetCounters();
		return SpellResult.NO_ACTION;
	}

	@Override
	public boolean next(CastContext context) {
		if (radius < 1 && ySize < 1) {
			return false;
		}

		boolean result = false;
		switch (volumeType) {
			case SPIRAL:
				result = nextSpiral(context);
				break;
			case YZX:
				result = nextYZX(context);
				break;
			case YXZ:
				result = nextYXZ(context);
				break;
		}
		return result;
	}

	@Override
	public SpellResult step(CastContext context) {
		SpellResult result = SpellResult.NO_ACTION;
		boolean singleBlock = (radius < 1 && ySize < 1);
		boolean validBlock = singleBlock ? true : containsPoint(dx, dy, dz);
		float probability = centerProbability;
		if (!singleBlock && centerProbability != outerProbability) {
			float weight = Math.abs((float) dx + dz) / ((float) radius * 2);
			probability = RandomUtils.lerp(centerProbability, outerProbability, weight);
		}
		validBlock = validBlock && (probability >= 1 || context.getRandom().nextDouble() <= probability);
		if (validBlock)
		{
			Block block = context.getTargetBlock();
			Vector offset = new Vector();
			if (autoOrient) {
				Location location = actionContext.getLocation();
				offset.setX(dx + xOffset);
				offset.setY(dy);
				offset.setZ(dz + zOffset);
				Block originalBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
				actionContext.setTargetSourceLocation(originalBlock.getRelative(-xOffset, 0, -zOffset).getLocation());
				offset = rotate(location.getYaw(), location.getPitch(), offset.getX(), offset.getY(), offset.getZ());
			} else {
				offset.setX(dx);
				offset.setY(dy);
				offset.setZ(dz);
			}
			Block targetBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
			actionContext.setTargetLocation(targetBlock.getLocation());
			result = startActions();
		}
		else
		{
			skippedActions(context);
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
		int volume = (1 + xSizeCeil * 2) * (1 + ySizeCeil * 2) * (1 + zSizeCeil * 2);
		return volume * super.getActionCount();
	}
}
