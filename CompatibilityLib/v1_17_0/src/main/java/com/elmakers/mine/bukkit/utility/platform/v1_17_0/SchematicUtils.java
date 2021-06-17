package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.util.Vector;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.SchematicUtilsBase;
import com.elmakers.mine.bukkit.utility.schematic.LoadableSchematic;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

public class SchematicUtils extends SchematicUtilsBase {
    public SchematicUtils(Platform platform) {
        super(platform);
    }

    @Override
    public boolean loadSchematic(InputStream input, LoadableSchematic schematic, Logger log) {
        if (input == null || schematic == null) return false;

        try {
            CompoundTag nbtData = NbtIo.readCompressed(input);
            if (nbtData == null) {
                return false;
            }

            short width = nbtData.getShort("Width");
            short height = nbtData.getShort("Height");
            short length = nbtData.getShort("Length");

            CompoundTag palette = nbtData.getCompound("Palette");
            byte[] blockData = nbtData.getByteArray("BlockData");
            int[] blockMap = null;
            Map<Integer, String> paletteMap = null;

            if (palette != null) {
                // Map the palette
                paletteMap = new HashMap<>();
                Set<String> keys = palette.getAllKeys();
                for (String key : keys) {
                    int index = palette.getInt(key);
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
            Collection<Object> tileEntityData = new ArrayList<>();
            Collection<Object> entityData = new ArrayList<>();

            ListTag entityList = nbtData.getList("Entities", CompatibilityConstants.NBT_TYPE_COMPOUND);
            ListTag tileEntityList = null;
            if (nbtData.contains("BlockEntities")) {
                tileEntityList = nbtData.getList("BlockEntities", CompatibilityConstants.NBT_TYPE_COMPOUND);
            } else {
                tileEntityList = nbtData.getList("TileEntities", CompatibilityConstants.NBT_TYPE_COMPOUND);
            }

            if (entityList != null) {
                int size = entityList.size();
                for (int i = 0; i < size; i++) {
                    Object entity = entityList.get(i);
                    entityData.add(entity);
                }
            }

            if (tileEntityList != null) {
                int size = tileEntityList.size();
                for (int i = 0; i < size; i++) {
                    Object tileEntity = tileEntityList.get(i);
                    tileEntityData.add(tileEntity);
                }
            }

            Vector origin = new Vector(0, 0, 0);
            int[] offset = nbtData.getIntArray("Offset");
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
    public boolean saveSchematic(OutputStream output, String[][][] blockData) {
        if (output == null || blockData == null || blockData.length == 0 || blockData[0].length == 0 || blockData[0][0].length == 0) {
            return false;
        }

        CompoundTag nbtData = new CompoundTag();
        int width = blockData.length;
        int height = blockData[0].length;
        int length = blockData[0][0].length;
        nbtData.putShort("Width", (short)width);
        nbtData.putShort("Height", (short)height);
        nbtData.putShort("Length", (short)length);

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
        CompoundTag palette = new CompoundTag();
        for (Map.Entry<String, Integer> entry : paletteLookup.entrySet()) {
            palette.putInt(entry.getKey(), entry.getValue());
        }
        nbtData.put("Palette", palette);

        // Save block list to NBT
        byte[] blockArray = Bytes.toArray(blockList);
        nbtData.put("BlockData", new ByteArrayTag(blockArray));

        // Add empty lists so they exist
        nbtData.put("Entities", new ListTag());
        nbtData.put("BlockEntities", new ListTag());
        int[] offset = {0, 0, 0};
        nbtData.put("Offset", new IntArrayTag(offset));

        try {
            NbtIo.writeCompressed(nbtData, output);
        } catch (IOException ex) {
            platform.getLogger().log(Level.WARNING, "Error writing schematic", ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean loadLegacySchematic(InputStream input, LoadableSchematic schematic) {
        if (input == null || schematic == null) return false;

        try {
            CompoundTag nbtData = NbtIo.readCompressed(input);
            if (nbtData == null) {
                return false;
            }

            // Version check
            String materials = nbtData.getString("Materials");
            if (!materials.equals("Alpha")) {
                Bukkit.getLogger().warning("Schematic is not in Alpha format");
                return false;
            }

            short width = nbtData.getShort("Width");
            short height = nbtData.getShort("Height");
            short length = nbtData.getShort("Length");

            byte[] blockIds = nbtData.getByteArray("Blocks");

            // Have to combine block ids to get 12 bits of ids
            // Thanks to the WorldEdit team for showing me how to do this.
            int[] blocks = new int[blockIds.length];
            byte[] addBlocks = new byte[0];
            if (nbtData.contains("AddBlocks")) {
                addBlocks = nbtData.getByteArray("AddBlocks");
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

            byte[] data = nbtData.getByteArray("Data");

            Collection<Object> tileEntityData = new ArrayList<>();
            Collection<Object> entityData = new ArrayList<>();

            ListTag entityList = nbtData.getList("Entities", CompatibilityConstants.NBT_TYPE_COMPOUND);
            ListTag tileEntityList = nbtData.getList("TileEntities", CompatibilityConstants.NBT_TYPE_COMPOUND);

            if (entityList != null) {
                int size = entityList.size();
                for (int i = 0; i < size; i++) {
                    Object entity = entityList.get(i);
                    entityData.add(entity);
                }
            }

            if (tileEntityList != null) {
                int size = tileEntityList.size();
                for (int i = 0; i < size; i++) {
                    Object tileEntity = tileEntityList.get(i);
                    tileEntityData.add(tileEntity);
                }
            }

            int originX = nbtData.getInt("WEOriginX");
            int originY = nbtData.getInt("WEOriginY");
            int originZ = nbtData.getInt("WEOriginZ");

            schematic.load(width, height, length, blocks, data, null, tileEntityData, entityData, new Vector(originX, originY, originZ));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
