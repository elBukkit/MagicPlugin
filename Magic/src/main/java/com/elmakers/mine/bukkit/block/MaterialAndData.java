package com.elmakers.mine.bukkit.block;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;

import com.elmakers.mine.bukkit.api.block.ModifyType;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.SkinUtils;
import com.google.common.base.Objects;

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
 * Material and MaterialData enumerations.
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
 * <li>Base Material type
 * <li>Data/durability of material
 * <li>Sign Text
 * <li>Command Block Text
 * <li>Custom Name of Block (Skull, Command block name)
 * <li>InventoryHolder contents
 *
 * <p>If persisted to a ConfigurationSection, this will currently only store the base Material and data, extra metadata will not
 * be saved.
 */
public class MaterialAndData implements com.elmakers.mine.bukkit.api.block.MaterialAndData {
    protected Material material;
    protected Short data;
    protected BlockExtraData extraData;
    protected boolean isValid = true;

    public static final Material DEFAULT_MATERIAL = Material.AIR;

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

    public MaterialAndData(ItemStack item) {
        this.material = item.getType();
        this.data = item.getDurability();
        if (this.material == Material.SKULL_ITEM)
        {
            ItemMeta meta = item.getItemMeta();
            Object profile = InventoryUtils.getSkullProfile(meta);
            SkullType skullType = SkullType.PLAYER;
            try {
                skullType = SkullType.values()[this.data];
            } catch (Exception ignored) {

            }
            extraData = new BlockSkull(profile, skullType);
        } else if (this.material == Material.STANDING_BANNER || this.material == Material.WALL_BANNER || this.material == Material.BANNER) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof BannerMeta)
            {
                BannerMeta banner = (BannerMeta)meta;
                extraData = new BlockBanner(banner.getPatterns(), banner.getBaseColor());
            }
        } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof LeatherArmorMeta) {
                extraData = new LeatherArmorData(((LeatherArmorMeta)meta).getColor());
            }
        }
    }

    public MaterialAndData(Block block) {
        updateFrom(block);
    }

    public MaterialAndData(com.elmakers.mine.bukkit.api.block.MaterialAndData other) {
        updateFrom(other);
    }

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
        Short data = 0;
        Material material = null;
        BlockExtraData extraData = null;

        try {
            if (pieces.length > 0) {
                if (!pieces[0].equals("*")) {
                    // Legacy material id loading
                    try {
                        Integer id = Integer.parseInt(pieces[0]);
                        material = DeprecatedUtils.getMaterial(id);
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
                // Some special-cases
                if (pieces[1].equals("*")) {
                    data = null;
                }
                else if (material == Material.MOB_SPAWNER) {
                    extraData = new BlockMobSpawner(pieces[1]);
                }
                else if (material == Material.SKULL_ITEM) {
                    if (pieces.length > 2) {
                        data = 3;
                        String dataString = pieces[1];
                        for (int i = 2; i < pieces.length; i++) {
                            dataString += ":" + pieces[i];
                        }
                        ItemStack item = InventoryUtils.getURLSkull(dataString);
                        extraData = new BlockSkull(InventoryUtils.getSkullProfile(item.getItemMeta()), SkullType.PLAYER);
                    } else {
                        try {
                            data = Short.parseShort(pieces[1]);
                        } catch (Exception ex) {
                            data = 3;
                            extraData = new BlockSkull(pieces[1]);
                        }
                    }
                }
                else if (material == Material.STANDING_BANNER || material == Material.WALL_BANNER || material == Material.BANNER) {
                    DyeColor color = null;
                    try {
                        short colorIndex = Short.parseShort(pieces[1]);
                        data = colorIndex;
                        color = DyeColor.values()[colorIndex];
                    }
                    catch (Exception ex) {
                        color = null;
                    }
                    if (color != null) {
                        extraData = new BlockBanner(color);
                    }
                }  else if (material == Material.LEATHER_BOOTS || material == Material.LEATHER_CHESTPLATE
                        || material == Material.LEATHER_HELMET || material == Material.LEATHER_LEGGINGS) {
                    StringUtils.split(pieces[1], ',');
                    for (String piece : pieces) {
                        if (piece.startsWith("#")) {
                            try {
                                Color color = Color.fromRGB(Integer.parseInt(piece.substring(1), 16));
                                extraData = new LeatherArmorData(color);
                            } catch (Exception ex) {
                                extraData = null;
                            }
                        } else {
                            try {
                                data = Short.parseShort(pieces[1]);
                            } catch (Exception ex) {
                                data = 0;
                            }
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
            material = null;
        }

        if (material == null) {
            this.setMaterial(null, null);
            isValid = false;
        } else {
            setMaterial(material, data);
        }
        if (isValid) {
            this.extraData = extraData;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int hashCode() {
        // Note that this does not incorporate any metadata!
        return (material == null ? -1 : (material.getId() << 16)) | (data == null ? -1 : data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof com.elmakers.mine.bukkit.api.block.MaterialAndData)) {
            return false;
        }

        com.elmakers.mine.bukkit.api.block.MaterialAndData other = (com.elmakers.mine.bukkit.api.block.MaterialAndData)obj;
        return Objects.equal(other.getData(), data) && other.getMaterial() == material;
    }

    @Override
    public void updateFrom(com.elmakers.mine.bukkit.api.block.MaterialAndData other) {
        material = other.getMaterial();
        data = other.getData();
        if (other instanceof MaterialAndData) {
            MaterialAndData o = (MaterialAndData)other;
            if (o.extraData != null) {
                extraData = o.extraData.clone();
            } else {
                extraData = null;
            }
            isValid = o.isValid;
        }
    }

    @Override
    public void updateFrom(Block block) {
        updateFromBlock(block, null);
    }

    @Override
    public void setMaterial(Material material, short data) {
        setMaterial(material, (Short)data);
    }

    public void setMaterial(Material material, Short data) {
        this.material = material;
        this.data = data;
        extraData = null;

        isValid = true;
    }

    @Override
    public void setMaterial(Material material) {
        setMaterial(material, (byte)0);
    }

    @SuppressWarnings("deprecation")
    public void setMaterialId(int id) {
        this.material = Material.getMaterial(id);
    }

    @SuppressWarnings("deprecation")
    public void updateFromBlock(Block block, @Nullable MaterialSet restrictedMaterials) {
        if (block == null) {
            isValid = false;
            return;
        }
        if (!block.getChunk().isLoaded()) {
            block.getChunk().load(true);
            return;
        }

        if (restrictedMaterials != null && restrictedMaterials.testBlock(block)) {
            isValid = false;
            return;
        }
        // Look for special block states
        extraData = null;

        Material blockMaterial = block.getType();
        material = blockMaterial;
        data = (short)block.getData();

        try {
            BlockState blockState = block.getState();
            if (material == Material.FLOWER_POT || blockState instanceof InventoryHolder || blockState instanceof Sign) {
                extraData = new BlockTileEntity(NMSUtils.getTileEntityData(block.getLocation()));
            } else if (blockState instanceof CommandBlock) {
                // This seems to occasionally throw exceptions...
                CommandBlock command = (CommandBlock)blockState;
                extraData = new BlockCommand(command.getCommand(), command.getName());
            } else if (blockState instanceof Skull) {
                Skull skull = (Skull)blockState;
                data = (short)skull.getSkullType().ordinal();
                extraData = new BlockSkull(InventoryUtils.getSkullProfile(skull), skull.getSkullType(), skull.getRotation());
            } else if (blockState instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner)blockState;
                extraData = new BlockMobSpawner(spawner.getCreatureTypeName());
            } else if (blockMaterial == Material.STANDING_BANNER || blockMaterial == Material.WALL_BANNER) {
                if (blockState != null && blockState instanceof Banner) {
                    Banner banner = (Banner)blockState;
                    DyeColor color = banner.getBaseColor();
                    extraData = new BlockBanner(banner.getPatterns(), color);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        isValid = true;
    }

    public static void clearItems(BlockState block) {
        if (block != null && (block instanceof InventoryHolder || block.getType() == Material.FLOWER_POT)) {
            NMSUtils.clearItems(block.getLocation());
        }
    }

    @SuppressWarnings("deprecation")
    public void modifyFast(Block block) {
        Material material = this.material == null ? block.getType() : this.material;
        int data = this.data == null ? block.getData() : this.data;
        if (material != block.getType() || data != block.getData()) {
            CompatibilityUtils.setBlockFast(block, material, data);
        }
    }

    @Override
    public void modify(Block block) {
        modify(block, false);
    }

    @Override
    public void modify(Block block, ModifyType modifyType) {
        switch (modifyType) {
            case FAST:
                modifyFast(block);
                break;
            case NORMAL:
                modify(block, true);
                break;
            case NO_PHYSICS:
                modify(block, false);
                break;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void modify(Block block, boolean applyPhysics) {
        if (!isValid) return;

        try {
            BlockState blockState = block.getState();
            if (material != null) {
                byte blockData = data != null ? (byte)(short)data : block.getData();

                if (material == Material.AIR) {
                    // Clear chests and flower pots so they don't dump their contents.
                    clearItems(blockState);
                }

                block.setTypeIdAndData(material.getId(), blockData, applyPhysics);
                blockState = block.getState();
            }

            // Set tile entity data first
            // Command blocks still prefer internal data for parameterized commands
            if (blockState != null && blockState instanceof CommandBlock && extraData != null && extraData instanceof BlockCommand) {
                CommandBlock command = (CommandBlock)blockState;
                BlockCommand commandData = (BlockCommand)extraData;
                command.setCommand(commandData.command);
                if (commandData.customName != null) {
                    command.setName(commandData.customName);
                }
                command.update();
            } else if (extraData != null && extraData instanceof BlockTileEntity) {
                // Tile entity data overrides everything else, and may replace all of this in the future.
                NMSUtils.setTileEntityData(block.getLocation(), ((BlockTileEntity) extraData).data);
            } else if (blockState != null && (material == Material.STANDING_BANNER || material == Material.WALL_BANNER) && extraData != null && extraData instanceof BlockBanner) {
                if (blockState instanceof Banner) {
                    BlockBanner bannerData = (BlockBanner)extraData;
                    Banner banner = (Banner)blockState;
                    if (bannerData.patterns != null)
                    {
                        banner.setPatterns(bannerData.patterns);
                    }
                    if (bannerData.baseColor != null)
                    {
                        banner.setBaseColor(bannerData.baseColor);
                    }
                }
                blockState.update(true, false);
            } else if (blockState != null && blockState instanceof Skull && extraData != null && extraData instanceof BlockSkull) {
                Skull skull = (Skull)blockState;
                BlockSkull skullData = (BlockSkull)extraData;
                if (skullData.skullType != null) {
                    skull.setSkullType(skullData.skullType);
                }
                if (skullData.rotation != null) {
                    skull.setRotation(skullData.rotation);
                }
                if (skullData.profile != null) {
                    InventoryUtils.setSkullProfile(skull, skullData.profile);
                } else if (skullData.playerName != null) {
                    skull.setOwner(skullData.playerName);
                }
                skull.update(true, false);
            } else if (blockState != null && blockState instanceof CreatureSpawner && extraData != null && extraData instanceof BlockMobSpawner) {
                BlockMobSpawner spawnerData = (BlockMobSpawner)extraData;
                if (spawnerData.mobName != null && !spawnerData.mobName.isEmpty())
                {
                    CreatureSpawner spawner = (CreatureSpawner)blockState;
                    spawner.setCreatureTypeByName(spawnerData.mobName);
                    spawner.update();
                }
            } else if (blockState != null && blockState instanceof Sign && extraData != null && extraData instanceof BlockSign) {
                BlockSign signData = (BlockSign)extraData;
                Sign sign = (Sign)blockState;
                for (int i = 0; i < signData.lines.length; i++) {
                    sign.setLine(i, signData.lines[i]);
                }
                sign.update();
            }
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[Magic] Error updating block state");
            ex.printStackTrace();
        }
    }

    @Override
    public Short getData() {
        return data;
    }

    @Nullable
    @Override
    public Byte getBlockData() {
        return data == null ? null : (byte)(short)data;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public String getKey() {
        return getKey(data);
    }

    public String getKey(Short data) {
        String materialKey = material == null ? "*" : material.name().toLowerCase();
        if (data == null) {
            materialKey += ":*";
        } else {
            // Some special keys
            if (material == Material.SKULL_ITEM && extraData != null && extraData instanceof BlockSkull) {
                BlockSkull skullData = (BlockSkull)extraData;
                if (skullData.playerName != null) {
                    materialKey += ":" + skullData.playerName;
                } else {
                    materialKey += ":" + SkinUtils.getProfileURL(skullData.profile);
                }
            }
            else if (material == Material.MOB_SPAWNER && extraData != null && extraData instanceof BlockMobSpawner) {
                BlockMobSpawner spawnerData = (BlockMobSpawner)extraData;
                if (spawnerData.mobName != null && !spawnerData.mobName.isEmpty()) {
                    materialKey += ":" + spawnerData.mobName;
                }
            }
            else if ((material == Material.STANDING_BANNER || material == Material.WALL_BANNER || material == Material.BANNER) && extraData != null && extraData instanceof BlockBanner && ((BlockBanner)extraData).baseColor != null) {
                materialKey += ":" + ((BlockBanner)extraData).baseColor.ordinal();
            } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                    || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS) {
                if (data != 0)
                    materialKey += ":" + data;
                if (extraData != null && extraData instanceof LeatherArmorData) {
                    Color color = ((LeatherArmorData)extraData).getColor();
                    if (data != 0) {
                        materialKey += ",#" + Integer.toHexString(color.asRGB());
                    } else {
                        materialKey += ":#" + Integer.toHexString(color.asRGB());
                    }
                }
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
    @Override
    @SuppressWarnings("deprecation")
    public boolean is(Block block) {
        return material == block.getType() && data == block.getData();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isDifferent(Block block) {
        Material blockMaterial = block.getType();
        byte blockData = block.getData();
        if ((material != null && blockMaterial != material) || (data != null && blockData != data)) {
            return true;
        }

        // Special cases
        if (material == Material.WALL_BANNER || material == Material.STANDING_BANNER) {
            // Can't compare patterns for now
            return true;
        }

        BlockState blockState = block.getState();
        if (blockState instanceof Sign) {
            // Not digging into sign text
            return true;
        } else if (blockState instanceof CommandBlock && extraData != null && extraData instanceof BlockCommand) {
            CommandBlock command = (CommandBlock)blockState;
            if (!command.getCommand().equals(((BlockCommand)extraData).command)) {
                return true;
            }
        } else if (blockState instanceof InventoryHolder) {
            // Just copy it over.... not going to compare inventories :P
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public ItemStack getItemStack(int amount) {
        if (material == null) return null;

        convertToItemStackMaterial();
        ItemStack stack = new ItemStack(material, amount, data == null ? 0 : data);
        applyToItem(stack);
        return stack;
    }

    // TODO: Need to config-drive this?
    // TODO: Should this mutate the material?
    private Material convertToItemStackMaterial() {
        switch (material) {
            case SKULL: return material = Material.SKULL_ITEM;
            case WALL_BANNER: return material = Material.BANNER;
            case STANDING_BANNER: return material = Material.BANNER;
            case WOODEN_DOOR: return material = Material.WOOD_DOOR;
            case IRON_DOOR_BLOCK: return material = Material.IRON_DOOR;
            case WALL_SIGN: return material = Material.SIGN;
            case SIGN_POST: return material = Material.SIGN;
            case ACACIA_DOOR: return material = Material.ACACIA_DOOR_ITEM;
            case JUNGLE_DOOR: return material = Material.JUNGLE_DOOR_ITEM;
            case DARK_OAK_DOOR: return material = Material.DARK_OAK_DOOR_ITEM;
            case BIRCH_DOOR: return material = Material.BIRCH_DOOR_ITEM;
            case SPRUCE_DOOR: return material = Material.SPRUCE_DOOR_ITEM;
            default: return material;
        }
    }

    @Override
    public ItemStack applyToItem(ItemStack stack)
    {
        stack.setType(material);
        if (data != null) {
            stack.setDurability(data);
        }
        if (material == Material.SKULL_ITEM)
        {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof SkullMeta && extraData != null && extraData instanceof BlockSkull)
            {
                BlockSkull skullData = (BlockSkull)extraData;
                if (skullData.skullType == SkullType.PLAYER && skullData.profile != null) {
                    SkullMeta skullMeta = (SkullMeta)meta;
                    InventoryUtils.setSkullProfile(skullMeta, ((BlockSkull)extraData).profile);
                    stack.setItemMeta(meta);
                } else if (skullData.skullType == SkullType.PLAYER && skullData.playerName != null) {
                    SkullMeta skullMeta = (SkullMeta)meta;
                    skullMeta.setOwner(skullData.playerName);
                    stack.setItemMeta(meta);
                }
            }
        } else if (material == Material.STANDING_BANNER || material == Material.WALL_BANNER || material == Material.BANNER) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof BannerMeta && extraData != null && extraData instanceof BlockBanner)
            {
                BannerMeta banner = (BannerMeta)meta;
                BlockBanner bannerData = (BlockBanner)extraData;
                if (bannerData.patterns != null)
                {
                    banner.setPatterns(bannerData.patterns);
                }
                if (bannerData.baseColor != null)
                {
                    banner.setBaseColor(bannerData.baseColor);
                }
                stack.setItemMeta(meta);
            }
        } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS) {
            ItemMeta meta = stack.getItemMeta();
            if (extraData != null && extraData instanceof LeatherArmorData && meta != null && meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta)meta).setColor(((LeatherArmorData)extraData).getColor());
                stack.setItemMeta(meta);
            }
        }
        return stack;
    }

    public static String[] splitMaterialKey(String materialKey) {
        if (materialKey.contains("|")) {
            return StringUtils.split(materialKey, "|", 3);
        } else if (materialKey.contains(":")) {
            return StringUtils.split(materialKey, ":", 3);
        }

        return new String[] { materialKey };
    }

    @Override
    public boolean isValid()
    {
        return isValid;
    }

    public static String getMaterialName(ItemStack item) {
        MaterialAndData material = new MaterialAndData(item);
        return material.getName();
    }

    public static String getMaterialName(Block block) {
        MaterialAndData material = new MaterialAndData(block);
        return material.getName();
    }

    @Nullable
    @Override
    public String getBaseName() {
        if (material == null) {
            return null;
        }
        return material.name().toLowerCase().replace('_', ' ');
    }

    @Override
    public String getName() {
        return getName(null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String getName(Messages messages) {
        if (!isValid()) return "";
        VaultController controller = VaultController.getInstance();
        if (controller != null && data != null) {
            try {
                String vaultName = controller.getItemName(material, data);
                if (vaultName != null && !vaultName.isEmpty()) {
                    return vaultName;
                }
            } catch (Throwable ex) {
                // Vault apparently throws exceptions on invalid item types
                // So we're just going to ignore it.
            }
        }

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
            } else if (material == Material.MOB_SPAWNER && extraData != null && extraData instanceof BlockMobSpawner) {
                 BlockMobSpawner spawnerData = (BlockMobSpawner)extraData;
                 if (spawnerData.mobName != null && !spawnerData.mobName.isEmpty())
                 {
                     materialName = materialName + " (" + spawnerData.mobName + ")";
                 }
            } else if ((material == Material.STANDING_BANNER || material == Material.WALL_BANNER || material == Material.BANNER) && extraData != null && extraData instanceof BlockBanner) {
                 DyeColor color = ((BlockBanner)extraData).baseColor;
                 if (color != null) {
                     materialName = color.name().toLowerCase() + " " + materialName;
                 }
            }
        } else if (messages != null) {
            materialName = materialName + messages.get("material.wildcard");
        }

        materialName = materialName.toLowerCase().replace('_', ' ');
        return materialName;
    }

    @Override
    public void setCustomName(String customName) {
        if (extraData != null && extraData instanceof BlockCommand) {
            ((BlockCommand)extraData).customName = customName;
        } else {
            extraData = new BlockCommand(null, customName);
        }
    }

    @Override
    public void setCommandLine(String command) {
        if (extraData != null && extraData instanceof BlockCommand) {
            ((BlockCommand)extraData).command = command;
        } else {
            extraData = new BlockCommand(command);
        }
    }

    @Nullable
    @Override
    public String getCommandLine() {
        if (extraData != null && extraData instanceof BlockCommand) {
            return ((BlockCommand)extraData).command;
        }
        return null;
    }

    @Override
    public void setData(Short data) {
        this.data = data;
    }

    @Override
    public void setRawData(Object data) {
        if (material == Material.SIGN_POST || material == Material.WALL_SIGN)
        {
            this.extraData = new BlockSign(data);
        }
        else
        {
            this.extraData = new BlockTileEntity(data);
        }
    }

    @Override
    public String toString() {
        return (isValid() ? material + (data != 0 ? "@" + data : "") : "invalid");
    }
}
