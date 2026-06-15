package com.elmakers.mine.bukkit.world.generator.builtin;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.EndGateway;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.GlowLichen;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;
import com.elmakers.mine.bukkit.world.generator.BaseChunkGenerator;

public class PoolsGenerator extends BaseChunkGenerator {
    private int bedrockLevel = 60;
    private int floorLevel = 62;
    private int roofMinHeight = 4;
    private int roofMaxHeight = 10;
    private int doorwayMinHeight = 2;
    private int doorwayMaxHeight = 4;
    private int doorwayMinWidthHalf = 0;
    private int doorwayMaxWidthHalf = 3;
    private int walkwayMaxWidthHalf = 5;
    private int hallwayMaxWidthHalf = 0;
    private int hallwayMinWidthHalf = 0;
    private double wallProbability = 0.75;
    private double windowProbability = 0.3;
    private double islandProbability = 0.75;
    private double poolProbability = 0.75;
    private double doubleDoorProbability = 0.5;
    private double lightProbability = 1;
    private double floorLightProbability = 0;
    private double sunroofProbability = 1;
    private double floodingProbability = 0;
    private int floodingMinLevel = 1;
    private int floodingMaxLevel = 6;
    private int waterHeightMax = 0;
    private int waterHeightMin = 0;
    private int waterDepthMax = 1;

    private List<MaterialAndData> floorBlocks = Collections.emptyList();
    private List<MaterialAndData> wallBlocks = Collections.emptyList();
    private List<MaterialAndData> ceilingBlocks = Collections.emptyList();
    private List<MaterialAndData> lightBlocks = Collections.emptyList();

