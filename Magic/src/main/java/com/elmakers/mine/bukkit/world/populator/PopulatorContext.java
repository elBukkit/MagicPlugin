package com.elmakers.mine.bukkit.world.populator;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.utility.BoundingBox;
import com.elmakers.mine.bukkit.utility.IntVector;
import com.elmakers.mine.bukkit.world.SearchType;
import com.elmakers.mine.bukkit.world.WorldInfoContext;

public class PopulatorContext extends WorldInfoContext {
    private final @NotNull LimitedRegion region;

    public PopulatorContext(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull LimitedRegion region
    ) {
        this(worldInfo, random, chunkX, chunkZ, region, IntVector.ZERO);
    }

    protected PopulatorContext(
        @NotNull WorldInfo worldInfo,
        @NotNull Random random,
        int chunkX,
        int chunkZ,
        @NotNull LimitedRegion region,
        int yOffset
    ) {
        this(worldInfo, random, chunkX, chunkZ, region, new IntVector(0, yOffset, 0));
    }

    protected PopulatorContext(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull LimitedRegion region,
            IntVector offset
    ) {
        super(worldInfo, random, chunkX, chunkZ, offset);
        this.region = region;
    }

    protected PopulatorContext(PopulatorContext base, int yOffset) {
        this(base.worldInfo, base.random, base.chunkX, base.chunkZ, base.region, base.offset.addY(yOffset));
    }

    protected PopulatorContext(PopulatorContext base, IntVector offset) {
        this(base.worldInfo, base.random, base.chunkX, base.chunkZ, base.region,  base.offset.add(offset));
    }

    public PopulatorContext offsetY(int yOffset) {
        if (yOffset == 0) {
            return this;
        }
        return new PopulatorContext(this, yOffset);
    }

    public PopulatorContext offset(IntVector offset) {
        if (offset.isZero()) {
            return this;
        }
        return new PopulatorContext(this, offset);
    }

