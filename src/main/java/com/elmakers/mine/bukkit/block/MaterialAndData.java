package com.elmakers.mine.bukkit.block;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.TreeSpecies;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.NMSUtils;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.MetadataValue;

/**
 * A utility class for presenting a Material in its entirety, including Material variants.
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
public class MaterialAndData implements com.elmakers.mine.bukkit.api.block.MaterialAndData {
    protected Material material;
    protected Short data;
    protected String[] signLines = null;
    protected String commandLine = null;
    protected String customName = null;
    protected ItemStack[] inventoryContents = null;
    protected boolean isValid = true;
    protected BlockFace rotation = null;
    protected Object customData = null;
    protected SkullType skullType = null;
    protected DyeColor color = null;

    public Material DEFAULT_MATERIAL = Material.AIR;

    public MaterialAndData() {
        material = DEFAULT_MATERIAL;
        data = 0;
    }

    public MaterialAndData(final Material material) {
        this.material = material;
        this.data = 0;
    }

    public MaterialAndData(final Material material, final  short data) {
        this.material = material;
        this.data = data;
    }

    @SuppressWarnings("deprecation")
    public MaterialAndData(ItemStack item) {
        this.material = item.getType();
        this.data = item.getDurability();
        if (this.material == Material.SKULL_ITEM)
        {
            ItemMeta meta = item.getItemMeta();
            this.customData = InventoryUtils.getSkullProfile(meta);
            try {
                this.skullType = SkullType.values()[this.data];
            } catch (Exception ex) {

            }
        } else if (this.material.getId() == 425) {
            // Banner
            // TODO: Change to Material.BANNER when dropping 1.7 support
            ItemMeta meta = item.getItemMeta();
            this.customData = InventoryUtils.getBannerPatterns(meta);
            this.color = InventoryUtils.getBannerBaseColor(meta);
        }
    }

    public MaterialAndData(Block block) {
        updateFrom(block);
    }

    public MaterialAndData(com.elmakers.mine.bukkit.api.block.MaterialAndData other) {
        updateFrom(other);
    }

    public MaterialAndData(final Material material, final  byte data, final String customName) {
        this(material, data);
        this.customName = customName;
    }

    @SuppressWarnings("deprecation")
    public MaterialAndData(String materialKey) {
        this();
        update(materialKey);
    }

    public void update(String materialKey) {
        if (materialKey == null || materialKey.length() == 0) {
            isValid = false;
            return;
        }
        String[] pieces = splitMaterialKey(materialKey);

        try {
            if (pieces.length > 0) {
                if (pieces[0].equals("*")) {
                    material = null;
                } else {
                    // Legacy material id loading
                    try {
                        Integer id = Integer.parseInt(pieces[0]);
                        material = Material.getMaterial(id);
                    } catch (Exception ex) {
                        material = Material.getMaterial(pieces[0].toUpperCase());
                    }
                }
            }
        } catch (Exception ex) {
            material = null;
        }
        try {
            if (pieces.length > 1) {
                if (pieces[1].equals("*")) {
                    data = null;
                } else if (material == Material.SKULL_ITEM) {
                    if (pieces.length > 2) {
                        data = 3;
                        skullType = SkullType.PLAYER;
                        String dataString = pieces[1];
                        for (int i = 2; i < pieces.length; i++) {
                            dataString += ":" + pieces[i];
                        }
                        ItemStack item = InventoryUtils.getURLSkull(dataString);
                        customData = InventoryUtils.getSkullProfile(item.getItemMeta());
                    } else {
                        try {
                            data = Short.parseShort(pieces[1]);
                        } catch (Exception ex) {
                            data = 3;
                            skullType = SkullType.PLAYER;
                            ItemStack item = InventoryUtils.getPlayerSkull(pieces[1]);
                            customData = InventoryUtils.getSkullProfile(item.getItemMeta());
                        }
                    }
                } else {
                    try {
                        data = Short.parseShort(pieces[1]);
                    } catch (Exception ex) {
                        data = 0;
                    }
                }
            }
        } catch (Exception ex) {
            // Some special-cases
            if (material == Material.MOB_SPAWNER) {
                customName = pieces[1];
            } else {
                data = 0;
                customName = "";
            }
        }

        if (material == null) {
            isValid = false;

            // TODO: null these out?
            material = DEFAULT_MATERIAL;
            data = 0;
            customName = "";
        } else {
            setMaterial(material, data);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int hashCode() {
        // Note that this does not incorporate any metadata!
        return (material.getId() << 16) | data;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MaterialAndData)) {
            return false;
        }

        MaterialAndData other = (MaterialAndData)obj;
        return other.data == data && other.material == material;
    }

    public void updateFrom(com.elmakers.mine.bukkit.api.block.MaterialAndData other) {
        material = other.getMaterial();
        data = other.getData();
        if (other instanceof MaterialAndData) {
            MaterialAndData o = (MaterialAndData)other;
            commandLine = o.commandLine;
            inventoryContents = o.inventoryContents;
            signLines = o.signLines;
            customName = o.customName;
            isValid = o.isValid;
            skullType = o.skullType;
            customData = o.customData;
            color = o.color;
        }
    }

    public void setMaterial(Material material, short data) {
        setMaterial(material, (Short)data);
    }

    public void setMaterial(Material material, Short data) {
        this.material = material;
        this.data = data;
        signLines = null;
        commandLine = null;
        inventoryContents = null;
        customName = null;
        skullType = null;
        customData = null;
        color = null;

        isValid = true;
    }

    public void setMaterial(Material material) {
        setMaterial(material, (byte)0);
    }

    public void updateFrom(Block block) {
        updateFrom(block, null);
    }

    @SuppressWarnings("deprecation")
    public void updateFrom(Block block, Set<Material> restrictedMaterials) {
        if (block == null) {
            isValid = false;
            return;
        }
        if (!block.getChunk().isLoaded()) {
            block.getChunk().load(true);
            return;
        }

        Material blockMaterial = block.getType();
        if (restrictedMaterials != null && restrictedMaterials.contains(blockMaterial)) {
            isValid = false;
            return;
        }
        // Look for special block states
        signLines = null;
        commandLine = null;
        inventoryContents = null;
        customName = null;
        skullType = null;
        customData = null;
        color = null;

        try {
            BlockState blockState = block.getState();
            if (blockState instanceof Sign) {
                Sign sign = (Sign)blockState;
                signLines = sign.getLines();
            } else if (blockState instanceof CommandBlock){
                // This seems to occasionally throw exceptions...
                CommandBlock command = (CommandBlock)blockState;
                commandLine = command.getCommand();
                customName = command.getName();
            } else if (blockState instanceof InventoryHolder) {
                InventoryHolder holder = (InventoryHolder)blockState;
                Inventory holderInventory = holder.getInventory();
                inventoryContents = holderInventory.getContents();
            } else if (blockState instanceof Skull) {
                Skull skull = (Skull)blockState;
                rotation = skull.getRotation();
                skullType = skull.getSkullType();
                customData = CompatibilityUtils.getSkullProfile(skull);
            } else if (blockState instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner)blockState;
                customName = spawner.getCreatureTypeName();
            } else if (blockMaterial.getId() == 176 || blockMaterial.getId() == 177) {
                // Banner
                // TODO: Change to Material.BANNER when dropping 1.7 support
                customData = CompatibilityUtils.getBannerPatterns(blockState);
                color = CompatibilityUtils.getBannerBaseColor(blockState);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        material = blockMaterial;
        data = (short)block.getData();
        isValid = true;
    }

    public static void removeMetadata(Block block, String key) {
        Collection<MetadataValue> metadata = block.getMetadata(key);
        for (MetadataValue value : metadata) {
            block.removeMetadata(key, value.getOwningPlugin());
        }
    }

    public void modify(Block block) {
        modify(block, false);
    }

    @SuppressWarnings("deprecation")
    public void modify(Block block, boolean applyPhysics) {
        if (!isValid) return;

        try {
            removeMetadata(block, "breakable");
            removeMetadata(block, "backfire");

            // Clear chests so they don't dump their contents.
            BlockState oldState = block.getState();
            if (oldState instanceof InventoryHolder) {
                InventoryHolder holder = (InventoryHolder)oldState;
                Inventory inventory = holder.getInventory();
                inventory.clear();
                oldState.update();
            }

            if (material != null) {
                byte blockData = data != null ? (byte)(short)data : block.getData();
                block.setTypeIdAndData(material.getId(), blockData, applyPhysics);
            }

            BlockState blockState = block.getState();
            if (blockState != null && material != null && material.getId() == 176 || material.getId() == 177) {
                // Banner
                // TODO: Change to Material.BANNER when dropping 1.7 support
                CompatibilityUtils.setBannerPatterns(blockState, customData);
                CompatibilityUtils.setBannerBaseColor(blockState, color);
                blockState.update(true, false);
            } else if (blockState instanceof Sign && signLines != null) {
                Sign sign = (Sign)blockState;
                for (int i = 0; i < signLines.length; i++) {
                    sign.setLine(i, signLines[i]);
                }
                sign.update();
            } else if (blockState instanceof CommandBlock && commandLine != null) {
                CommandBlock command = (CommandBlock)blockState;
                command.setCommand(commandLine);
                if (customName != null) {
                    command.setName(customName);
                }
                command.update();
            } else if (blockState instanceof InventoryHolder && inventoryContents != null) {
                InventoryHolder holder = (InventoryHolder)blockState;
                Inventory newInventory = holder.getInventory();
                int maxSize = Math.min(newInventory.getSize(), inventoryContents.length);
                for (int i = 0; i < maxSize; i++) {
                    ItemStack item = inventoryContents[i];
                    item = NMSUtils.getCopy(item);
                    if (item != null) {
                        newInventory.setItem(i, item);
                    }
                }
            } else if (blockState instanceof Skull) {
                Skull skull = (Skull)blockState;
                if (skullType != null) {
                    skull.setSkullType(skullType);
                }
                if (rotation != null) {
                    skull.setRotation(rotation);
                }
                if (customData != null) {
                    CompatibilityUtils.setSkullProfile(skull, customData);
                }
                skull.update(true, false);
            } else if (blockState instanceof CreatureSpawner && customName != null && customName.length() > 0) {
                CreatureSpawner spawner = (CreatureSpawner)blockState;
                spawner.setCreatureTypeByName(customName);
                spawner.update();
            }
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Error updating block state: " + ex.getMessage());
        }
    }

    @Override
    public Short getData() {
        return data;
    }

    @Override
    public Byte getBlockData() {
        return data == null ? null : (byte)(short)data;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    public String getKey() {
        return getKey(data);
    }

    public String getKey(Short data) {
        String materialKey = material == null ? "*" : material.name().toLowerCase();
        if (data == null) {
            materialKey += ":*";
        } else {
            // Some special keys
            if (material == Material.SKULL_ITEM && customData != null) {
                materialKey += ":" + InventoryUtils.getProfileURL(customData);
            }
            else if (material == Material.MOB_SPAWNER && customName != null && customName.length() > 0) {
                materialKey += ":" + customName;
            }
            else if (data != 0) {
                materialKey += ":" + data;
            }
        }

        return materialKey;
    }

    public String getWildDataKey() {
        return getKey(null);
    }

    // TODO: Should this just be !isDifferent .. ? It's fast right now.
    @SuppressWarnings("deprecation")
    public boolean is(Block block) {
        return material == block.getType() && data == block.getData();
    }

    @SuppressWarnings("deprecation")
    public boolean isDifferent(Block block) {
        Material blockMaterial = block.getType();
        byte blockData = block.getData();
        if ((material != null && blockMaterial != material) || (data != null && blockData != data)) {
            return true;
        }

        // Special cases
        if (material.getId() == 176 || material.getId() == 177) {
            // Can't compare patterns for now
            return true;
        }

        BlockState blockState = block.getState();
        if (blockState instanceof Sign && signLines != null) {
            Sign sign = (Sign)blockState;
            String[] currentLines = sign.getLines();
            for (int i = 0; i < signLines.length; i++) {
                if (!currentLines[i].equals(signLines[i])) {
                    return true;
                }
            }
        } else if (blockState instanceof CommandBlock && commandLine != null) {
            CommandBlock command = (CommandBlock)blockState;
            if (!command.getCommand().equals(commandLine)) {
                return true;
            }
        } else if (blockState instanceof InventoryHolder && inventoryContents != null) {
            // Just copy it over.... not going to compare inventories :P
            return true;
        }

        return false;
    }

    public void setSignLines(String[] lines) {
        signLines = lines.clone();
    }

    public String[] getSignLines() {
        return signLines.clone();
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomName() {
        return customName;
    }

    public void setInventoryContents(ItemStack[] contents) {
        inventoryContents = contents;
    }

    @SuppressWarnings("deprecation")
    public ItemStack getItemStack(int amount)
    {
        ItemStack stack = new ItemStack(material, amount, data);
        applyToItem(stack);
        return stack;
    }

    public ItemStack applyToItem(ItemStack stack)
    {
        stack.setType(material);
        stack.setDurability(data);
        if (material == Material.SKULL_ITEM)
        {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof SkullMeta && customData != null)
            {
                SkullMeta skullMeta = (SkullMeta)meta;
                InventoryUtils.setSkullProfile(skullMeta, customData);
                stack.setItemMeta(meta);
            }
        } else if (this.material.getId() == 425) {
            // Banner
            // TODO: Change to Material.BANNER when dropping 1.7 support
            ItemMeta meta = stack.getItemMeta();
            InventoryUtils.setBannerPatterns(meta, this.customData);
            InventoryUtils.setBannerBaseColor(meta, this.color);
        }
        return stack;
    }

    public static String[] splitMaterialKey(String materialKey) {
        if (materialKey.contains("|")) {
            return StringUtils.split(materialKey, "|");
        } else if (materialKey.contains(":")) {
            return StringUtils.split(materialKey, ":");
        }

        return new String[] { materialKey };
    }

    public boolean isValid()
    {
        return isValid;
    }

    public static String getMaterialName(ItemStack item) {
        MaterialAndData material = new MaterialAndData(item.getType(), item.getDurability());
        return material.getName();
    }

    @SuppressWarnings("deprecation")
    public static String getMaterialName(Block block) {
        MaterialAndData material = new MaterialAndData(block.getType(), block.getData());
        return material.getName();
    }

    public String getName() {
        return getName(null);
    }


    @SuppressWarnings("deprecation")
    public String getBaseName() {
        return material.name().toLowerCase().replace('_', ' ');
    }

        @SuppressWarnings("deprecation")
    public String getName(Messages messages) {
        if (!isValid()) return null;

        Material material = getMaterial();
        Short data = getData();
        String customName = getCustomName();
        String materialName = material.name();

        // This is the "right" way to do this, but relies on Bukkit actually updating Material in a timely fashion :P
        /*
        MaterialData materialData = material.getNewData((byte)(short)data);
        if (materialData instanceof Colorable) {
            materialName += " " + ((Colorable)materialData).getColor().name();
        }
        if (materialData instanceof Tree) {
            Tree tree = (Tree)materialData;
            materialName += " " + tree.getSpecies().name() + " " + tree.getDirection().name();
        }
        if (materialData instanceof Stairs) {
            Stairs stairs = (Stairs)materialData;
            materialName += " " + stairs.getFacing().name();
            // TODO: Ascending/descending directions?
        }
        if (materialData instanceof WoodenStep) {
            WoodenStep step = (WoodenStep)materialData;
            materialName += " " + step.getSpecies().name();
        }
        */

        if (data != null) {
             if (material == Material.CARPET || material == Material.STAINED_GLASS || material == Material.STAINED_CLAY || material == Material.STAINED_GLASS_PANE || material == Material.WOOL) {
                // Note that getByDyeData doesn't work for stained glass or clay. Kind of misleading?
                DyeColor color = DyeColor.getByWoolData((byte)(short)data);
                if (color != null) {
                    materialName = color.name().toLowerCase().replace('_', ' ') + " " + materialName;
                }
            } else if (material == Material.WOOD || material == Material.LOG || material == Material.SAPLING || material == Material.LEAVES
                     || material == Material.LOG_2 || material == Material.LEAVES_2) {
                TreeSpecies treeSpecies = TreeSpecies.getByData((byte)(short)data);
                if (treeSpecies != null) {
                    materialName = treeSpecies.name().toLowerCase().replace('_', ' ') + " " + materialName;
                }
            } else if (material == Material.MOB_SPAWNER && customName != null && customName.length() > 0) {
                materialName = materialName + " (" + customName + ")";
            }
        }

        materialName = materialName.toLowerCase().replace('_', ' ');
        return materialName;
    }

    @Override
    public void setCommandLine(String command) {
        commandLine = command;
    }

    @Override
    public String getCommandLine() {
        return commandLine;
    }

    @Override
    public void setData(Short data) {
        this.data = data;
    }
}
