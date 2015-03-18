package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ModifyBlockAction extends BaseSpellAction {
    protected Set<MaterialAndData> replaceable = null;

    private boolean spawnFallingBlocks;
    private double fallingBlockSpeed;
    private Vector fallingBlockDirection;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        spawnFallingBlocks = parameters.getBoolean("falling", false);
        if (spawnFallingBlocks && parameters.contains("direction"))
        {
            fallingBlockSpeed = parameters.getDouble("speed", 1.0);
            fallingBlockDirection = ConfigurationUtils.getVector(parameters, "direction");
            if (fallingBlockDirection != null)
            {
                fallingBlockDirection.normalize();
            }
        }
        else
        {
            fallingBlockSpeed = 0;
        }
    }

    public void addReplaceable(MaterialAndData material) {
        if (replaceable == null) {
            replaceable = new HashSet<MaterialAndData>();
        }
        replaceable.add(material);
    }

    public void addReplaceable(Material material, byte data) {
        addReplaceable(new MaterialAndData(material, data));
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context) {
        MaterialBrush brush = context.getBrush();
        if (brush == null) {
            return SpellResult.FAIL;
        }

        Block block = context.getTargetBlock();
        if (!context.hasBuildPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        if (!context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }

        if (replaceable == null || replaceable.contains(new MaterialAndData(block))) {
            Material previousMaterial = block.getType();
            byte previousData = block.getData();

            if (brush.isDifferent(block)) {
                context.registerForUndo(block);
                Mage mage = context.getMage();
                brush.update(mage, block.getLocation());
                brush.modify(block);

                if (spawnFallingBlocks)
                {
                    FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
                    falling.setDropItem(false);
                    if (fallingBlockSpeed > 0) {
                        Vector fallingBlockVelocity = fallingBlockDirection;
                        if (fallingBlockVelocity == null) {
                            Location source = context.getLocation();
                            fallingBlockVelocity = block.getLocation().toVector().subtract(source.toVector());
                            fallingBlockVelocity.normalize();
                        } else {
                            fallingBlockVelocity = fallingBlockVelocity.clone();
                        }
                        fallingBlockVelocity.multiply(fallingBlockSpeed);
                        falling.setVelocity(fallingBlockVelocity);
                    }
                }
            }
            return SpellResult.CAST;
        }

        return SpellResult.NO_TARGET;
    }

    @Override
    public void getParameterNames(Collection<String> parameters) {
        super.getParameterNames(parameters);
        parameters.add("falling");
        parameters.add("speed");
        parameters.add("falling_direction");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("falling")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("speed")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else if (parameterKey.equals("falling_direction")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_VECTOR_COMPONENTS)));
        } else {
            super.getParameterOptions(examples, parameterKey);
        }
    }

    @Override
    public boolean requiresBuildPermission() {
        return true;
    }

    @Override
    public boolean requiresTarget() {
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
}