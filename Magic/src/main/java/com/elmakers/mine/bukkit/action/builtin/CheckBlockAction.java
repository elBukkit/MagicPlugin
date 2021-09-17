package com.elmakers.mine.bukkit.action.builtin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CheckAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialBrush;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.magic.SourceLocation;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class CheckBlockAction extends CheckAction {
    private MaterialSet allowed;
    private boolean useTarget;
    private BlockFace direction;
    private int directionCount;
    private boolean setTarget;
    private boolean allowBrush;
    private boolean notBrush;
    private SourceLocation sourceLocation;
    private Set<Biome> allowedBiomes;
    private Set<Biome> notBiomes;
    private Map<Biome, String> biomeActions;
    private Map<Material, String> blockActions;
    private boolean checkPermissions;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        useTarget = parameters.getBoolean("use_target", true);
        allowed = spell.getController().getMaterialSetManager()
                .fromConfig(parameters.getString("allowed"));
        allowedBiomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(parameters, "biomes"), spell.getController().getLogger(), "spell " + spell.getKey());
        notBiomes = ConfigurationUtils.loadBiomes(ConfigurationUtils.getStringList(parameters, "not_biomes"), spell.getController().getLogger(), "spell " + spell.getKey());
        ConfigurationSection biomeActionConfig = ConfigurationUtils.getConfigurationSection(parameters, "biome_actions");
        if (biomeActionConfig != null) {
            biomeActions = new HashMap<>();
            for (String biomeKey : biomeActionConfig.getKeys(false)) {
                try {
                    Biome biome = Biome.valueOf(biomeKey.trim().toUpperCase());
                    biomeActions.put(biome, biomeActionConfig.getString(biomeKey));
                } catch (Exception ex) {
                    spell.getController().getLogger().warning("Invalid biome in biome_actions config: " + biomeKey);
                }
            }
        }

        ConfigurationSection blockActionConfig = ConfigurationUtils.getConfigurationSection(parameters, "block_actions");
        if (blockActionConfig != null) {
            blockActions = new HashMap<>();
            for (String blockKey : blockActionConfig.getKeys(false)) {
                try {
                    Material blockType = Material.valueOf(blockKey.trim().toUpperCase());
                    blockActions.put(blockType, blockActionConfig.getString(blockKey));
                } catch (Exception ex) {
                    spell.getController().getLogger().warning("Invalid block type in block_actions config: " + blockKey);
                }
            }
        }

        // Have to do this after initializing the action map above since super.initialize calls addHandlers
        super.initialize(spell, parameters);
    }

    @Override
    protected void addHandlers(Spell spell, ConfigurationSection parameters) {
        super.addHandlers(spell, parameters);
        if (blockActions != null) {
            for (String handler : blockActions.values()) {
                addHandler(spell, handler);
            }
        }
        if (biomeActions != null) {
            for (String handler : biomeActions.values()) {
                addHandler(spell, handler);
            }
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        useTarget = parameters.getBoolean("use_target", true);
        setTarget = parameters.getBoolean("set_target", false);
        allowBrush = parameters.getBoolean("allow_brush", false);
        notBrush = parameters.getBoolean("not_brush", false);
        sourceLocation = new SourceLocation(parameters.getString("source_location", "BLOCK"), !useTarget);
        directionCount = parameters.getInt("direction_count", 1);
        checkPermissions = notBiomes == null && allowedBiomes == null && !allowBrush && !notBrush && allowed == null;
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
    public SpellResult step(CastContext context) {
        String actionHandler = null;
        if (blockActions != null || biomeActions != null) {
            Block block = getTargetBlock(context);
            if (block == null) {
                return SpellResult.NO_TARGET;
            }
            if (blockActions != null) {
                actionHandler = blockActions.get(block.getType());
            }
            if (biomeActions != null && actionHandler == null) {
                actionHandler = biomeActions.get(block.getBiome());
            }
        }
        if (actionHandler != null)  {
            return startActions(actionHandler);
        }
        return super.step(context);
    }

    private Block getTargetBlock(CastContext context) {
        Block block = sourceLocation.getBlock(context);
        if (block != null && direction != null) {
            for (int i = 0; i < directionCount; i++) {
                block = block.getRelative(direction);
            }
        }
        return block;
    }

    @Override
    protected boolean isAllowed(CastContext context) {
        MaterialBrush brush = context.getBrush();
        Block block = getTargetBlock(context);
        if (block == null) {
            return false;
        }

        if (brush != null && (notBrush || allowBrush)) {
            brush.update(context.getMage(), context.getTargetSourceLocation());
        }

        // Default to true
        boolean isAllowed = true;

        // Perform positive tests first
        if (allowed != null && !allowed.testBlock(block)) {
            isAllowed = false;
        }
        if (checkPermissions) {
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
        if (isAllowed && notBrush && brush != null && !brush.isDifferent(block)) {
            isAllowed = false;
        }

        if (setTarget && isAllowed) {
            createActionContext(context, context.getEntity(), null, context.getTargetEntity(), block.getLocation());
        }

        return isAllowed;
    }

    @Override
    public boolean requiresTarget() {
        return useTarget;
    }
}
