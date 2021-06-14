package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.action.BaseProjectileAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.block.UndoList;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.SafetyUtils;

public class ThrowBlockAction extends BaseProjectileAction
{
    private double speedMin;
    private double speedMax;
    private float fallDamage;
    private int maxDamage;
    private boolean consumeBlocks = false;
    private boolean hurts = true;
    private boolean consumeVariants = true;
    private SourceLocation sourceLocation;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        double itemSpeed = parameters.getDouble("speed", 0.6f);
        speedMin = parameters.getDouble("speed_min", itemSpeed);
        speedMax = parameters.getDouble("speed_max", itemSpeed);
        int damage = parameters.getInt("damage", 0);
        fallDamage = (float)parameters.getDouble("fall_damage", damage);
        maxDamage = parameters.getInt("max_damage", damage);
        consumeBlocks = parameters.getBoolean("consume", false);
        hurts = parameters.getBoolean("hurts", true);
        consumeVariants = parameters.getBoolean("consume_variants", true);
        sourceLocation = new SourceLocation(parameters);
    }

    @Override
    public SpellResult start(CastContext context)
    {
        Location location = context.getLocation();
        if (!context.hasBuildPermission(location.getBlock())) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        location.setY(location.getY() - 1);
        MaterialBrush buildWith = context.getBrush();
        buildWith.setTarget(location);

        if (buildWith.isErase() || DefaultMaterials.isAir(buildWith.getMaterial())) {
            return SpellResult.NO_TARGET;
        }

        if (consumeBlocks && !context.isConsumeFree()) {
            Mage mage = context.getMage();
            UndoList undoList = context.getUndoList();
            if (undoList != null) {
                undoList.setConsumed(true);
            }
            if (!mage.consumeBlock(buildWith, consumeVariants)) {
                String requiresMessage = context.getMessage("insufficient_resources");
                context.sendMessageKey("insufficient_resources", requiresMessage.replace("$cost", buildWith.getName()));
                return SpellResult.STOP;
            }
        }

        Material material = buildWith.getMaterial();
        byte data = buildWith.getBlockData();

        location = sourceLocation.getLocation(context);
        Vector direction = location.getDirection();
        double speed = context.getRandom().nextDouble() * (speedMax - speedMin) + speedMin;
        direction.normalize().multiply(speed);

        FallingBlock falling = CompatibilityLib.getDeprecatedUtils().spawnFallingBlock(location, material, data);
        if (falling == null)
        {
            return SpellResult.FAIL;
        }
        track(context, falling);
        if (!consumeBlocks) {
            falling.setDropItem(false);
        }
        SafetyUtils.setVelocity(falling, direction);
        if (maxDamage > 0 && fallDamage > 0) {
            CompatibilityLib.getCompatibilityUtils().setFallingBlockDamage(falling, fallDamage, maxDamage);
        } else {
            falling.setHurtEntities(hurts);
        }

        return checkTracking(context);
    }

    @Override
    public boolean requiresBuildPermission() {
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

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("speed");
        parameters.add("speed_min");
        parameters.add("speed_max");
        parameters.add("damage");
        parameters.add("max_damage");
        parameters.add("fall_damage");
        parameters.add("hurts");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("speed") || parameterKey.equals("speed_max") || parameterKey.equals("speed_min")
            || parameterKey.equals("damage") || parameterKey.equals("max_damage") || parameterKey.equals("fall_damage")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("hurts")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
