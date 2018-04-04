package com.elmakers.mine.bukkit.api.block;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Messages;

/**
 * A utility class for presenting a Material in its entirety, including Material variants.
 *
 * <p>This will probably need an overhaul for 1.8, but I'm hoping that using this class everywhere as an intermediate for
 * the concept of "material type" will allow for a relatively easy transition. We'll see.
 *
 * <p>In the meantime, this class primary uses String-based "keys" to identify a material. This is not
 * necessarily meant to be a friendly or printable name, though the class is capable of generating a semi-friendly
 * name, which will be the key lowercased and with underscores replaced with spaces. It will also attempt to create
 * a nice name for the variant, such as "blue wool". There is no DB for this, it is all based on the internal Bukkit
 * Material enumerations.
 *
 * <p>Some examples of keys:
 * wool
 * diamond_block
 * monster_egg
 * wool:15 (for black wool)
 *
 * <p>This class may also handle special "brushes", and is extended in the MagicPlugin as MaterialBrush. In this case
 * there may be other non-material keys such as clone, copy, schematic:lantern, map, etc.
 *
 * <p>When used as a storage mechanism for Block or Material data, this class will store the following bits of information:
 *
 * <li> Base Material type
 * <li> Data/durability of material
 * <li> Sign Text
 * <li> Command Block Text
 * <li> Custom Name of Block (Skull, Command block name)
 * <li> InventoryHolder contents
 *
 * <p>If persisted to a ConfigurationSection, this will currently only store the base Material and data, extra metadata will not
 * be saved.
 */
public interface MaterialAndData {
    void updateFrom(MaterialAndData other);
    void updateFrom(Block block);
    void setMaterial(Material material, short data);
    void setMaterial(Material material);
    void modify(Block block);
    void modify(Block block, boolean applyPhysics);
    void modify(Block block, ModifyType modifyType);
    @Nullable
    Short getData();
    @Nullable
    Byte getBlockData();
    void setData(Short data);
    @Nullable
    Material getMaterial();
    String getKey();
    String getName();
    String getName(Messages messages);
    @Nullable
    String getBaseName();
    boolean is(Block block);
    boolean isDifferent(Block block);
    @Nullable
    ItemStack getItemStack(int amount);
    boolean isValid();
    @Nullable
    String getCommandLine();
    void setCommandLine(String commandLine);
    void setCustomName(String customName);
    void setRawData(Object data);
    ItemStack applyToItem(ItemStack stack);
}
