package com.elmakers.mine.bukkit.utility.platform.base;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SchematicUtils;
import com.elmakers.mine.bukkit.utility.schematic.LoadableSchematic;
import com.google.common.primitives.Bytes;

public class SchematicUtilsBase implements SchematicUtils {
    protected final Platform platform;

    protected SchematicUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean saveSchematic(OutputStream output, String[][][] blockData) {
        if (output == null || blockData == null || blockData.length == 0 || blockData[0].length == 0 || blockData[0][0].length == 0) {
            return false;
        }

        com.elmakers.mine.bukkit.utility.platform.NBTUtils nbtUtils = platform.getNBTUtils();
        Object nbtData = nbtUtils.newCompoundTag();
        int width = blockData.length;
        int height = blockData[0].length;
        int length = blockData[0][0].length;
        nbtUtils.setMetaShort(nbtData, "Width", (short)width);
        nbtUtils.setMetaShort(nbtData, "Height", (short)height);
        nbtUtils.setMetaShort(nbtData, "Length", (short)length);

        // Iterate through blocks and build varint data list and block palette
        Map<String, Integer> paletteLookup = new HashMap<>();
        List<Byte> blockList = new ArrayList<>();
        int currentId = 0;
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    String block = blockData[x][y][z];
                    Integer paletteIndex = paletteLookup.get(block);
                    if (paletteIndex == null) {
                        paletteIndex = currentId++;
                        paletteLookup.put(block, paletteIndex);
                    }
                    while (paletteIndex > 128) {
                        // This is probably wrong ...
                        // TODO: test with a variety of blocks in a schematic!
                        blockList.add((byte)((paletteIndex & 127) | 128));
                        paletteIndex >>= 7;
                    }
                    blockList.add((byte)(int)paletteIndex);
                }
            }
        }

        // Save palette to NBT
        Object palette = nbtUtils.newCompoundTag();
        for (Map.Entry<String, Integer> entry : paletteLookup.entrySet()) {
            nbtUtils.setInt(palette, entry.getKey(), entry.getValue());
        }
        nbtUtils.setTag(nbtData, "Palette", palette);

        // Save block list to NBT
        byte[] blockArray = Bytes.toArray(blockList);
        nbtUtils.setByteArray(nbtData, "BlockData", blockArray);

        // Add empty lists so they exist
        nbtUtils.setEmptyList(nbtData, "Entities");
        nbtUtils.setEmptyList(nbtData, "BlockEntities");
        int[] offset = {0, 0, 0};
        nbtUtils.setIntArray(nbtData, "Offset", offset);
        return nbtUtils.writeTagToStream(nbtData, output);
    }

    @Override
    public boolean loadSchematic(InputStream input, LoadableSchematic schematic, Logger log) {
        if (input == null || schematic == null) return false;

        com.elmakers.mine.bukkit.utility.platform.NBTUtils nbtUtils = platform.getNBTUtils();
        try {
            Object nbtData = nbtUtils.readTagFromStream(input);
            if (nbtData == null) {
                return false;
            }

            short width = nbtUtils.getShort(nbtData, "Width", (short)0);
            short height = nbtUtils.getShort(nbtData, "Height", (short)0);
            short length = nbtUtils.getShort(nbtData, "Length", (short)0);

            Object palette = nbtUtils.getTag(nbtData,"Palette");
            byte[] blockData = nbtUtils.getByteArray(nbtData, "BlockData");
            int[] blockMap = null;
            Map<Integer, String> paletteMap = null;

            if (palette != null) {
                // Map the palette
                paletteMap = new HashMap<>();
                Set<String> keys = nbtUtils.getAllKeys(palette);
                for (String key : keys) {
                    int index = nbtUtils.getInt(palette, key, 0);
                    paletteMap.put(index, key);
                }
            }
            if (blockData != null) {
                int varInt = 0;
                int varIntLength = 0;
                int index = 0;
                blockMap = new int[width * height * length];

                for (int i = 0; i < blockData.length; i++) {
                    varInt |= (blockData[i] & 127) << (varIntLength++ * 7);

                    if ((blockData[i] & 128) == 128) {
                        continue;
                    }

                    blockMap[index++] = varInt;
                    varIntLength = 0;
                    varInt = 0;
                }
                if (index != blockMap.length) {
                    log.warning("Block data array length does not match dimensions in schematic");
                }
            }

            // Load entities
            Collection<Object> entityData = nbtUtils.getTagList(nbtData, "Entities");
            Collection<Object> tileEntityData;
            if (nbtUtils.contains(nbtData, "BlockEntities")) {
                tileEntityData = nbtUtils.getTagList(nbtData, "BlockEntities");
            } else {
                tileEntityData = nbtUtils.getTagList(nbtData, "TileEntities");
            }

            Vector origin = new Vector(0, 0, 0);
            int[] offset = nbtUtils.getIntArray(nbtData, "Offset");
            if (offset != null && offset.length == 3) {
                origin.setX(offset[0]);
                origin.setY(offset[1]);
                origin.setZ(offset[2]);
            }
            schematic.load(width, height, length, blockMap, null, paletteMap, tileEntityData, entityData, origin);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean loadLegacySchematic(InputStream input, LoadableSchematic schematic) {
        if (input == null || schematic == null) return false;

        com.elmakers.mine.bukkit.utility.platform.NBTUtils nbtUtils = platform.getNBTUtils();
        try {
            Object nbtData = nbtUtils.readTagFromStream(input);
            if (nbtData == null) {
                return false;
            }

            // Version check
            String materials = nbtUtils.getString(nbtData, "Materials");
            if (!materials.equals("Alpha")) {
                Bukkit.getLogger().warning("Schematic is not in Alpha format");
                return false;
            }

            short width = nbtUtils.getShort(nbtData, "Width", (short)0);
            short height = nbtUtils.getShort(nbtData, "Height", (short)0);
            short length = nbtUtils.getShort(nbtData, "Length", (short)0);

            byte[] blockIds = nbtUtils.getByteArray(nbtData, "Blocks");

            // Have to combine block ids to get 12 bits of ids
            // Thanks to the WorldEdit team for showing me how to do this.
            int[] blocks = new int[blockIds.length];
            byte[] addBlocks = new byte[0];
            if (nbtUtils.contains(nbtData, "AddBlocks")) {
                addBlocks = nbtUtils.getByteArray(nbtData,"AddBlocks");
            }
            for (int index = 0; index < blocks.length; index++) {
                if ((index >> 1) >= addBlocks.length) {
                    blocks[index] = (short) (blockIds[index] & 0xFF);
                } else {
                    if ((index & 1) == 0) {
                        blocks[index] = (short) (((addBlocks[index >> 1] & 0x0F) << 8) + (blockIds[index] & 0xFF));
                    } else {
                        blocks[index] = (short) (((addBlocks[index >> 1] & 0xF0) << 4) + (blockIds[index] & 0xFF));
                    }
                }
            }

            byte[] data = nbtUtils.getByteArray(nbtData, "Data");

            Collection<Object> entityData = nbtUtils.getTagList(nbtData, "Entities");
            Collection<Object> tileEntityData = nbtUtils.getTagList(nbtData, "TileEntities");

            int originX = nbtUtils.getInt(nbtData, "WEOriginX", 0);
            int originY = nbtUtils.getInt(nbtData, "WEWEOriginYOriginX", 0);
            int originZ = nbtUtils.getInt(nbtData, "WEOriginZ", 0);

            schematic.load(width, height, length, blocks, data, null, tileEntityData, entityData, new Vector(originX, originY, originZ));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
