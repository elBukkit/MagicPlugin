package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;

public class CheckBlockAction extends CheckAction {
    private MaterialSet allowed;
    private boolean useTarget;
    private BlockFace direction;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);

        allowed = spell.getController().getMaterialSetManager()
                .fromConfig(parameters.getString("allowed"));
    }



    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useTarget = parameters.getBoolean("use_target", true);
        String directionString = parameters.getString("direction");
        if (directionString != null && !directionString.isEmpty()) {
            try {
                direction = BlockFace.valueOf(directionString.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Invalid BlockFace direction: " + directionString);
            }
        }
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        MaterialBrush brush = context.getBrush();
        Block block = useTarget ? context.getTargetBlock() : context.getLocation().getBlock();
        if (block == null) {
            return false;
        }
        if (direction != null) {
            block = block.getRelative(direction);
        }
        if (allowed != null) {
            if (!allowed.testBlock(block)) return false;
        } else {
            if (brush != null && brush.isErase()) {
                if (!context.hasBreakPermission(block)) {
                    return false;
                }
            } else {
                if (!context.hasBuildPermission(block)) {
                    return false;
                }
            }
            if (!context.isDestructible(block)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
