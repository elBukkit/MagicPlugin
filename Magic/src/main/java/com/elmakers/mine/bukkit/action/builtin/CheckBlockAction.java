package com.elmakers.mine.bukkit.action.builtin;

import java.util.Set;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CheckBlockAction extends CheckAction {
    private MaterialSet allowed;
    private boolean useTarget;
    private BlockFace direction;
    private int directionCount;
    private boolean setTarget;
    private boolean allowBrush;
    private SourceLocation sourceLocation;
    private Set<Biome> allowedBiomes;
    private Set<Biome> notBiomes;
    private boolean checkPermissions;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);

        allowed = spell.getController().getMaterialSetManager()
                .fromConfig(parameters.getString("allowed"));
        allowedBiomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(parameters, "biomes"), spell.getController().getLogger(), "spell " + spell.getKey());
        notBiomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(parameters, "not_biomes"), spell.getController().getLogger(), "spell " + spell.getKey());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useTarget = parameters.getBoolean("use_target", true);
        setTarget = parameters.getBoolean("set_target", false);
        allowBrush = parameters.getBoolean("allow_brush", false);
        sourceLocation = new SourceLocation(parameters.getString("source_location", "BLOCK"), !useTarget);
        directionCount = parameters.getInt("direction_count", 1);
        checkPermissions = notBiomes == null && allowedBiomes == null && !allowBrush && allowed == null;
        checkPermissions = parameters.getBoolean("check_permission", checkPermissions);

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
        Block block = sourceLocation.getBlock(context);
        if (block == null) {
            return false;
        }
        if (direction != null) {
            for (int i = 0; i < directionCount; i++) {
                block = block.getRelative(direction);
            }
        }

        // Default to true
        boolean isAllowed = true;

        // Perform positive tests first
        if (allowed != null && !allowed.testBlock(block)) {
            isAllowed = false;
        }
        if (!isAllowed && checkPermissions) {
            isAllowed = true;
            if (brush != null && brush.isErase()) {
                if (!context.hasBreakPermission(block)) {
                    isAllowed = false;
                }
            } else {
                if (!context.hasBuildPermission(block)) {
                    isAllowed = false;
                }
            }
            if (!context.isDestructible(block)) {
                isAllowed = false;
            }
        }
        if (isAllowed && allowedBiomes != null && !allowedBiomes.contains(block.getBiome())) {
            isAllowed = false;
        }

        // Brush can override positive tests
        if (!isAllowed && allowBrush && brush != null && !brush.isDifferent(block)) {
            isAllowed = true;
        }

        // Negative tests override all
        if (isAllowed && notBiomes != null && notBiomes.contains(block.getBiome())) {
            isAllowed = false;
        }

        if (setTarget && isAllowed) {
            createActionContext(context, context.getEntity(), null, context.getTargetEntity(), block.getLocation());
        }

        return isAllowed;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
