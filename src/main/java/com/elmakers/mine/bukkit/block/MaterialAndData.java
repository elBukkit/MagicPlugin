package com.elmakers.mine.bukkit.block;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.NMSUtils;
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
    protected Byte data;
    protected String[] signLines = null;
    protected String commandLine = null;
    protected String customName = null;
    protected ItemStack[] inventoryContents = null;
    protected boolean isValid = true;

    public Material DEFAULT_MATERIAL = Material.AIR;

    public MaterialAndData() {
        material = DEFAULT_MATERIAL;
        data = 0;
    }

    public MaterialAndData(final Material material) {
        this.material = material;
        this.data = 0;
    }

    public MaterialAndData(final Material material, final  byte data) {
        this.material = material;
        this.data = data;
    }

    public MaterialAndData(ItemStack item) {
        this.material = item.getType();
        this.data = (byte)item.getDurability();
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
                } else {
                    data = Byte.parseByte(pieces[1]);
                }
            }
        } catch (Exception ex) {
            // Some special-cases
            if (material == Material.SKULL || material == Material.MOB_SPAWNER) {
                customName = pieces[1];
                data = 3;
            } else if (material == Material.SKULL || material == Material.MOB_SPAWNER) {
                customName = pieces[1];
                data = 0;
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
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int hashCode() {
        // Note that this does not incorporate any metadata!
        return (material.getId() << 8) | data;
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
        }
    }

    public void setMaterial(Material material, byte data) {
        this.material = material;
        this.data = data;
        signLines = null;
        commandLine = null;
        inventoryContents = null;
        customName = null;
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
                customName = skull.getOwner();
            } else if (blockState instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner)blockState;
                customName = spawner.getCreatureTypeName();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        material = blockMaterial;
        data = block.getData();
        isValid = true;
    }

    public static void removeMetadata(Block block, String key) {
        Collection<MetadataValue> metadata = block.getMetadata(key);
        for (MetadataValue value : metadata) {
            block.removeMetadata(key, value.getOwningPlugin());
        }
    }

    @SuppressWarnings("deprecation")
    public void modify(Block block) {
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
            }

            if (material != null) {
                block.setType(material);
            }
            if (data != null) {
                block.setData(data);
            }

            BlockState blockState = block.getState();
            if (blockState instanceof Sign && signLines != null) {
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
            } else if (blockState instanceof Skull && customName != null) {
                Skull skull = (Skull)blockState;
                skull.setOwner(customName);
                skull.update();
            } else if (blockState instanceof CreatureSpawner && customName != null && customName.length() > 0) {
                CreatureSpawner spawner = (CreatureSpawner)blockState;
                spawner.setCreatureTypeByName(customName);
                spawner.update();
            }
        } catch (Exception ex) {
            Bukkit.getLogger().warning("Error updating block state: " + ex.getMessage());
        }
    }

    public byte getData() {
        return data;
    }

    public Material getMaterial() {
        return material;
    }

    public String getKey() {
        String materialKey = material == null ? "*" : material.name().toLowerCase();

        if (data == null) {
            materialKey += ":*";
        } else {
            // Some special keys
            if (material == Material.SKULL && data == 3 && customName != null && customName.length() > 0) {
                materialKey += ":" + customName;
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
        return new ItemStack(material, amount, (short)0, data);
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

    public String getName() {
        return getMaterialName(getKey());
    }

    @SuppressWarnings("deprecation")
    public static String getMaterialName(String materialKey) {
        if (materialKey == null) return null;
        String materialName = materialKey;
        String[] namePieces = splitMaterialKey(materialName);
        if (namePieces.length == 0) return null;

        materialName = namePieces[0];

        MaterialAndData materialAndData = new MaterialAndData(materialKey);
        if (!materialAndData.isValid()) return null;

        Material material = materialAndData.getMaterial();
        byte data = materialAndData.getData();
        String customName = materialAndData.getCustomName();

        // This is the "right" way to do this, but relies on Bukkit actually updating Material in a timely fashion :P
        /*
        Class<? extends MaterialData> materialData = material.getData();
        Bukkit.getLogger().info("Material " + material + " has " + materialData);
        if (Wool.class.isAssignableFrom(materialData)) {
            Wool wool = new Wool(material, data);
            materialName += " " + wool.getColor().name();
        } else if (Dye.class.isAssignableFrom(materialData)) {
            Dye dye = new Dye(material, data);
            materialName += " " + dye.getColor().name();
        } else if (Dye.class.isAssignableFrom(materialData)) {
            Dye dye = new Dye(material, data);
            materialName += " " + dye.getColor().name();
        }
        */

        // Using raw id's for 1.6 support, but still trying to give them nice names.
        // TODO: I don't think these colors are right... is DyeColor correct here?

        // if (material == Material.CARPET || material == Material.STAINED_GLASS || material == Material.STAINED_CLAY || material == Material.STAINED_GLASS_PANE || material == Material.WOOL) {
        if (material == Material.CARPET || material.getId() == 95 || material.getId() ==159 || material.getId() == 160 || material == Material.WOOL) {
            // Note that getByDyeData doesn't work for stained glass or clay. Kind of misleading?
            DyeColor color = DyeColor.getByWoolData(data);
            if (color != null) {
                materialName = color.name().toLowerCase().replace('_', ' ') + " " + materialName;
            }
        } else if (material == Material.WOOD || material == Material.LOG || material == Material.SAPLING || material == Material.LEAVES) {
            TreeSpecies treeSpecies = TreeSpecies.getByData(data);
            if (treeSpecies != null) {
                materialName = treeSpecies.name().toLowerCase().replace('_', ' ') + " " + materialName;
            }
        } else if ((material == Material.SKULL || material == Material.MOB_SPAWNER) && customName != null && customName.length() > 0) {
            materialName = materialName + " (" + customName + ")";
        } else {
            materialName = material.name();
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
    public void setData(byte data) {
        this.data = data;
    }
}
