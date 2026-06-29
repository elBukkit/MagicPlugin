package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.utility.random.IntegerRange;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class RoomGenerator extends BaseChunkGenerator {
    private IntegerRange roofHeight;
    private IntegerRange doorwayHeight;
    private IntegerRange doorwayWidth;
    private IntegerRange hallwayWidth;
    private int yOffset;
    private double wallProbability = 0.75;
    private double windowProbability = 0.3;
    private double islandProbability = 0.75;
    private double poolProbability = 0.75;
    private double doubleDoorProbability = 0.5;
    private double sunroofProbability = 1;

    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private List<MaterialAndData> ceilingBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        roofHeight = IntegerRange.fromConfig(getLogger(), config, "roof_height", 4, 10);
        doorwayWidth = IntegerRange.fromConfig(getLogger(), config, "doorway_width", 0, 6);
        doorwayHeight = IntegerRange.fromConfig(getLogger(), config, "doorway_height", 2, 4);
        hallwayWidth = IntegerRange.fromConfig(getLogger(), config, "hallway_width", 0, 0);

        wallProbability = config.getDouble("wall_probability", wallProbability);
        windowProbability = config.getDouble("window_probability", windowProbability);
        islandProbability = config.getDouble("island_probability", islandProbability);
        poolProbability = config.getDouble("pool_probability", poolProbability);
        doubleDoorProbability = config.getDouble("double_door_probability", doubleDoorProbability);
        sunroofProbability = config.getDouble("sunroof_probability", sunroofProbability);

        wallBlocks = parseBlocks(config, "wall_blocks", "stones");
        ceilingBlocks = parseBlocks(config, "ceiling_blocks", "stones");
        yOffset = config.getInt("y_offset", 0);

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
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkGenerator.ChunkData chunk) {
        final int floorLevel = world.getGroundLevel() + yOffset;
        final int roofLevel = floorLevel + roofHeight.getRandom(random);
        final int roofMaxLevel = floorLevel + roofHeight.getMax();
        final int doorwayLevel = Math.min(roofLevel, floorLevel + doorwayHeight.getRandom(random));
        final int doorwayWidthHalf = (int)Math.ceil((double)doorwayWidth.getRandom(random) / 2);
        final int hallwayWidthHalf = (int)Math.ceil((double)hallwayWidth.getRandom(random) / 2);
        final int doorwayLeft = 7 - doorwayWidthHalf;
        final int doorwayRight = 9 + doorwayWidthHalf;
        final boolean canHaveWindow = doorwayWidthHalf < 4;
        int xWindowLocation = 0;
        int zWindowLocation = 0;
        if (canHaveWindow) {
            xWindowLocation = random.nextInt(4 - doorwayWidthHalf) + 1;
            if (random.nextDouble() > 0.5) xWindowLocation = 15 - xWindowLocation;
            zWindowLocation = random.nextInt(4 - doorwayWidthHalf) + 1;
            if (random.nextDouble() > 0.5) zWindowLocation = 15 - zWindowLocation;
        }
        final boolean hasXWall = random.nextDouble() < wallProbability;
        final boolean hasZWall = random.nextDouble() < wallProbability;
        final boolean hasXWindow = canHaveWindow && random.nextDouble() < windowProbability;
        final boolean hasZWindow = canHaveWindow && random.nextDouble() < windowProbability;
        final boolean hasSunRoof = random.nextDouble() < sunroofProbability;
        final boolean hasDoubleDoor = random.nextDouble() < doubleDoorProbability;
        final boolean doorXSide = random.nextDouble() < 0.5;
        final boolean hasXDoor = hasDoubleDoor || doorXSide;
        final boolean hasZDoor = hasDoubleDoor || !doorXSide;
        final BlockData wallBlock = RandomUtils.getRandom(wallBlocks, random).createBlockData();
        final BlockData ceilingBlock = RandomUtils.getRandom(ceilingBlocks, random).createBlockData();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final boolean isSunRoof = hasSunRoof && x >= 7 && z >= 7 && x <= 9 && z <= 9;
                if (x == 0 || z == 0) {
                    if ((hasXWall && z == 0) || (hasZWall && x == 0)) {
                        // Walls and doorway
                        boolean isDoorway = (x >= doorwayLeft && x <= doorwayRight) || (z >= doorwayLeft && z <= doorwayRight);
                        if (!hasXDoor && z == 0) isDoorway = false;
                        else if (!hasZDoor && x == 0) isDoorway = false;
                        for (int y = floorLevel + 1; y <= roofMaxLevel; y++) {
                            if (isDoorway && y <= doorwayLevel) continue;
                            chunk.setBlock(x, y, z, wallBlock);
                        }
                    } else {
                        for (int y = roofLevel; y <= roofMaxLevel; y++) {
                            chunk.setBlock(x, y, z, wallBlock);
                        }
                    }
                } else if (x == 1 || z == 1 || x == 15 || z == 15) {
                    // Border around sunroof
                    chunk.setBlock(x, roofLevel, z, ceilingBlock);
                } else if (!isSunRoof) {
                    // Roof
                    chunk.setBlock(x, roofLevel, z, ceilingBlock);
                }

                // Extend ceiling up
                if (!isSunRoof) {
                    for (int y = roofLevel + 1; y <= roofMaxLevel; y++) {
                        chunk.setBlock(x, y, z, ceilingBlock);
                    }
                }

                // Fill in windows after
                if (hasXWall && hasXWindow && x == xWindowLocation && z == 0) {
                    chunk.setBlock(x, floorLevel + 2, z, getWindowBlock());
                } else if (hasZWall && hasZWindow && z == zWindowLocation && x == 0) {
                    chunk.setBlock(x, floorLevel + 2, z, getWindowBlock());
                }

                // Fill in hallways after
                if (hallwayWidthHalf > 0) {
                    int hallwayLeft = 8 - hallwayWidthHalf;
                    int hallwayRight = 8 + hallwayWidthHalf;
                    if (x < hallwayLeft || x > hallwayRight || z < hallwayLeft || z > hallwayRight) {
                        for (int y = floorLevel; y <= roofLevel; y++) {
                            chunk.setBlock(x, y, z, wallBlock);
                        }
                    }
                }
            }
        }
    }
}
