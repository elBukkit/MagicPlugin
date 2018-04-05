package com.elmakers.mine.bukkit.block;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
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
 * <p>This class primary uses String-based "keys" to identify a material. This is not
 * necessarily meant to be a friendly or printable name, though the class is capable of generating a semi-friendly
 * name, which will be the key lowercased and with underscores replaced with spaces. It will also attempt to create
 * a nice name for the variant, such as "blue wool". There is no DB for this, it is all based on the internal Bukkit
 * Material and MaterialData enumerations.
 *
 * <p>Some examples of keys:
 * wool
 * diamond_block
 * monster_egg
 * diamond_hoe:15 (for a diamond hoe at damage level 15)
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
        if (this.material == Material.PLAYER_HEAD)
        {
            ItemMeta meta = item.getItemMeta();
            Object profile = InventoryUtils.getSkullProfile(meta);
            SkullType skullType = SkullType.PLAYER;
            try {
                skullType = SkullType.values()[this.data];
            } catch (Exception ignored) {

            }
            extraData = new BlockSkull(profile, skullType);
        } else if (isBanner()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof BannerMeta)
            {
                BannerMeta banner = (BannerMeta)meta;
                extraData = new BlockBanner(banner.getPatterns());
            }
        } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof LeatherArmorMeta) {
                extraData = new LeatherArmorData(((LeatherArmorMeta)meta).getColor());
            }
        }
    }

    private boolean isBanner() {
        return isBanner(material);
    }

    public static boolean isBanner(Material material) {
        return material == Material.BLACK_BANNER || material == Material.BLACK_WALL_BANNER
            || material == Material.BLUE_BANNER || material == Material.BLUE_WALL_BANNER
            || material == Material.BROWN_BANNER || material == Material.BROWN_WALL_BANNER
            || material == Material.CYAN_BANNER || material == Material.CYAN_WALL_BANNER
            || material == Material.GRAY_BANNER || material == Material.GRAY_WALL_BANNER
            || material == Material.GREEN_BANNER || material == Material.GREEN_WALL_BANNER
            || material == Material.LIGHT_BLUE_BANNER || material == Material.LIGHT_BLUE_WALL_BANNER
            || material == Material.LIGHT_GRAY_BANNER || material == Material.LIGHT_GRAY_WALL_BANNER
            || material == Material.LIME_BANNER || material == Material.LIME_WALL_BANNER
            || material == Material.MAGENTA_BANNER || material == Material.MAGENTA_WALL_BANNER
            || material == Material.ORANGE_BANNER || material == Material.ORANGE_WALL_BANNER
            || material == Material.PINK_BANNER || material == Material.PINK_WALL_BANNER
            || material == Material.PURPLE_BANNER || material == Material.PURPLE_WALL_BANNER
            || material == Material.RED_BANNER || material == Material.RED_WALL_BANNER
            || material == Material.WHITE_BANNER || material == Material.WHITE_WALL_BANNER
            || material == Material.YELLOW_BANNER || material == Material.YELLOW_WALL_BANNER;
    }

    public static boolean isSkull(Material material) {
        return material == Material.PLAYER_HEAD || material == Material.PLAYER_WALL_HEAD
            || material == Material.WITHER_SKELETON_SKULL || material == Material.WITHER_SKELETON_WALL_SKULL
            || material == Material.DRAGON_HEAD || material == Material.DRAGON_WALL_HEAD
            || material == Material.ZOMBIE_HEAD || material == Material.ZOMBIE_WALL_HEAD
            || material == Material.SKELETON_SKULL || material == Material.SKELETON_WALL_SKULL
            || material == Material.CREEPER_HEAD || material == Material.CREEPER_WALL_HEAD;
    }

    public static boolean isCommand(Material material) {
        return material == Material.COMMAND_BLOCK || material == Material.CHAIN_COMMAND_BLOCK || material == Material.REPEATING_COMMAND_BLOCK;
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
                    material = Material.getMaterial(pieces[0].toUpperCase());
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
                else if (material == Material.PLAYER_HEAD) {
                    String dataString = pieces[1];
                    for (int i = 1; i < pieces.length; i++) {
                        dataString += ":" + pieces[i];
                    }
                    ItemStack item = InventoryUtils.getURLSkull(dataString);
                    extraData = new BlockSkull(InventoryUtils.getSkullProfile(item.getItemMeta()), SkullType.PLAYER);
                } else if (material == Material.LEATHER_BOOTS || material == Material.LEATHER_CHESTPLATE
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
            } else if (isBanner(blockMaterial)) {
                if (blockState != null && blockState instanceof Banner) {
                    Banner banner = (Banner)blockState;
                    DyeColor color = banner.getBaseColor();
                    extraData = new BlockBanner(banner.getPatterns());
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

                block.setType(material, applyPhysics);
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
            } else if (blockState != null && isBanner(material) && extraData != null && extraData instanceof BlockBanner) {
                if (blockState instanceof Banner) {
                    BlockBanner bannerData = (BlockBanner)extraData;
                    Banner banner = (Banner)blockState;
                    if (bannerData.patterns != null)
                    {
                        banner.setPatterns(bannerData.patterns);
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
            if (material == Material.PLAYER_HEAD && extraData != null && extraData instanceof BlockSkull) {
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
        if (isBanner(material)) {
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
            case PLAYER_WALL_HEAD: return material = Material.PLAYER_HEAD;
            case CREEPER_WALL_HEAD: return material = Material.CREEPER_HEAD;
            case SKELETON_WALL_SKULL: return material = Material.SKELETON_SKULL;
            case WITHER_SKELETON_WALL_SKULL: return material = Material.WITHER_SKELETON_SKULL;
            case DRAGON_WALL_HEAD: return material = Material.DRAGON_HEAD;
            case ZOMBIE_WALL_HEAD: return material = Material.ZOMBIE_HEAD;
            case BLACK_WALL_BANNER: return material = Material.BLACK_BANNER;
            case BLUE_WALL_BANNER: return material = Material.BLUE_BANNER;
            case BROWN_WALL_BANNER: return material = Material.BROWN_BANNER;
            case CYAN_WALL_BANNER: return material = Material.CYAN_BANNER;
            case GRAY_WALL_BANNER: return material = Material.GRAY_BANNER;
            case GREEN_WALL_BANNER: return material = Material.GREEN_BANNER;
            case LIGHT_BLUE_WALL_BANNER: return material = Material.LIGHT_BLUE_BANNER;
            case LIGHT_GRAY_WALL_BANNER: return material = Material.LIGHT_GRAY_BANNER;
            case LIME_WALL_BANNER: return material = Material.LIME_BANNER;
            case MAGENTA_WALL_BANNER: return material = Material.MAGENTA_BANNER;
            case ORANGE_WALL_BANNER: return material = Material.ORANGE_BANNER;
            case PINK_WALL_BANNER: return material = Material.PINK_BANNER;
            case PURPLE_WALL_BANNER: return material = Material.PURPLE_BANNER;
            case RED_WALL_BANNER: return material = Material.RED_BANNER;
            case WHITE_WALL_BANNER: return material = Material.WHITE_BANNER;
            case YELLOW_WALL_BANNER: return material = Material.YELLOW_BANNER;
            case WALL_SIGN: return material = Material.SIGN;
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
        if (material == Material.PLAYER_HEAD)
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
                    DeprecatedUtils.setSkullOwner(skullMeta, skullData.playerName);
                    stack.setItemMeta(meta);
                }
            }
        } else if (isBanner(material)) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof BannerMeta && extraData != null && extraData instanceof BlockBanner)
            {
                BannerMeta banner = (BannerMeta)meta;
                BlockBanner bannerData = (BlockBanner)extraData;
                if (bannerData.patterns != null)
                {
                    banner.setPatterns(bannerData.patterns);
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
        if (material == Material.SIGN || material == Material.WALL_SIGN)
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
