package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundLocationAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.magic.MagicBlock;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class FindMagicBlocksAction extends CompoundLocationAction {
    protected int radius;
    protected boolean allowCrossWorld;
    protected boolean targetBlockMage;
    protected Set<String> targetMagicBlocks;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context, context.getTargetEntity(), context.getTargetLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        radius = parameters.getInt("radius", 0);
        allowCrossWorld = parameters.getBoolean("target_all_worlds", true);
        targetBlockMage = parameters.getBoolean("target_block_mage", true);

        List<String> targetMagicBlockKeys = ConfigurationUtils.getStringList(parameters, "target_magic_blocks");
        if (targetMagicBlockKeys != null && !targetMagicBlockKeys.isEmpty()) {
            targetMagicBlocks = new HashSet<>(targetMagicBlockKeys);
        }

        Mage mage = context.getMage();
        radius = (int)(mage.getRadiusMultiplier() * radius);

        super.prepare(context, parameters);
    }

    @Override
    public void addLocations(CastContext context, List<Location> locations) {
        if (targetMagicBlocks == null) return;

        context.addWork(20);
        Mage mage = context.getMage();
        Location sourceLocation = context.getTargetLocation();
        if (mage.getDebugLevel() > 8) {
            mage.sendDebugMessage(ChatColor.GREEN + "Finding magic blocks from " + ChatColor.GRAY + sourceLocation.getBlockX()
                    + ChatColor.DARK_GRAY + ","  + ChatColor.GRAY + sourceLocation.getBlockY()
                    + ChatColor.DARK_GRAY + "," + ChatColor.GRAY + sourceLocation.getBlockZ()
                    + ChatColor.DARK_GREEN + " with radius of " + ChatColor.GREEN + radius, 14
            );
        }
        Collection<MagicBlock> magicBlocks = context.getController().getMagicBlocks();
        for (MagicBlock magicBlock : magicBlocks) {
            if (!targetMagicBlocks.contains(magicBlock.getTemplateKey())) continue;
            Location location = targetBlockMage ? magicBlock.getMage().getLocation() : magicBlock.getLocation();
            if (!allowCrossWorld || radius > 0) {
                if (location == null || location.getWorld() == null || !location.getWorld().equals(sourceLocation.getWorld())) continue;
            } else if (radius > 0) {
                if (location.distanceSquared(sourceLocation) > radius * radius) continue;
            }
            locations.add(location);
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("radius");
        parameters.add("target_all_worlds");
        parameters.add("target_magic_blocks");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else if (parameterKey.equals("target_all_worlds") || parameterKey.equals("target_block_mage")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
