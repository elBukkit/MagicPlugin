package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.UndoList;
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

public class ModifyBlockAction extends BaseSpellAction {
    private boolean spawnFallingBlocks;
    private double fallingBlockSpeed;
    private Vector fallingBlockDirection;
    private int breakable = 0;
    private double backfireChance = 0;
    private boolean applyPhysics = false;
    private boolean commit = false;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        spawnFallingBlocks = parameters.getBoolean("falling", false);
        applyPhysics = parameters.getBoolean("physics", false);
        commit = parameters.getBoolean("commit", false);
        breakable = parameters.getInt("breakable", 0);
        backfireChance = parameters.getDouble("reflect_chance", 0);
        fallingBlockSpeed = parameters.getDouble("speed", 0);
        fallingBlockDirection = null;
        if (spawnFallingBlocks && parameters.contains("direction"))
        {
            if (fallingBlockSpeed == 0) {
                fallingBlockSpeed = 1;
            }
            fallingBlockDirection = ConfigurationUtils.getVector(parameters, "direction");
            if (fallingBlockDirection != null)
            {
                fallingBlockDirection.normalize();
            }
        }
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

        if (commit)
        {
            if (!context.areAnyDestructible(block)) {
                return SpellResult.NO_TARGET;
            }
        }
        else if (!context.isDestructible(block)) {
            return SpellResult.NO_TARGET;
        }

        Material previousMaterial = block.getType();
        byte previousData = block.getData();

        if (brush.isDifferent(block)) {
            if (!commit) {
                context.registerForUndo(block);
            }
            Mage mage = context.getMage();
            brush.update(mage, block.getLocation());
            brush.modify(block, applyPhysics);

            if (spawnFallingBlocks && previousMaterial != Material.AIR)
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
            context.registerBreakable(block, breakable);
        }
        if (backfireChance > 0) {
            context.registerReflective(block, backfireChance);
        }

        if (commit) {
            com.elmakers.mine.bukkit.api.block.BlockData blockData = UndoList.register(block);
            blockData.commit();
        }
        return SpellResult.CAST;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("falling");
        parameters.add("speed");
        parameters.add("direction");
        parameters.add("reflect_chance");
        parameters.add("breakable");
        parameters.add("physics");
        parameters.add("commit");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("falling") || parameterKey.equals("physics") || parameterKey.equals("commit")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
        } else if (parameterKey.equals("speed") || parameterKey.equals("breakable")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_SIZES)));
        } else if (parameterKey.equals("direction")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_VECTOR_COMPONENTS)));
        } else if (parameterKey.equals("reflect_chance")) {
            examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_PERCENTAGES)));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
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