package com.elmakers.mine.bukkit.world.populator.builtin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.populator.MagicBlockPopulator;

public class TowerPopulator extends MagicBlockPopulator {
    private double exitProbability;
    private List<MaterialAndData> wallBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        exitProbability = config.getDouble("exit_probability");
        MagicController controller = world.getController();
        MaterialSet wallSet = controller.getMaterialSetManager().fromConfig(config, "blocks");
        if (wallSet == null) {
            world.getLogger().warning("Invalid block set: " + config.getString("blocks") + ", defaulting to stones");
            wallSet = controller.getMaterialSetManager().getMaterialSet("stones");
        }
        if (wallSet != null) {
            wallBlocks = new ArrayList<>(wallSet.getMaterialsWithData());
        }
        return true;
    }

    private BlockData getWindowBlock() {
        BlockData gatewayData = getController().getPlugin().getServer().createBlockData(Material.END_GATEWAY);
        if (gatewayData instanceof EndGateway) {
            EndGateway gateway = (EndGateway)gatewayData;
            gateway.setAge(-Integer.MAX_VALUE);
        }
        return gatewayData;
    }

    @Override
    public void populate(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, LimitedRegion region) {
        final int maxHeight = worldInfo.getMaxHeight();
        final int chunkGlobalX = chunkX << 4;
        final int chunkGlobalZ = chunkZ << 4;
        final int floorLevel = 0; // room.getFloorLevel();

        final int buffer = region.getBuffer();
        final int minX = chunkGlobalX - buffer;
        final int maxX = chunkGlobalX + 16 + buffer;
        final int minZ = chunkGlobalZ - buffer;
        final int maxZ = chunkGlobalZ + 16 + buffer;
        final boolean isExit = random.nextDouble() < exitProbability;
        final int minExitX = chunkGlobalX + 6;
        final int maxExitX = chunkGlobalX + 9;
        final int minExitZ = chunkGlobalZ + 6;
        final int maxExitZ = chunkGlobalZ + 9;
        final int maxExitY = floorLevel + 6;

        final BlockData exitBlock = isExit ? getWindowBlock() : null;

        final MaterialAndData wallBlock = RandomUtils.getRandom(wallBlocks, random);
        final BlockData wallBlockData = wallBlock.createBlockData();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = floorLevel + 1; y < maxHeight; y++) {
                    if (exitBlock != null && y <= maxExitY && ((x >= minExitX && x <= maxExitX) || (z >= minExitZ && z <= maxExitZ))) {
                        if (x >= minExitX && x <= maxExitX && z >= minExitZ && z <= maxExitZ) {
                            region.setBlockData(x, y, z, exitBlock);
                        } else {
                            region.setType(x, y, z, Material.AIR);
                        }
                    } else {
                        region.setBlockData(x, y, z, wallBlockData);
                    }
                }

                if (exitBlock != null && x >= minExitX && x <= maxExitX && z >= minExitZ && z <= maxExitZ) {
                    region.setType(x, floorLevel, z, Material.END_PORTAL);
                }
            }
        }
    }
}
