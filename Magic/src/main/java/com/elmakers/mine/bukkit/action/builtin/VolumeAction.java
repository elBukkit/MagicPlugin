package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.CompoundAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.RandomUtils;

public class VolumeAction extends CompoundAction
{
    public static final float DEGTORAD = 0.017453292f;
    private static final int DEFAULT_RADIUS    = 2;
    protected boolean autoOrient;
    protected boolean autoPitch;
    protected boolean reorient;
    protected float orientYawLock = 0;
    protected float orientPitchLock = 0;
    protected double radius;
    protected double radiusSquared;
    protected int spiralRadius;
    protected int currentRadius;
    protected float centerProbability;
    protected float outerProbability;
    protected double xSize;
    protected double ySize;
    protected double zSize;
    protected double radiusPadding;
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
    private boolean replaceTarget;
    private boolean appliedMultiplier;
    private float pitch;
    private float yaw;
    private boolean checkChunk = false;

    private Material replaceMaterial;

    private enum VolumeType {
        SPIRAL(true),
        REVERSE_SPIRAL(true),
        YZX,
        YXZ,
        ZXY,
        XZY;

        VolumeType() {
            symmetrical = false;
        }

        VolumeType(boolean symmetrical) {
            this.symmetrical = symmetrical;
        }

        private final boolean symmetrical;

        public boolean isSymmetrical() {
            return symmetrical;
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        radiusPadding = parameters.getDouble("radius_padding", 0.25);
        radius = parameters.getDouble("radius", DEFAULT_RADIUS);
        xSize = parameters.getDouble("x_size", radius);
        ySize = parameters.getDouble("y_size", radius);
        zSize = parameters.getDouble("z_size", radius);
        appliedMultiplier = false;
        centerY = parameters.getBoolean("center_y", true);
        centerX = parameters.getBoolean("center_x", true);
        centerZ = parameters.getBoolean("center_z", true);
        thickness = parameters.getDouble("thickness", 0);
        reorient = parameters.getBoolean("reorient", true);
        autoOrient = parameters.getBoolean("orient", false);
        autoPitch = parameters.getBoolean("orient_pitch", autoOrient);
        orientYawLock = (float)parameters.getDouble("orient_snap", 0);
        orientPitchLock = (float)parameters.getDouble("orient_pitch_snap", orientYawLock);
        centerProbability = (float)parameters.getDouble("probability", 1);
        outerProbability = (float)parameters.getDouble("probability", 1);
        centerProbability = (float)parameters.getDouble("center_probability", centerProbability);
        outerProbability = (float)parameters.getDouble("outer_probability", outerProbability);
        useBrushSize = parameters.getBoolean("use_brush_size", false);
        replaceTarget = parameters.getBoolean("replace", false);
        checkChunk = parameters.getBoolean("check_chunk", true);
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

        if (!reorient && autoOrient) {
            updateOrient(context);
        }
    }

    private void updateOrient(CastContext context) {
        Location location = context.getLocation();
        if (location == null) return;
        pitch = 0.0f;
        if (autoPitch) {
            pitch = location.getPitch();
            if (orientPitchLock > 0) {
                pitch = orientPitchLock * Math.round(pitch / orientPitchLock);
            }
        }
        yaw = location.getYaw();
        if (orientYawLock > 0) {
            yaw = orientYawLock * Math.round(yaw / orientYawLock);
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
            if (volumeType.isSymmetrical()) {
                xSize = Math.max(xSize, zSize);
                zSize = Math.max(xSize, zSize);
            }
            centerY = false;
            context.getMage().sendDebugMessage(ChatColor.GREEN + "Brush Size: " + ChatColor.GRAY + xSize + "," + ySize + "," + zSize, 2);
        } else if (!appliedMultiplier) {
            xSize = context.getMage().getRadiusMultiplier() * this.xSize;
            ySize = context.getMage().getRadiusMultiplier() * this.ySize;
            zSize = context.getMage().getRadiusMultiplier() * this.zSize;
            appliedMultiplier = true;
        }
        if (volumeType.isSymmetrical() && xSize != zSize) {
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

        if (!volumeType.isSymmetrical()) {
            min = new Vector(-xSizeCeil, yStart, -zSizeCeil);
            max = new Vector(xSizeCeil, yEnd, zSizeCeil);
        }

        radius = Math.max(xSize, zSize);
        spiralRadius = (int)Math.ceil(radius);
        radius = Math.max(radius, ySize);
        radiusSquared = (radius + radiusPadding) * (radius + radiusPadding);
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
        } else if (volumeType == VolumeType.REVERSE_SPIRAL) {
            currentRadius = spiralRadius;
            dx = -Math.max(startRadius, xSizeCeil);
            dy = yStart;
            dz = -Math.max(startRadius, zSizeCeil);
            xDirection = 1;
            zDirection = 0;
        } else {
            dx = min.getBlockX();
            dy = min.getBlockY();
            dz = min.getBlockZ();
        }
    }

