package com.elmakers.mine.bukkit.api.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.elmakers.mine.bukkit.api.magic.Messages;

/**
 * A utility interface for presenting a Material in its entirety, including Material variants.
 * 
 * This will probably need an overhaul for 1.8, but I'm hoping that using this class everywhere as an intermediate for 
 * the concept of "material type" will allow for a relatively easy transition. We'll see.
 * 
 * In the meantime, this class primary uses String-based "keys" to identify a material. This is not
 * necessarily meant to be a friendly or printable name, though the class is capable of generating a semi-friendly
 * name, which will be the key lowercased and with underscores replaced with spaces. It will also attempt to create
 * a nice name for the variant, such as "blue wool". There is no DB for this, it is all based on the internal Bukkit
 * Material and MaterialData enumerations.
 * 
 * Some examples of keys:
 * wool
 * diamond_block
 * monster_egg
 * wool:15 (for black wool)
 * 
 * This class may also handle special "brushes", and is extended in the MagicPlugin as MaterialBrush. In this case
 * there may be other non-material keys such as clone, copy, schematic:lantern, map, etc.
 * 
 * When used as a storage mechanism for Block or Material data, this class will store the following bits of information:
 * 
 * - Base Material type
 * - Data/durability of material (stored as a byte.. TODO: should this be a short? Let's wait for 1.8)
 * - Sign Text
 * - Command Block Text
 * - Custom Name of Block (Skull, Command block name)
 * - InventoryHolder contents
 * 
 * If persisted to a ConfigurationSection, this will currently only store the base Material and data, extra metadata will not 
 * be saved.
 */
public interface MaterialAndData {
    void updateFrom(MaterialAndData other);
    void setMaterial(Material material, short data);
    void setMaterial(Material material);
    void updateFrom(Block block);
    void modify(Block block);
    void modify(Block block, boolean applyPhysics);
    void modify(Block block, ModifyType modifyType);
    Short getData();
    Byte getBlockData();
    void setData(Short data);
    Material getMaterial();
    String getKey();
    String getName();
    String getName(Messages messages);
    String getBaseName();
    boolean is(Block block);
    boolean isDifferent(Block block);
    ItemStack getItemStack(int amount);
    boolean isValid();
    String getCommandLine();
    void setCommandLine(String commandLine);
    void setCustomName(String customName);
    void setRawData(Object data);
    ItemStack applyToItem(ItemStack stack);
    MaterialData getMaterialData();
}
