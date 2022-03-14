package com.elmakers.mine.bukkit.action.builtin;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.action.CompoundLocationAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.TextUtils;

public class FindTileEntitiesAction extends CompoundLocationAction {
    protected int radius;

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        createActionContext(context, context.getTargetEntity(), context.getTargetLocation());
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        radius = parameters.getInt("radius", 0);
        Mage mage = context.getMage();
        radius = (int)(mage.getRadiusMultiplier() * radius);

        super.prepare(context, parameters);
    }

    @Override
    public void addLocations(CastContext context, List<Location> locations) {
        context.addWork(20);
        Mage mage = context.getMage();
        Location sourceLocation = context.getTargetLocation();
        if (mage.getDebugLevel() > 8) {
            mage.sendDebugMessage(ChatColor.GREEN + "Finding tile entities from " + ChatColor.GRAY + sourceLocation.getBlockX()
                    + ChatColor.DARK_GRAY + ","  + ChatColor.GRAY + sourceLocation.getBlockY()
                    + ChatColor.DARK_GRAY + "," + ChatColor.GRAY + sourceLocation.getBlockZ()
                    + ChatColor.DARK_GREEN + " with radius of " + ChatColor.GREEN + radius, 14
            );
        }

        World world = context.getWorld();
        int baseX = sourceLocation.getChunk().getX();
        int baseZ = sourceLocation.getChunk().getZ();
        int chunkRadius = (int)Math.ceil((double)radius / 16);
        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                Chunk chunk = world.getChunkAt(baseX + x, baseZ + z);
                BlockState[] blocks = chunk.getTileEntities();
                for (BlockState blockState : blocks) {
                    Block block = blockState.getBlock();
                        if (!context.isDestructible(block)) {
                        mage.sendDebugMessage(ChatColor.YELLOW + " Skipping "  + ChatColor.GOLD + block.getType() + ChatColor.GRAY + ", not destructible", 50);
                        continue;
                    }
                    mage.sendDebugMessage(ChatColor.AQUA + "Found " + ChatColor.DARK_AQUA + block.getType() + ChatColor.DARK_GRAY + " at " + TextUtils.printBlockLocation(block.getLocation()), 20);
                    locations.add(block.getLocation());
                }
            }
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("radius");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("radius")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_SIZES));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
