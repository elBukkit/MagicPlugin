package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.MaterialMap;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.batch.ConstructBatch;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.Target;

public class ConstructSpell extends BrushSpell
{
    public static final String[] CONSTRUCT_PARAMETERS = {
        "radius", "falling", "speed", "max_dimension", "replace", "consume",
        "type", "thickness", "orient_dimension_max", "orient_dimension_min",
        "breakable", "backfire", "select_self", "use_brush_size", "falling_direction", "lock_chunks"
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
        boolean commit = parameters.getBoolean("commit", false);
        boolean consume = parameters.getBoolean("consume", false);
        boolean checkChunks = parameters.getBoolean("check_chunks", true);
        boolean lockChunks = parameters.getBoolean("lock_chunks", false);
        double breakable = parameters.getDouble("breakable", 0);
        double backfireChance = parameters.getDouble("reflect_chance", 0);

        String physicsType = parameters.getString("physics", "");
        Vector orientTo = null;

        if (isSelect) {
            if (targetLocation2 != null) {
                this.targetBlock = targetLocation2.getBlock();
            }

            if (targetBlock == null || !targetBlock.getWorld().equals(target.getWorld())) {
                targetBlock = target;
                activate();
                setSelectedLocation(targetBlock.getLocation());
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

        batch.setUseBrushSize(parameters.getBoolean("use_brush_size", false));
        batch.setCommit(commit);
        batch.setConsume(consume);
        batch.setCheckChunks(checkChunks);

        MaterialMap replacements = controller.getMaterialSetManager().mapFromConfig(parameters, "replacements");
        if (replacements != null) {
            batch.setReplaceMaterials(replacements);
        }

        UndoList undoList = getUndoList();
        if (undoList != null && !currentCast.isConsumeFree()) {
            undoList.setConsumed(consume);
        }

        if (parameters.getBoolean("replace", false)) {
            boolean matchData = parameters.getBoolean("match_data", true);
            if (mage.isSneaking()) {
                matchData = parameters.getBoolean("sneak_match_data", matchData);
            }
            if (matchData) {
                batch.setReplace(new MaterialAndData(target));
            } else {
                batch.setReplaceType(target.getType());
            }
        }

        // Check for command block overrides
        // this is here for backwards-compatibility, the better way to do this now is via brush_commands
        // which is handled by BrushSpell
        if (parameters.contains("commands"))
        {
            ConfigurationSection commandMap = parameters.getConfigurationSection("commands");
            Set<String> keys = commandMap.getKeys(false);
            for (String key : keys) {
                brush.addCommandMapping(key, commandMap.getString(key));
            }
        }

        if (falling) {
            float force = (float)parameters.getDouble("speed", 0);
            batch.setFallingDirection(ConfigurationUtils.getVector(parameters, "falling_direction"));
            batch.setFallingBlockSpeed(force);
        }
        batch.setLockChunks(lockChunks);
        batch.setApplyPhysics(physicsType.equalsIgnoreCase("true"));
        batch.setDeferPhysics(physicsType.equalsIgnoreCase("defer"));
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
        boolean success = mage.addBatch(batch);
        return success ? SpellResult.CAST : SpellResult.FAIL;
    }

    @Override
    protected void onFinalizeCast(SpellResult result) {
        if (result != SpellResult.TARGET_SELECTED) {
            deactivate(false, true, false);
        }
    }

    @Override
    protected boolean isBatched() {
        return true;
    }

    @Override
    public void onDeactivate() {
        targetBlock = null;
        setSelectedLocation(null);
    }

    @Override
    public boolean onCancelSelection()
    {
        return targetBlock != null;
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
        } else if (parameterKey.equals("power") || parameterKey.equals("replace")
                || parameterKey.equals("falling") || parameterKey.equals("lock_chunks")) {
            examples.addAll(Arrays.asList(EXAMPLE_BOOLEANS));
        }
    }
}
