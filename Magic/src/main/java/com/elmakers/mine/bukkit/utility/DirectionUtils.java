package com.elmakers.mine.bukkit.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.block.BlockFace;

public class DirectionUtils {

    public static final String[] EXAMPLE_DIRECTIONS = {"cardinal", "all", "plane", "neighbors", "up", "down", "north", "south", "east", "west"};

    private static final List<BlockFace> cardinalDirections = Arrays.asList(
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST,
        BlockFace.UP, BlockFace.DOWN
    );

    private static final List<BlockFace> neighborDirections = Arrays.asList(
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST
    );

    private static final List<BlockFace> allDirections = Arrays.asList(
        BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST,

        BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST,

        BlockFace.UP_NORTH, BlockFace.UP_EAST,
        BlockFace.UP_SOUTH, BlockFace.UP_WEST,

        BlockFace.UP_NORTH_EAST, BlockFace.UP_NORTH_WEST,
        BlockFace.UP_SOUTH_EAST, BlockFace.UP_SOUTH_WEST,

        BlockFace.DOWN_NORTH, BlockFace.DOWN_EAST,
        BlockFace.DOWN_SOUTH, BlockFace.DOWN_WEST,

        BlockFace.DOWN_NORTH_EAST, BlockFace.DOWN_NORTH_WEST,
        BlockFace.DOWN_SOUTH_EAST, BlockFace.DOWN_SOUTH_WEST
    );

    private static final List<BlockFace> planeDirections = Arrays.asList(
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST,
        BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
    );

    @Nullable
    public static BlockFace getBlockFace(String name) {
        try {
            return BlockFace.valueOf(name.toUpperCase());
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public static List<BlockFace> getDirections(String name) {
        if (name == null) {
            return null;
        }
        if (name.equalsIgnoreCase("cardinal")) {
            return cardinalDirections;
        }
        if (name.equalsIgnoreCase("all")) {
            return allDirections;
        }
        if (name.equalsIgnoreCase("plane")) {
            return planeDirections;
        }
        if (name.equalsIgnoreCase("neighbors")) {
            return neighborDirections;
        }
        BlockFace single = getBlockFace(name);
        if (single == null) return null;
        List<BlockFace> singleSet = new ArrayList<>();
        singleSet.add(single);
        return singleSet;
    }

    @Nonnull
    public static List<BlockFace> getDirections(ConfigurationSection parameters, String key) {
        List<BlockFace> faceSet = null;
        if (parameters.isString(key)) {
            String name = parameters.getString(key);
            faceSet = getDirections(name);
        }

        if (faceSet == null) {
            Collection<String> faceList = ConfigurationUtils.getStringList(parameters, key);
            if (faceList != null) {
                faceSet = new ArrayList<>();
                for (String face : faceList) {
                    faceSet.addAll(getDirections(face));
                }
            }
        }

        if (faceSet == null) {
            faceSet = cardinalDirections;
        }

        return faceSet;
    }
}
