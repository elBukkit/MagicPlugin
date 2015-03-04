package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.elmakers.mine.bukkit.api.action.BlockAction;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.spell.BaseSpellAction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.block.MaterialAndData;

public class ReplaceMaterialAction extends BaseSpellAction implements BlockAction {
    protected Set<MaterialAndData> replaceable = null;

    private boolean spawnFallingBlocks = false;
    private Vector fallingBlockVelocity = null;

    // TOOD: Remove these constructors once Recursespell is Actionized
    public ReplaceMaterialAction() {

    }

    public ReplaceMaterialAction(Spell spell, Block targetBlock)
    {
        this.setSpell(spell);
        if (targetBlock != null) {
            replaceable.add(new MaterialAndData(targetBlock));
        }
    }

    public void addReplaceable(Material material) {
        replaceable.add(new MaterialAndData(material));
    }

    public void addReplaceable(Material material, byte data) {
        replaceable.add(new MaterialAndData(material, data));
    }

    @SuppressWarnings("deprecation")
    @Override
    public SpellResult perform(ConfigurationSection parameters, Block block) {
        MaterialBrush brush = getBrush();
        if (brush == null) {
            return SpellResult.FAIL;
        }

        if (!hasBuildPermission(block)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        if (!isDestructible(block)) {
            return SpellResult.FAIL;
        }

        if (replaceable == null || replaceable.contains(new MaterialAndData(block))) {
            Material previousMaterial = block.getType();
            byte previousData = block.getData();

            if (brush.isDifferent(block)) {
                registerForUndo(block);
                Mage mage = getMage();
                brush.update(mage, block.getLocation());
                brush.modify(block);
                updateBlock(block);

                if (spawnFallingBlocks) {
                    FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), previousMaterial, previousData);
                    falling.setDropItem(false);
                    if (fallingBlockVelocity != null) {
                        falling.setVelocity(fallingBlockVelocity);
                    }
                }
            }
            return SpellResult.CAST;
        }

        return SpellResult.FAIL;
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
    public boolean isUndoable() {
        return true;
    }

    @Override
    public boolean usesBrush() {
        return true;
    }
}