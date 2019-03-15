package com.elmakers.mine.bukkit.block;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.api.block.ModifyType;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;
import com.elmakers.mine.bukkit.utility.SkinUtils;
import com.elmakers.mine.bukkit.utility.SkullLoadedCallback;
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
    protected String blockData;
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

    public MaterialAndData(final Material material, String blockData) {
        this.material = material;
        this.blockData = blockData;
    }

    public MaterialAndData(final Material material, final  short data) {
        this.material = material;
        this.data = data;
    }

    public MaterialAndData(ItemStack item) {
        this.material = item.getType();
        this.data = item.getDurability();
        if (DefaultMaterials.isPlayerSkull(this))
        {
            ItemMeta meta = item.getItemMeta();
            Object profile = InventoryUtils.getSkullProfile(meta);
            extraData = new BlockSkull(profile);
        } else if (DefaultMaterials.isBanner(this.material)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof BannerMeta)
            {
                BannerMeta banner = (BannerMeta)meta;
                extraData = new BlockBanner(banner.getPatterns(), DeprecatedUtils.getBaseColor(banner));
            }
        } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof LeatherArmorMeta) {
                extraData = new LeatherArmorData(((LeatherArmorMeta)meta).getColor());
            }
        } else if (this.material == Material.POTION) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof PotionMeta) {
                extraData = new PotionData(CompatibilityUtils.getColor((PotionMeta)meta));
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
        String blockData = null;
        materialKey = pieces[0];
        if (materialKey.equalsIgnoreCase("skull") || materialKey.equalsIgnoreCase("skull_item")) {
            MaterialAndData skullData = DefaultMaterials.getPlayerSkullItem();
            if (skullData != null) {
                material = skullData.material;
                data = skullData.data;
            }
        }

        if (material == null) {
            try {
                if (pieces.length > 0) {
                    if (!materialKey.equals("*")) {
                        // Legacy material id loading
                        try {
                            Integer id = Integer.parseInt(materialKey);
                            material = CompatibilityUtils.getMaterial(id);
                        } catch (Exception ex) {
                            materialKey = materialKey.toUpperCase();
                            material = Material.getMaterial(materialKey);
                            if (material == null) {
                                material = CompatibilityUtils.getLegacyMaterial(materialKey);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                material = null;
            }
        }
        try {
            if (pieces.length > 1) {
                String dataString = pieces[1];
                String[] blockPieces = StringUtils.split(dataString, "?");
                if (blockPieces.length > 0) {
                    dataString = blockPieces[0];
                    if (blockPieces.length > 1) {
                        blockData = blockPieces[1];
                    }
                }

                // Some special-cases
                if (dataString.equals("*")) {
                    data = null;
                } else if (DefaultMaterials.isMobSpawner(material)) {
                    extraData = new BlockMobSpawner(dataString);
                } else if (DefaultMaterials.isSkull(material)) {
                    if (dataString.contains(":")) {
                        data = 3;
                        this.material = material;
                        this.data = data;
                        ItemStack item = getItemStack(1);
                        item = InventoryUtils.setSkullURL(item, dataString);
                        extraData = new BlockSkull(InventoryUtils.getSkullProfile(item.getItemMeta()));
                    } else {
                        try {
                            data = Short.parseShort(dataString);
                        } catch (Exception ex) {
                            data = 3;
                            extraData = new BlockSkull(dataString);
                        }
                    }
                } else if (DefaultMaterials.isBanner(material)) {
                    DyeColor color = null;
                    try {
                        short colorIndex = Short.parseShort(dataString);
                        data = colorIndex;
                        color = DyeColor.values()[colorIndex];
                    }
                    catch (Exception ex) {
                        color = null;
                    }
                    if (color != null) {
                        extraData = new BlockBanner(color);
                    }
                } else if (material == Material.LEATHER_BOOTS || material == Material.LEATHER_CHESTPLATE
                        || material == Material.LEATHER_HELMET || material == Material.LEATHER_LEGGINGS) {
                    StringUtils.split(dataString, ',');
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
                                data = Short.parseShort(dataString);
                            } catch (Exception ex) {
                                data = 0;
                            }
                        }
                    }
                } else if (material == Material.POTION) {
                    String color = dataString;
                    if (color.startsWith("#")) {
                        color = color.substring(1);
                    }
                    try {
                        Color potionColor = Color.fromRGB(Integer.parseInt(color, 16));
                        extraData = new PotionData(potionColor);
                    } catch (Exception ex) {
                        extraData = null;
                    }
                } else {
                    try {
                        data = Short.parseShort(dataString);
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
            this.blockData = blockData;
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
            blockData = o.blockData;
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
        blockData = null;
        if (material != null && CompatibilityUtils.isLegacy(material)) {
            short convertData = (this.data == null ? 0 : this.data);
            material = CompatibilityUtils.migrateMaterial(material, (byte)convertData);
            this.material = material;
        }

        isValid = material != null;
    }

    @Override
    public void setMaterial(Material material) {
        setMaterial(material, (byte)0);
    }

    public void setMaterialId(int id) {
        this.material = CompatibilityUtils.getMaterial(id);
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
                if (!NMSUtils.isCurrentVersion()) {
                    data = (short)skull.getSkullType().ordinal();
                }
                extraData = new BlockSkull(InventoryUtils.getSkullProfile(skull), skull.getRotation());
            } else if (blockState instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner)blockState;
                extraData = new BlockMobSpawner(spawner.getCreatureTypeName());
            } else if (DefaultMaterials.isBanner(blockMaterial)) {
                if (blockState != null && blockState instanceof Banner) {
                    Banner banner = (Banner)blockState;
                    DyeColor color = banner.getBaseColor();
                    extraData = new BlockBanner(banner.getPatterns(), color);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        blockData = CompatibilityUtils.getBlockData(block);
        if (blockData != null) {
            // If we have block data, the data byte is no longer relevant
            data = 0;
            // If the blockData is not complex we don't really need to keep it, though this is making some nasty
            // assumptions about this string representation
            if (!blockData.contains("[")) {
                blockData = null;
            }
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

                DeprecatedUtils.setTypeAndData(block, material, blockData, applyPhysics);

                if (this.blockData != null) {
                    CompatibilityUtils.setBlockData(Bukkit.getServer(), block, this.blockData);
                }
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
            } else if (blockState != null && DefaultMaterials.isBanner(material) && extraData != null && extraData instanceof BlockBanner) {
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
                // Don't do this in 1.13
                if (data != null && data != 0 && !NMSUtils.isCurrentVersion()) {
                    skull.setSkullType(SkullType.values()[data]);
                }
                if (skullData.rotation != null && skullData.rotation != org.bukkit.block.BlockFace.SELF) {
                    skull.setRotation(skullData.rotation);
                }
                if (skullData.profile != null) {
                    InventoryUtils.setSkullProfile(skull, skullData.profile);
                    skull.update(true, false);
                } else if (skullData.playerName != null) {
                    DeprecatedUtils.setOwner(skull, skullData.playerName);
                }
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
            if (DefaultMaterials.isPlayerSkull(this) && extraData != null && extraData instanceof BlockSkull) {
                BlockSkull skullData = (BlockSkull)extraData;
                if (skullData.playerName != null) {
                    materialKey += ":" + skullData.playerName;
                } else {
                    String profileURL = SkinUtils.getProfileURL(skullData.profile);
                    if (profileURL != null) {
                        materialKey += ":" + profileURL;
                    }
                }
            }
            else if (DefaultMaterials.isMobSpawner(material) && extraData != null && extraData instanceof BlockMobSpawner) {
                BlockMobSpawner spawnerData = (BlockMobSpawner)extraData;
                if (spawnerData.mobName != null && !spawnerData.mobName.isEmpty()) {
                    materialKey += ":" + spawnerData.mobName;
                }
            }
            else if (DefaultMaterials.isBanner(material) && extraData != null && extraData instanceof BlockBanner && ((BlockBanner)extraData).baseColor != null) {
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
            } else if (this.material == Material.POTION) {
                if (extraData != null && extraData instanceof PotionData) {
                    Color color = ((PotionData)extraData).getColor();
                    if (color != null) {
                        materialKey += ":" + Integer.toHexString(color.asRGB());
                    }
                }
            } else if (data != 0) {
                materialKey += ":" + data;
            }
        }
        if (blockData != null) {
            materialKey += "?" + blockData;
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
        if (NMSUtils.isCurrentVersion()) {
            return material == block.getType();
        }
        return material == block.getType() && data == block.getData();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isDifferent(Block block) {
        Material blockMaterial = block.getType();
        if (material != null && blockMaterial != material) {
            return true;
        }
        if (!NMSUtils.isCurrentVersion()) {
            byte blockData = block.getData();
            if (data != null && blockData != data) {
                return true;
            }
        }

        // Special cases
        if (DefaultMaterials.isBanner(material)) {
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
        }

        return false;
    }

    @Nullable
    @Override
    public ItemStack getItemStack(int amount) {
        return getItemStack(amount, null);
    }

    @Nullable
    @Override
    public ItemStack getItemStack(int amount, ItemUpdatedCallback callback) {
        if (material == null) {
            if (callback != null) {
                callback.updated(null);
            }
            return null;
        }

        Material material = convertToItemStackMaterial();
        MaterialAndData item = this;
        if (material != this.material) {
            item = new MaterialAndData(this);
            item.material = material;
        }

        ItemStack stack = new ItemStack(material, amount, data == null ? 0 : data);
        stack = item.applyToItem(stack, callback);
        return stack;
    }

    private Material convertToItemStackMaterial() {
        return DefaultMaterials.blockToItem(material);
    }

    @Override
    public ItemStack applyToItem(ItemStack stack) {
        return applyToItem(stack, null);
    }

    @Override
    public ItemStack applyToItem(ItemStack stack, ItemUpdatedCallback callback)
    {
        boolean asynchronous = false;
        stack.setType(material);
        if (data != null) {
            stack.setDurability(data);
        }
        if (DefaultMaterials.isPlayerSkull(this))
        {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof SkullMeta && extraData != null && extraData instanceof BlockSkull)
            {
                BlockSkull skullData = (BlockSkull)extraData;
                if (skullData.profile != null) {
                    SkullMeta skullMeta = (SkullMeta)meta;
                    InventoryUtils.setSkullProfile(skullMeta, ((BlockSkull)extraData).profile);
                    stack.setItemMeta(meta);
                } else if (skullData.playerName != null) {
                    asynchronous = true;
                    SkullLoadedCallback skullCallback = null;
                    if (callback != null) {
                        skullCallback = new SkullLoadedCallback() {
                            @Override
                            public void updated(ItemStack itemStack) {
                                callback.updated(itemStack);
                            }
                        };
                    }
                    DeprecatedUtils.setSkullOwner(stack, skullData.playerName, skullCallback);
                }
            }
        } else if (DefaultMaterials.isBanner(material)) {
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
                    DeprecatedUtils.setBaseColor(banner, bannerData.baseColor);
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
        } else if (this.material == Material.POTION) {
            ItemMeta meta = stack.getItemMeta();
            if (extraData != null && extraData instanceof PotionData && meta != null && meta instanceof PotionMeta) {
                CompatibilityUtils.setColor((PotionMeta)meta, ((PotionData)extraData).getColor());
                stack.setItemMeta(meta);
            }
        }
        if (!asynchronous && callback != null) {
            callback.updated(stack);
        }
        return stack;
    }

    public static String[] splitMaterialKey(String materialKey) {
        if (materialKey.contains("|")) {
            return StringUtils.split(materialKey, "|", 2);
        } else if (materialKey.contains(":")) {
            return StringUtils.split(materialKey, ":", 2);
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
    public String getName(Messages messages) {
        if (!isValid()) return "";

        String materialName = material == null ? "?" : material.name();

        if (data == null && messages != null) {
            materialName = materialName + messages.get("material.wildcard");
        }

        materialName = materialName.toLowerCase().replace('_', ' ');
        materialName = WordUtils.capitalize(materialName);
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
        if (DefaultMaterials.isSign(material))
        {
            this.extraData = new BlockSign(data);
        }
        else
        {
            this.extraData = new BlockTileEntity(data);
        }
    }

    public boolean matches(Material material, short data) {
        return (this.material == null || this.material == material)
                && (this.data == null || this.data == data);
    }

    @Override
    public String toString() {
        return (isValid() ? material + (data != 0 ? "@" + data : "") : "invalid");
    }
}
