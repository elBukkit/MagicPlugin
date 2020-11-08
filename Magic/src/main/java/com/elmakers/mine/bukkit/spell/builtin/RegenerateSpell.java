package com.elmakers.mine.bukkit.spell.builtin;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.spell.TargetType;
import com.elmakers.mine.bukkit.batch.RegenerateBatch;
import com.elmakers.mine.bukkit.spell.BlockSpell;

public class RegenerateSpell extends BlockSpell
{
    private static final int DEFAULT_MAX_DIMENSION = 128;

    private Block targetBlock = null;

    @Override
    public SpellResult onCast(ConfigurationSection parameters)
    {
        Block targetBlock = getTargetBlock();

        if (targetBlock == null)
        {
            return SpellResult.NO_TARGET;
        }
        if (!hasBuildPermission(targetBlock) || !hasBreakPermission(targetBlock)) {
            return SpellResult.INSUFFICIENT_PERMISSION;
        }

        if (targetLocation2 != null) {
            this.targetBlock = targetLocation2.getBlock();
        }

        TargetType targetType = getTargetType();
        if (this.targetBlock != null || targetType == TargetType.BLOCK)
        {
            Block secondBlock = targetType == TargetType.BLOCK ? targetBlock : this.targetBlock;
            RegenerateBatch batch = new RegenerateBatch(this, secondBlock.getLocation(), targetBlock.getLocation());

            int maxDimension = parameters.getInt("max_dimension", DEFAULT_MAX_DIMENSION);
            maxDimension = (int)(mage.getConstructionMultiplier() * maxDimension);

            if (!batch.checkDimension(maxDimension))
            {
                return SpellResult.FAIL;
            }

            batch.setExpand(parameters.getBoolean("expand", false));

            boolean success = mage.addBatch(batch);
            return success ? SpellResult.CAST : SpellResult.FAIL;
        }
        else
        {
            setSelectedLocation(targetBlock.getLocation());
            this.targetBlock = targetBlock;
            activate();
            return SpellResult.TARGET_SELECTED;
        }
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
    public boolean onCancelSelection()
    {
        return targetBlock != null;
    }

    @Override
    public void onDeactivate() {
        targetBlock = null;
        setSelectedLocation(null);
    }
}
