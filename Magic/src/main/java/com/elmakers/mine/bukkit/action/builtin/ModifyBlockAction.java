package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.SafetyUtils;

public class ModifyBlockAction extends BaseSpellAction {
    private boolean spawnFallingBlocks;
    private boolean fallingBlocksHurt;
    private double fallingBlockSpeed;
    private Vector fallingBlockDirection;
    private float fallingBlockFallDamage;
    private int fallingBlockMaxDamage;
    private double fallingProbability;
    private double breakable = 0;
    private double backfireChance = 0;
    private boolean applyPhysics = false;
    private boolean commit = false;
    private boolean consumeBlocks = false;
    private boolean consumeVariants = true;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        spawnFallingBlocks = parameters.getBoolean("falling", false);
        applyPhysics = parameters.getBoolean("physics", false);
        commit = parameters.getBoolean("commit", false);
        breakable = parameters.getDouble("breakable", 0);
        backfireChance = parameters.getDouble("reflect_chance", 0);
        fallingBlockSpeed = parameters.getDouble("speed", 0);
        fallingProbability = parameters.getDouble("falling_probability", 1);
        consumeBlocks = parameters.getBoolean("consume", false);
        consumeVariants = parameters.getBoolean("consume_variants", true);
        fallingBlocksHurt = parameters.getBoolean("falling_hurts", false);
        fallingBlockDirection = null;
        if (spawnFallingBlocks && parameters.contains("direction") && !parameters.getString("direction").isEmpty())
        {
            if (fallingBlockSpeed == 0) {
                fallingBlockSpeed = 1;
            }
            fallingBlockDirection = ConfigurationUtils.getVector(parameters, "direction");
        }

        int damage = parameters.getInt("damage", 0);
        fallingBlockFallDamage = (float)parameters.getDouble("fall_damage", damage);
        fallingBlockMaxDamage = parameters.getInt("max_damage", damage);
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(CastContext context) {
        MaterialBrush brush = context.getBrush();
        if (brush == null) {
            return SpellResult.FAIL;
        }

        Block block = context.getTargetBlock();
        if (brush.isErase()) {
            if (!context.hasBreakPermission(block)) {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
        } else {
            if (!context.hasBuildPermission(block)) {
                return SpellResult.INSUFFICIENT_PERMISSION;
            }
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

        Mage mage = context.getMage();
        brush.update(mage, context.getTargetSourceLocation());

        if (!brush.isDifferent(block)) {
            return SpellResult.NO_TARGET;
        }

        if (!brush.isReady()) {
            brush.prepare();
            return SpellResult.PENDING;
        }

        if (!brush.isValid()) {
            return SpellResult.FAIL;
        }

        if (consumeBlocks && !context.isConsumeFree() && !brush.isErase()) {
            UndoList undoList = context.getUndoList();
            if (undoList != null) {
                undoList.setConsumed(true);
            }
            ItemStack requires = brush.getItemStack(1);
            if (!mage.hasItem(requires, consumeVariants)) {
                String requiresMessage = context.getMessage("insufficient_resources");
                context.sendMessage(requiresMessage.replace("$cost", brush.getName()));
                return SpellResult.STOP;
            }
            mage.removeItem(requires, consumeVariants);
        }

        if (!commit) {
            context.registerForUndo(block);
            if (brush.isErase()) {
                context.clearAttachables(block);
            }
        }
        UndoList undoList = context.getUndoList();
        if (undoList != null) {
            undoList.setApplyPhysics(applyPhysics);
        }
        brush.modify(block, applyPhysics);

        boolean spawnFalling = spawnFallingBlocks && previousMaterial != Material.AIR;
        if (spawnFalling && fallingProbability < 1) {
            spawnFalling = context.getRandom().nextDouble() < fallingProbability;
        }
        if (spawnFalling)
        {
            Location blockLocation = block.getLocation();
            Location blockCenter = new Location(blockLocation.getWorld(), blockLocation.getX() + 0.5, blockLocation.getY() + 0.5, blockLocation.getZ() + 0.5);
            Vector fallingBlockVelocity = null;
            if (fallingBlockSpeed > 0) {
                Location source = context.getTargetCenterLocation();
                fallingBlockVelocity = blockCenter.clone().subtract(source).toVector();
                fallingBlockVelocity.normalize();

                if (fallingBlockDirection != null)
                {
                    fallingBlockVelocity.add(fallingBlockDirection).normalize();
                }
                fallingBlockVelocity.multiply(fallingBlockSpeed);
            }
            if (fallingBlockVelocity != null && (
                   Double.isNaN(fallingBlockVelocity.getX()) || Double.isNaN(fallingBlockVelocity.getY()) || Double.isNaN(fallingBlockVelocity.getZ())
                || Double.isInfinite(fallingBlockVelocity.getX()) || Double.isInfinite(fallingBlockVelocity.getY()) || Double.isInfinite(fallingBlockVelocity.getZ())
            ))
            {
                fallingBlockVelocity = null;
            }
            boolean spawned = false;
            if (!spawned) {
                FallingBlock falling = block.getWorld().spawnFallingBlock(blockCenter, previousMaterial, previousData);
                falling.setDropItem(false);
                if (fallingBlockVelocity != null) {
                    SafetyUtils.setVelocity(falling, fallingBlockVelocity);
                }
                if (fallingBlockMaxDamage > 0 && fallingBlockFallDamage > 0) {
                    CompatibilityUtils.setFallingBlockDamage(falling, fallingBlockFallDamage, fallingBlockMaxDamage);
                } else {
                    falling.setHurtEntities(fallingBlocksHurt);
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
            com.elmakers.mine.bukkit.api.block.BlockData blockData = com.elmakers.mine.bukkit.block.UndoList.register(block);
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
        parameters.add("hurts");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("falling") || parameterKey.equals("physics") || parameterKey.equals("commit") || parameterKey.equals("falling_hurts")) {
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