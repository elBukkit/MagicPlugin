package com.elmakers.mine.bukkit.spell.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.batch.ShapeBatch;
import com.elmakers.mine.bukkit.block.ConstructionType;
import com.elmakers.mine.bukkit.spell.BrushSpell;
import com.elmakers.mine.bukkit.utility.Target;

public class ShapeSpell extends BrushSpell
{
    public static final String[] CONSTRUCT_PARAMETERS = {
        "radius", "max_dimension", "orient_dimension_max", "orient_dimension_min", "thickness"
    };

    private static final ConstructionType DEFAULT_CONSTRUCTION_TYPE = ConstructionType.SPHERE;
    private static final int DEFAULT_RADIUS                        = 8;
    private static final int DEFAULT_MAX_DIMENSION                 = 32;

    private Block targetBlock = null;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Target t = getTarget();
        Block target = t.getBlock();
        if (target == null)
        {
            return SpellResult.NO_TARGET;
        }

        int radius = parameters.getInt("radius", DEFAULT_RADIUS);
        radius = parameters.getInt("r", radius);
        Location orientTo = null;

        if (getTargetType() == TargetType.SELECT) {

            if (targetLocation2 != null) {
                this.targetBlock = targetLocation2.getBlock();
            }

            if (targetBlock == null) {
                targetBlock = target;
                activate();

                return SpellResult.TARGET_SELECTED;
            } else {
                setSelectedLocation(targetBlock.getLocation());
                radius = (int)targetBlock.getLocation().distance(target.getLocation());
                orientTo = target.getLocation();
                target = targetBlock;
            }
        }

        int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);
        maxDimension = parameters.getInt("md", maxDimension);
        maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);

        int diameter = radius * 2;
        if (diameter > maxDimension)
        {
            return SpellResult.FAIL;
        }

        if (!hasBuildPermission(target)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        MaterialBrush buildWith = getBrush();
        buildWith.setTarget(target.getLocation());

        ConstructionType conType = DEFAULT_CONSTRUCTION_TYPE;
        String typeString = parameters.getString("type", "");

        ConstructionType testType = ConstructionType.parseString(typeString, ConstructionType.UNKNOWN);
        if (testType != ConstructionType.UNKNOWN)
        {
            conType = testType;
        }

        int thickness = parameters.getInt("thickness", 1);

        ShapeBatch batch = new ShapeBatch(this, target.getLocation(), conType, radius, thickness, orientTo);

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

        boolean success = mage.addBatch(batch);
        return success ? SpellResult.CAST : SpellResult.FAIL;
    }

    @Override
    protected void onFinalizeCast(SpellResult result) {
        if (result != SpellResult.TARGET_SELECTED) {
            onDeactivate();
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
        if (targetBlock != null)
        {
            deactivate();
            return true;
        }

        return false;
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
                || parameterKey.equals("orient_dimension_max")
                || parameterKey.equals("orient_dimension_min") || parameterKey.equals("thickness")) {
            examples.addAll(Arrays.asList(EXAMPLE_SIZES));
        } else if (parameterKey.equals("type")) {
            ConstructionType[] constructionTypes = ConstructionType.values();
            for (ConstructionType constructionType : constructionTypes) {
                examples.add(constructionType.name().toLowerCase());
            }
        }
    }
}
