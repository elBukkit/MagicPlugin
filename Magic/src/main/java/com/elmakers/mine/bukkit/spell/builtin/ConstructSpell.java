package com.elmakers.mine.bukkit.spell.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.batch.ConstructBatch;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class ConstructSpell extends BrushSpell
{
    public static final String[] CONSTRUCT_PARAMETERS = {
        "radius", "falling", "speed", "max_dimension", "replace", "consume",
        "type", "thickness", "orient_dimension_max", "orient_dimension_min",
        "breakable", "backfire", "select_self", "use_brush_size", "falling_direction"
    };

    private static final ConstructionType DEFAULT_CONSTRUCTION_TYPE = ConstructionType.SPHERE;
    private static final int DEFAULT_RADIUS                        = 2;
    private static final int DEFAULT_MAX_DIMENSION                 = 16;

    private Block targetBlock = null;
    private boolean powered = false;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Block target = null;
        boolean isSelect = getTargetType() == TargetType.SELECT;
        boolean finalCast = !isSelect || this.targetBlock != null;
        if (finalCast && parameters.getBoolean("select_self", true) && isLookingDown()) {
            target = mage.getLocation().getBlock().getRelative(BlockFace.DOWN);
        } else {
            Target t = getTarget();
            target = t.getBlock();
        }
        if (target == null)
        {
            return SpellResult.NO_TARGET;
        }

        MaterialBrush buildWith = getBrush();
        boolean hasPermission = buildWith != null && buildWith.isErase() ? hasBreakPermission(target) : hasBuildPermission(target);
        if (!hasPermission) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        int radius = parameters.getInt("radius", DEFAULT_RADIUS);
        radius = parameters.getInt("r", radius);
        radius = parameters.getInt("size", radius);
        boolean falling = parameters.getBoolean("falling", false);
        boolean physics = parameters.getBoolean("physics", false);
        boolean commit = parameters.getBoolean("commit", false);
        boolean consume = parameters.getBoolean("consume", false);
        boolean checkChunks = parameters.getBoolean("check_chunks", true);
        double breakable = parameters.getDouble("breakable", 0);
        double backfireChance = parameters.getDouble("reflect_chance", 0);
        Vector orientTo = null;
        Vector bounds = null;

        if (parameters.getBoolean("use_brush_size", false)) {
            if (!buildWith.isReady()) {
                long timeout = System.currentTimeMillis() + 10000;
                while (System.currentTimeMillis() < timeout) {
                    try {
                        Thread.sleep(500);
                        if (buildWith.isReady()) {
                            break;
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
                if (!buildWith.isReady()) {
                    return SpellResult.NO_ACTION;
                }
            }
            bounds = buildWith.getSize();
            radius = (int)Math.max(Math.max(bounds.getX() / 2, bounds.getZ() / 2), bounds.getY());
        } else if (isSelect) {
            if (targetLocation2 != null) {
                this.targetBlock = targetLocation2.getBlock();
            }

            if (targetBlock == null || !targetBlock.getWorld().equals(target.getWorld())) {
                targetBlock = target;
                activate();
                return SpellResult.TARGET_SELECTED;
            } else {
                radius = (int)targetBlock.getLocation().distance(target.getLocation());

                if (parameters.getBoolean("orient")) {
                    orientTo = target.getLocation().toVector().subtract(targetBlock.getLocation().toVector());
                    orientTo.setX(Math.abs(orientTo.getX()));
                    orientTo.setY(Math.abs(orientTo.getY()));
                    orientTo.setZ(Math.abs(orientTo.getZ()));
                    if (orientTo.getX() < orientTo.getZ() && orientTo.getX() < orientTo.getY()) {
                        orientTo = new Vector(1, 0, 0);
                    } else if (orientTo.getZ() < orientTo.getX() && orientTo.getZ() < orientTo.getY()) {
                        orientTo = new Vector(0, 0, 1);
                    } else {
                        orientTo = new Vector(0, 1, 0);
                    }
                }
                target = targetBlock;
                targetBlock = null;
            }
        } else if (parameters.getBoolean("orient")) {
            // orientTo = mage.getLocation().toVector().crossProduct(target.getLocation().toVector());
            orientTo = mage.getLocation().toVector().subtract(target.getLocation().toVector());
            orientTo.setX(Math.abs(orientTo.getX()));
            orientTo.setY(Math.abs(orientTo.getY()));
            orientTo.setZ(Math.abs(orientTo.getZ()));
            if (orientTo.getX() > orientTo.getZ() && orientTo.getX() > orientTo.getY()) {
                orientTo = new Vector(1, 0, 0);
            } else if (orientTo.getZ() > orientTo.getX() && orientTo.getZ() > orientTo.getY()) {
                orientTo = new Vector(0, 0, 1);
            } else {
                orientTo = new Vector(0, 1, 0);
            }
        }

        if (!parameters.contains("radius")) {
            int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);
            maxDimension = parameters.getInt("md", maxDimension);
            maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);

            int diameter = radius * 2;
            if (diameter > maxDimension)
            {
                return SpellResult.FAIL;
            }
        }

        // TODO : Is this needed? Or just use "ty"?
        if (parameters.contains("y_offset")) {
            target = target.getRelative(BlockFace.UP, parameters.getInt("y_offset", 0));
        }

        buildWith.setTarget(target.getLocation());

        ConstructionType conType = DEFAULT_CONSTRUCTION_TYPE;

        int thickness = parameters.getInt("thickness", 0);
        String typeString = parameters.getString("type", "");

        ConstructionType testType = ConstructionType.parseString(typeString, ConstructionType.UNKNOWN);
        if (testType != ConstructionType.UNKNOWN)
        {
            conType = testType;
        }

        ConstructBatch batch = new ConstructBatch(this, target.getLocation(), conType, radius, thickness, falling, orientTo);

        batch.setCommit(commit);
        batch.setConsume(consume);
        batch.setCheckChunks(checkChunks);
        UndoList undoList = getUndoList();
        if (undoList != null && !currentCast.isConsumeFree()) {
            undoList.setConsumed(consume);
        }

        if (parameters.getBoolean("replace", false)) {
            List<com.elmakers.mine.bukkit.api.block.MaterialAndData> replaceMaterials = new ArrayList<>();
            MaterialAndData wildReplace = new MaterialAndData(target);
            if (!parameters.getBoolean("match_data", true)) {
                wildReplace.setData(null);
            }
            // Hacky, but generally desired - maybe abstract to a parameterized list?
            Material targetMaterial = target.getType();
            if (DefaultMaterials.isWater(targetMaterial) || DefaultMaterials.isLava(targetMaterial))
            {
                wildReplace.setData(null);
            }
            replaceMaterials.add(wildReplace);
            batch.setReplace(replaceMaterials);
        }

        // Check for command block overrides
        if (parameters.contains("commands"))
        {
            ConfigurationSection commandMap = parameters.getConfigurationSection("commands");
            Set<String> keys = commandMap.getKeys(false);
            for (String key : keys) {
                batch.addCommandMapping(key, commandMap.getString(key));
            }
        }

        if (falling) {
            float force = (float)parameters.getDouble("speed", 0);
            batch.setFallingDirection(ConfigurationUtils.getVector(parameters, "falling_direction"));
            batch.setFallingBlockSpeed(force);
        }
        batch.setApplyPhysics(physics);
        if (breakable > 0) {
            batch.setBreakable(breakable);
        }
        if (backfireChance > 0) {
            batch.setBackfireChance(backfireChance);
        }
        if (parameters.contains("orient_dimension_max")) {
            batch.setOrientDimensionMax(parameters.getInt("orient_dimension_max"));
        } else if (parameters.contains("odmax")) {
            batch.setOrientDimensionMax(parameters.getInt("odmax"));
        }

        if (parameters.contains("orient_dimension_min")) {
            batch.setOrientDimensionMin(parameters.getInt("orient_dimension_min"));
        } else if (parameters.contains("odmin")) {
            batch.setOrientDimensionMin(parameters.getInt("odmin"));
        }
        if (parameters.getBoolean("power", false)) {
            batch.setPower(true);
        }
        if (bounds != null) {
            batch.setBounds(bounds);
            batch.setOrientDimensionMin(0);
        }
        boolean success = mage.addBatch(batch);
        deactivate();
        return success ? SpellResult.CAST : SpellResult.FAIL;
    }

    @Override
    protected boolean isBatched() {
        return true;
    }

    @Override
    public void onDeactivate() {
        targetBlock = null;
    }

    @Override
    public boolean onCancelSelection()
    {
        if (targetBlock != null)
        {
            deactivate();
            return true;
        }

        return false;
    }

    @Override
    public boolean hasBrushOverride()
    {
        return powered || super.hasBrushOverride();
    }

    @Override
    protected void loadTemplate(ConfigurationSection node)
    {
        super.loadTemplate(node);
        ConfigurationSection parameters = node.getConfigurationSection("parameters");
        if (parameters != null) {
            powered = parameters.getBoolean("power", false);
            if (powered) {
                controller.getLogger().warning("Using the 'power' flag with ConstructSpell is deprecated and will be removed in the future. Please use a Sphere + PowerBlock combination instead.");
            }
        }
    }

    @Override
    public void getParameters(Collection<String> parameters)
    {
        super.getParameters(parameters);
        parameters.addAll(Arrays.asList(CONSTRUCT_PARAMETERS));
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey)
    {
        super.getParameterOptions(examples, parameterKey);

        if (parameterKey.equals("radius") || parameterKey.equals("max_dimension")
                || parameterKey.equals("orient_dimension_max") || parameterKey.equals("orient_dimension_min")
                || parameterKey.equals("thickness") || parameterKey.equals("speed")) {
            examples.addAll(Arrays.asList(EXAMPLE_SIZES));
        } else if (parameterKey.equals("type")) {
            ConstructionType[] constructionTypes = ConstructionType.values();
            for (ConstructionType constructionType : constructionTypes) {
                examples.add(constructionType.name().toLowerCase());
            }
        } else if (parameterKey.equals("power") || parameterKey.equals("replace") || parameterKey.equals("falling")) {
            examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
        }
    }
}