    @Override
    public boolean onLoad(ConfigurationSection config) {
        bedrockLevel = config.getInt("bedrock_level", bedrockLevel);
        floorLevel = config.getInt("floor_level", floorLevel);
        roofMinHeight = config.getInt("roof_min_height", roofMinHeight);
        roofMaxHeight = config.getInt("roof_max_height", roofMaxHeight);
        doorwayMinHeight = config.getInt("doorway_min_height", doorwayMinHeight);
        doorwayMaxHeight = config.getInt("doorway_max_height", doorwayMaxHeight);
        doorwayMaxWidthHalf = config.getInt("doorway_max_width_half", doorwayMaxWidthHalf);
        doorwayMinWidthHalf = config.getInt("doorway_min_width_half", doorwayMinWidthHalf);
        walkwayMaxWidthHalf = config.getInt("walkway_max_width_half", walkwayMaxWidthHalf);
        wallProbability = config.getDouble("wall_probability", wallProbability);
        windowProbability = config.getDouble("window_probability", windowProbability);
        islandProbability = config.getDouble("island_probability", islandProbability);
        poolProbability = config.getDouble("pool_probability", poolProbability);
        doubleDoorProbability = config.getDouble("double_door_probability", doubleDoorProbability);
        lightProbability = config.getDouble("light_probability", lightProbability);
        floorLightProbability = config.getDouble("floor_light_probability", floorLightProbability);
        hallwayMaxWidthHalf = config.getInt("hallway_max_width_half", hallwayMaxWidthHalf);
        hallwayMinWidthHalf = config.getInt("hallway_min_width_half", hallwayMinWidthHalf);
        sunroofProbability = config.getDouble("sunroof_probability", sunroofProbability);
        floodingProbability = config.getDouble("flooding_probability", floodingProbability);
        floodingMinLevel = config.getInt("flooding_min_level", floodingMinLevel);
        floodingMaxLevel = config.getInt("flooding_mx_level", floodingMaxLevel);
        waterDepthMax =  config.getInt("water_depth_max", waterDepthMax);
        waterHeightMax = config.getInt("water_height_max", waterHeightMax);
        waterHeightMin = config.getInt("water_height_min", waterHeightMin);

        floorBlocks = parseBlocks(config, "floor_blocks", "concretes");
        wallBlocks = parseBlocks(config, "wall_blocks", "stones");
        ceilingBlocks = parseBlocks(config, "ceiling_blocks", "stones");
        lightBlocks = parseBlocks(config, "light_blocks", "all_lights");

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
        final MagicController controller = getController();
        final boolean isStartingChunk = chunkX == 0 && chunkZ == 0;
        final int floorLevel = this.floorLevel;
        final int roofLevel = floorLevel + RandomUtils.range(random, roofMinHeight, roofMaxHeight);
        final int roofMaxLevel = floorLevel + roofMaxHeight;
        final int doorwayLevel = Math.min(roofLevel, floorLevel + RandomUtils.range(random, doorwayMinHeight, doorwayMaxHeight));
        final int doorwayWidthHalf = RandomUtils.range(random, doorwayMinWidthHalf, doorwayMaxWidthHalf);
        final int waterHeight = RandomUtils.range(random, waterHeightMin, waterHeightMax);
        final int doorwayLeft = 7 - doorwayWidthHalf;
        final int doorwayRight = 9 + doorwayWidthHalf;
        final int walkwayWidthHalf = isStartingChunk ? 0 : random.nextInt(walkwayMaxWidthHalf);
        final int walkwayLeft = 8 - walkwayWidthHalf;
        final int walkWayRight = 8 + walkwayWidthHalf;
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
        final boolean hasIsland = !isStartingChunk && random.nextDouble() < islandProbability;
        final boolean hasPools = random.nextDouble() < poolProbability;
        final boolean hasSunRoof = isStartingChunk || random.nextDouble() < sunroofProbability;
        final boolean hasDoubleDoor = random.nextDouble() < doubleDoorProbability;
        final boolean doorXSide = random.nextDouble() < 0.5;
        final boolean hasXDoor = hasDoubleDoor || doorXSide;
        final boolean hasZDoor = hasDoubleDoor || !doorXSide;
        final BlockData floorBlock = RandomUtils.getRandom(floorBlocks, random).createBlockData();
        final BlockData wallBlock = RandomUtils.getRandom(wallBlocks, random).createBlockData();
        final BlockData ceilingBlock = RandomUtils.getRandom(ceilingBlocks, random).createBlockData();
        final BlockData lightBlock = RandomUtils.getRandom(lightBlocks, random).createBlockData();
        final int lightsFirst = walkwayLeft / 2 + 1;
        final int lightsSecond = 16 - lightsFirst;
        final boolean isFlooded = hasSunRoof && random.nextDouble() < floodingProbability;
        final boolean hasFloorLights = random.nextDouble() < floorLightProbability;
        final int waterMinY = floorLevel - waterDepthMax;
        final int lightY = waterMinY;
        Levelled floodWater = null;
        if (isFlooded) {
            int floodLevel = RandomUtils.range(random, floodingMinLevel, floodingMaxLevel);
            floodWater = (Levelled)controller.getPlugin().getServer().createBlockData(Material.WATER);
            floodWater.setLevel(floodLevel);
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final BlockData waterBlock = Material.WATER.createBlockData();
                final boolean hasLight = random.nextDouble() < lightProbability;
                final BlockData lightMaterial = hasLight ? lightBlock : floorBlock;

                // Fill in the sub-floor first
                for (int y = bedrockLevel + 1; y < this.floorLevel; y++) {
                    chunk.setBlock(x, y, z, floorBlock);
                }

                final boolean isSunRoof = hasSunRoof && x >= 7 && z >= 7 && x <= 9 && z <= 9;
                final boolean isWalkway = (x > walkwayLeft && x < walkWayRight) || (z > walkwayLeft && z < walkWayRight);
                if (x == 0 || z == 0) {
                    chunk.setBlock(x, floorLevel, z, floorBlock);
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
                    // Border walkway
                    chunk.setBlock(x, floorLevel, z, floorBlock);
                    chunk.setBlock(x, roofLevel, z, ceilingBlock);
                } else if (isWalkway) {
                    // Pathways
                    if (!isSunRoof) {
                        chunk.setBlock(x, roofLevel, z, ceilingBlock);
                    }
                    chunk.setBlock(x, floorLevel, z, floorBlock);
                    if (waterHeight > 0) {
                        final int maxWaterHeight = Math.min(floorLevel + waterHeight, roofLevel);
                        for (int y = floorLevel + 1; y < maxWaterHeight; y++) {
                            final BlockData waterData = floodWater != null && y == maxWaterHeight - 1 ? floodWater : waterBlock;
                            chunk.setBlock(x, y, z, waterData);
                        }
                    } else if (floodWater != null) {
                        chunk.setBlock(x, floorLevel + 1, z, floodWater);
                    }
                } else if (isSunRoof) {
                    // Island
                    if (!hasIsland) {
                        for (int y = floorLevel; y > waterMinY; y--) {
                            chunk.setBlock(x, y, z, waterBlock);
                        }
                        if (x == 8 && z == 8) {
                            chunk.setBlock(x, lightY, z, lightMaterial);
                        }
                    } else {
                        chunk.setBlock(x, floorLevel, z, floorBlock);
                    }
                } else {
                    // Water and roof
                    chunk.setBlock(x, roofLevel, z, ceilingBlock);
                    final boolean isCenterLight = (x == lightsFirst || x == lightsSecond) && (z == lightsFirst || z == lightsSecond);
                    if (hasPools) {
                        for (int y = floorLevel; y > waterMinY; y--) {
                            chunk.setBlock(x, y, z, waterBlock);
                        }
                        if (isCenterLight) {
                            chunk.setBlock(x, lightY, z, lightMaterial);
                        }
                    } else if (hasFloorLights && isCenterLight) {
                        chunk.setBlock(x, floorLevel, z, lightMaterial);
                    } else {
                        chunk.setBlock(x, floorLevel, z, floorBlock);
                    }
                }

                // Extend ceiling up
                if (!isSunRoof) {
                    final int ceilingHeight = isStartingChunk ? worldInfo.getMaxHeight() : roofMaxLevel;
                    for (int y = roofLevel + 1; y <= ceilingHeight; y++) {
                        chunk.setBlock(x, y, z, ceilingBlock);
                    }
                }

                // Add a dim light if no sunroof so it's not 100% dark
                if (!hasSunRoof && x == 8 && z == 8) {
                    GlowLichen dimLight = (GlowLichen)controller.getPlugin().getServer().createBlockData(Material.GLOW_LICHEN);
                    dimLight.setFace(BlockFace.DOWN, true);
                    BlockData centerBlock = chunk.getBlockData(x, floorLevel, z);
                    boolean isWaterlogged = centerBlock instanceof Waterlogged && ((Waterlogged)centerBlock).isWaterlogged();
                    if (centerBlock.getMaterial() == Material.WATER || isWaterlogged) {
                        dimLight.setWaterlogged(true);
                        chunk.setBlock(x, floorLevel, z, dimLight);
                    } else {
                        chunk.setBlock(x, floorLevel + 1, z, dimLight);
                    }
                }

                // Fill in windows after
                if (hasXWall && hasXWindow && x == xWindowLocation && z == 0) {
                    chunk.setBlock(x, floorLevel + 2, z, getWindowBlock());
                } else if (hasZWall && hasZWindow && z == zWindowLocation && x == 0) {
                    chunk.setBlock(x, floorLevel + 2, z, getWindowBlock());
                }

                // Fill in hallways after
                boolean isCenterWalkway = isWalkway || x == 8 || z == 8;
                if (!isCenterWalkway && (hallwayMaxWidthHalf > 0 || hallwayMinWidthHalf > 0)) {
                    int hallwayWidthHalf = RandomUtils.range(random, hallwayMinWidthHalf, hallwayMaxWidthHalf);
                    int hallwayLeft = 8 - hallwayWidthHalf;
                    int hallwayRight = 8 + hallwayWidthHalf;
                    if (isStartingChunk) {
                        hallwayLeft = Math.min(hallwayLeft, 6);
                        hallwayRight = Math.max(hallwayRight, 10);
                    }
                    if (x < hallwayLeft || x > hallwayRight || z < hallwayLeft || z > hallwayRight) {
                        for (int y = waterMinY; y <= roofLevel; y++) {
                            chunk.setBlock(x, y, z, wallBlock);
                        }
                    }
                }
            }
        }
    }
}