    public static Vector rotate(float yaw, float pitch, double x, double y, double z) {
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

    protected boolean nextXZY(CastContext context) {
        dx++;
        if (dx > max.getBlockX()) {
            dx = min.getBlockX();
            dz++;
            if (dz > max.getBlockZ()) {
                dz = min.getBlockZ();
                dy++;
            }
        }
        return (dx <= max.getBlockX() && dy <= max.getBlockY() && dz <= max.getBlockZ());
    }

    protected boolean nextZXY(CastContext context) {
        dz++;
        if (dz > max.getBlockZ()) {
            dz = min.getBlockZ();
            dx++;
            if (dx > max.getBlockX()) {
                dx = min.getBlockX();
                dy++;
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
        return currentRadius <= spiralRadius;
    }

    protected boolean nextReverseSpiral(CastContext context) {
        dy++;
        if (dy > yEnd) {
            dy = yStart;
            int nextX = dx + xDirection;
            int nextZ = dz + zDirection;
            if (currentRadius == 0) {
                currentRadius--;
            } else if (xDirection == 1 && dx >= currentRadius) {
                xDirection = 0;
                zDirection = 1;
                dz += zDirection;
            } else if (zDirection == 1 && dz >= currentRadius) {
                xDirection = -1;
                zDirection = 0;
                dx += xDirection;
            } else if (xDirection == -1 && dx <= -currentRadius) {
                xDirection = 0;
                zDirection = -1;
                dz += zDirection;
            } else if (zDirection == -1 && nextZ <= -currentRadius) {
                currentRadius--;
                dx = -currentRadius;
                dz = -currentRadius;
                xDirection = 1;
                zDirection = 0;
            } else {
                dx = nextX;
                dz = nextZ;
            }
        }

        return currentRadius >= 0;
    }

    @Override
    public SpellResult start(CastContext context) {
        if (!calculateSize(context)) {
            return SpellResult.PENDING;
        }
        if (replaceTarget) {
            replaceMaterial = context.getTargetBlock().getType();
        } else {
            replaceMaterial = null;
        }
        resetCounters();
        actionContext.setTargetCenterLocation(context.getTargetLocation());
        return SpellResult.NO_ACTION;
    }

    @Override
    public boolean next(CastContext context) {
        if (radius < 1) {
            return false;
        }

        boolean result = false;
        switch (volumeType) {
            case SPIRAL:
                result = nextSpiral(context);
                break;
            case REVERSE_SPIRAL:
                result = nextReverseSpiral(context);
                break;
            case YZX:
                result = nextYZX(context);
                break;
            case YXZ:
                result = nextYXZ(context);
                break;
            case ZXY:
                result = nextZXY(context);
                break;
            case XZY:
                result = nextXZY(context);
                break;
        }
        return result;
    }

    @Override
    public SpellResult step(CastContext context) {
        SpellResult result = SpellResult.NO_ACTION;
        boolean singleBlock = radius < 1;
        boolean validBlock = singleBlock ? true : containsPoint(context, dy, dz, dx);
        float probability = centerProbability;
        if (!singleBlock && centerProbability != outerProbability) {
            float weight = Math.abs((float) dx + dz) / ((float) spiralRadius * 2);
            probability = RandomUtils.lerp(centerProbability, outerProbability, weight);
        }
        validBlock = validBlock && (probability >= 1 || context.getRandom().nextDouble() <= probability);
        if (validBlock)
        {
            if (checkChunk && !CompatibilityUtils.checkChunk(context.getTargetLocation())) {
                context.addWork(100);
                return SpellResult.PENDING;
            }
            Block block = context.getTargetBlock();
            Vector offset = new Vector();
            if (autoOrient) {
                if (reorient) {
                    updateOrient(actionContext);
                }
                offset.setX(dx + xOffset);
                offset.setY(dy);
                offset.setZ(dz + zOffset);
                Block originalBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
                actionContext.setTargetSourceLocation(originalBlock.getRelative(-xOffset, 0, -zOffset).getLocation());
                offset = rotate(yaw, pitch, offset.getX(), offset.getY(), offset.getZ());
            } else {
                offset.setX(dx);
                offset.setY(dy);
                offset.setZ(dz);
            }
            Block targetBlock = block.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
            if (replaceMaterial == null || targetBlock.getType() == replaceMaterial) {
                actionContext.setTargetLocation(targetBlock.getLocation());
                result = startActions();
            }
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

    protected boolean containsPoint(CastContext context, int y, int z, int x)
    {
        return thickness == 0
                || (x > 0 && x > radius - thickness)
                || (y > 0 && y > radius - thickness)
                || (z > 0 && z > radius - thickness)
                || (x < 0 && x < thickness - radius)
                || (y < 0 && y < thickness - radius)
                || (z < 0 && z < thickness - radius);
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