    public Material getType(int x, int y, int z) {
        return region.getType(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public boolean setType(int x, int y, int z, @NotNull Material material) {
        return setBlockData(x, y, z, material.createBlockData());
    }

    public boolean setBlockData(int x, int y, int z, @NotNull BlockData blockData) {
        x = getWorldX(x);
        y = getWorldY(y);
        z = getWorldZ(z);
        if (!region.isInRegion(x, y, z)) {
            return false;
        }
        region.setBlockData(x, y, z, blockData);
        return true;
    }

    public BlockData getBlockData(int x, int y, int z) {
        return region.getBlockData(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public BlockState getBlockState(int x, int y, int z) {
        return region.getBlockState(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public int searchBlock(int x, int y, int z, int maxSearch, int direction, boolean throughAir, boolean returnAir) {
        return searchBlock(x, y, z, maxSearch, direction, throughAir, returnAir, material -> material.isAir());
    }

    protected int searchBlock(int x, int y, int z, int maxSearch, int direction, boolean throughBlocks, boolean returnBlocks, Predicate<Material> materialPredicate) {
        // y location isn't offset, the offset is applied after this search
        x = getWorldX(x);
        z = getWorldZ(z);
        if (!region.isInRegion(x, y, z)) {
            return y;
        }
        int blockCount = 0;
        Material material = region.getType(x, y, z);
        while (((direction > 0 && y < worldInfo.getMaxHeight() - 1) || (direction < 0 && y > worldInfo.getMinHeight() + 1))
                && materialPredicate.test(material) == throughBlocks
                && blockCount < maxSearch) {
            y += direction;
            blockCount++;
            material = region.getType(x, y, z);
        }
        return throughBlocks == returnBlocks ? y - direction : y;
    }

    public int getTopBlock(int x, int y, int z) {
        return searchBlock(x, y, z, worldInfo.getMaxHeight() - worldInfo.getMinHeight(), 1, false, false);
    }

    public int searchArea(final SearchType searchType, int startX, int startY, int startZ, int endX, int endZ) {
        if (searchType == SearchType.CONFORM_UP || searchType == SearchType.NONE) {
            return startY;
        }

        Integer currentSearch = null;
        for (int x = startX; x < endX; x++) {
            for (int z = startZ; z < endZ; z++) {
                final int searchY = getTopBlock(x, startY, z);
                if (currentSearch == null || (searchType != SearchType.AREA_UP_MAX && searchType != SearchType.AREA_UP_MIN)) {
                    currentSearch = searchY;
                } else {
                    currentSearch = searchType == SearchType.AREA_UP_MAX ? Math.max(searchY, currentSearch) : Math.min(searchY, currentSearch);
                }
                if (searchType == SearchType.BASE_UP) break;
            }
            if (searchType == SearchType.BASE_UP) break;
        }
        return currentSearch != null ? currentSearch : startY;
    }

    public int continueSearchArea(final SearchType searchType, int x, int y, int z, int startY) {
        if (searchType != SearchType.CONFORM_UP) {
            return y;
        }
        return searchArea(SearchType.BASE_UP, x, startY, z, x + 1, z + 1);
    }

    public PopulatorContext fit(int sizeX, int sizeZ) {
        int x = Math.min(offset.x(), 16 - sizeX);
        int z = Math.min(offset.z(), 16 - sizeZ);
        if (x == offset.x() && z == offset.z()) {
            return this;
        }
        return new PopulatorContext(worldInfo, random, chunkX, chunkZ, region, new IntVector(x, offset.y(), z));
    }

    public Biome getBiome(int x, int y, int z) {
        return region.getBiome(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public List<BlockState> getTileEntities() {
        return region.getTileEntities();
    }

    public boolean isCenterChunk(Chunk chunk) {
        // Note: Be careful when using this to not cause circular chunk loading during world gen
        return chunk.getX() == chunkX && chunk.getZ() == chunkZ;
    }

    public boolean isInCenterChunk(BlockState block) {
        return isInCenterChunk(block.getX(), block.getZ());
    }

    public boolean isInCenterChunk(Block block) {
        return isInCenterChunk(block.getX(), block.getZ());
    }

    public boolean isInCenterChunk(int x, int z) {
        return x >> 4 == chunkX && z >> 4 == chunkZ;
    }

    public int getSolidFloorOrCeiling(boolean isFloor, int x, int y, int z) {
        final int topY = getTopSolidBlock(x, y, z);
        final int solidY;
        if (isFloor) {
            solidY = topY;
        } else {
            solidY = getSolidBlockAbove(x, topY, z);
        }
        return solidY;
    }

    protected int getTopSolidBlock(int x, int y, int z) {
        return searchBlock(x, y, z, worldInfo.getMaxHeight() - worldInfo.getMinHeight(), 1, true, true, material -> material.isSolid());
    }

    protected int getSolidBlockAbove(int x, int y, int z) {
        return searchBlock(x, y + 1, z, worldInfo.getMaxHeight() - worldInfo.getMinHeight(), 1, true, false, material -> !material.isSolid());
    }

    public boolean isInRegion(int x, int y, int z) {
        return region.isInRegion(getWorldX(x), getWorldY(y), getWorldZ(z));
    }

    public World getWorld() {
        return Bukkit.getWorld(worldInfo.getName());
    }

    public Entity spawn(EntityData entityData, int x, int y, int z) {
        final World world = getWorld();
        if (world == null) return null;

        Entity entity = entityData.create(new Location(world, getWorldX(x), getWorldY(y), getWorldZ(z)));
        if (entity != null) {
            region.addEntity(entity);
        }
        return entity;
    }

    public boolean generateTree(int x, int y, int z, @NotNull Random random, @NotNull TreeType treeType) {
        final World world = getWorld();
        if (world == null) return false;
        region.generateTree(new Location(world, getWorldX(x), getWorldY(y), getWorldZ(z)), random, treeType);
        return true;
    }

    public BoundingBox getBounds() {
        final int buffer = region.getBuffer();
        final int minX = -buffer - offset.x();
        final int maxX = buffer * 2 - offset.x();
        final int minZ = -buffer - offset.z();
        final int maxZ = buffer * 2 - offset.z();
        return new BoundingBox(minX, maxX, getMinHeight(), getMaxHeight(), minZ, maxZ);
    }
}
