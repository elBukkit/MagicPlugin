package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.metadata.FixedMetadataValue;
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
    private int breakable = 0;
    private double backfireChance = 0;
    private boolean applyPhysics = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        spawnFallingBlocks = parameters.getBoolean("falling", false);
        applyPhysics = parameters.getBoolean("physics", false);
        breakable = parameters.getInt("breakable", 0);
        backfireChance = parameters.getDouble("reflect_chance", 0);
        fallingBlockSpeed = parameters.getDouble("speed", 1.0);
        fallingBlockDirection = null;
        if (spawnFallingBlocks && parameters.contains("direction"))
        {
            fallingBlockDirection = ConfigurationUtils.getVector(parameters, "direction");
            if (fallingBlockDirection != null)
            {
                fallingBlockDirection.normalize();
            }
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
            MageController controller = context.getController();

            if (brush.isDifferent(block)) {
                context.registerForUndo(block);
                Mage mage = context.getMage();
                brush.update(mage, block.getLocation());
                brush.modify(block, applyPhysics);

                if (spawnFallingBlocks)
                {
                    FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
                    falling.setDropItem(false);
                    if (fallingBlockSpeed > 0) {
                        Vector fallingBlockVelocity = fallingBlockDirection;
                        if (fallingBlockVelocity == null) {
                            Location source = context.getBaseContext().getTargetLocation();
                            fallingBlockVelocity = falling.getLocation().subtract(source).toVector();
                            fallingBlockVelocity.normalize();
                        } else {
                            fallingBlockVelocity = fallingBlockVelocity.clone();
                        }
                        fallingBlockVelocity.multiply(fallingBlockSpeed);
                        falling.setVelocity(fallingBlockVelocity);
                    }
                    context.registerForUndo(falling);
                }
            }

            if (breakable > 0) {
                block.setMetadata("breakable", new FixedMetadataValue(controller.getPlugin(), breakable));
            }
            if (backfireChance > 0) {
                block.setMetadata("backfire", new FixedMetadataValue(controller.getPlugin(), backfireChance));
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
        parameters.add("direction");
        parameters.add("reflect_chance");
        parameters.add("breakable");
        parameters.add("physics");
    }

    @Override
    public void getParameterOptions(Collection<String> examples, String parameterKey) {
        if (parameterKey.equals("falling") || parameterKey.equals("physics")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("speed") || parameterKey.equals("breakable")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else if (parameterKey.equals("direction")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_VECTOR_COMPONENTS)));
        } else if (parameterKey.equals("reflect_chance")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_PERCENTAGES)));
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