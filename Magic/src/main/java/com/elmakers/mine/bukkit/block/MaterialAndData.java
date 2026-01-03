package com.elmakers.mine.bukkit.block;

import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerProfile;

import com.elmakers.mine.bukkit.api.block.ModifyType;
import com.elmakers.mine.bukkit.api.item.ItemUpdatedCallback;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.SkullLoadedCallback;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.utility.TextUtils;
import com.elmakers.mine.bukkit.utility.WordUtils;
import com.elmakers.mine.bukkit.utility.platform.CompatibilityUtils;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * A utility class for presenting a Material in its entirety, including Material variants.
 *
 * <p>This will probably need an overhaul for 1.8, but I'm hoping that using this class everywhere as an intermediate for
 * the concept of "material type" will allow for a relatively easy transition. We'll see.
 *
 * <p>In the meantime, this class primarily uses String-based "keys" to identify a material. This is not
 * necessarily meant to be a friendly or printable name, though the class is capable of generating a semi-friendly
 * name, which will be the key lowercased and with underscores replaced with spaces. It will also attempt to create
 * a nice name for the variant, such as "blue wool". There is no DB for this, it is all based on the internal Bukkit
 * Material enumerations.
 *
 * <p>Some examples of keys:
 * wool
 * diamond_block
 * monster_egg
 * wooden_hoe:15 (for a damaged tool)
 * wooden_hoe{CustomModelData:32}
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
    protected static Gson gson;

    protected Material material;
    protected Short data;
    protected int customModelData;
    protected String itemModel;
    protected MaterialExtraData extraData;
    protected GenericExtraData genericExtraData;
    protected String blockData;
    protected boolean isValid = true;
    protected boolean isTargetValid = true;

    public static final Material DEFAULT_MATERIAL = Material.AIR;
    public static boolean DEBUG = false;

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

    public MaterialAndData(final Material material, final short data) {
        this(material, (Short)data);
    }

    public MaterialAndData(final Material material, final Short data) {
        this.material = material;
        this.data = data;
    }

    public MaterialAndData(ItemStack item) {
        this.material = item.getType();
        this.data = CompatibilityLib.getDeprecatedUtils().getItemDamage(item);
        if (DefaultMaterials.isPlayerSkull(this))
        {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof SkullMeta) {
                SkullMeta skullMeta = (SkullMeta)meta;
                PlayerProfile profile = skullMeta.getOwnerProfile();
                if (profile != null) {
                    extraData = new BlockSkull(profile);
                }
            }
        } else if (DefaultMaterials.isBanner(this.material)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof BannerMeta)
            {
                BannerMeta banner = (BannerMeta)meta;
                extraData = new BlockBanner(banner.getPatterns());
            }
        } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS
                || this.material.name().equals("LEATHER_HORSE_ARMOR")) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof LeatherArmorMeta) {
                extraData = new LeatherArmorData(((LeatherArmorMeta)meta).getColor());
            }
        } else if (this.material == Material.POTION || this.material == Material.TIPPED_ARROW) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta)meta;
                Color color = CompatibilityLib.getCompatibilityUtils().getColor(potionMeta);
                extraData = new PotionData(color, potionMeta.getCustomEffects());
            }
        } else if (this.material == DefaultMaterials.getFireworkStar()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof FireworkEffectMeta) {
                FireworkEffect effect = ((FireworkEffectMeta)meta).getEffect();
                if (effect != null && !effect.getColors().isEmpty()) {
                    extraData = new ColoredData(effect.getColors().get(0));
                }
            }
        } else if (this.material == Material.WRITTEN_BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof BookMeta) {
                extraData = new WrittenBookData((BookMeta)meta);
            }
        } else if (this.material == Material.ENCHANTED_BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta enchantStorage = (EnchantmentStorageMeta)meta;
                Map<Enchantment, Integer> enchants = enchantStorage.getStoredEnchants();
                if (enchants != null && !enchants.isEmpty()) {
                    extraData = new EnchantmentData(enchants);
                }
            }
        }

        if (item.hasItemMeta()) {
            item = CompatibilityLib.getItemUtils().makeReal(item);
            customModelData = CompatibilityLib.getItemUtils().getCustomModelData(item);
            if (item.getItemMeta().hasItemModel()) {
                itemModel = item.getItemMeta().getItemModel().toString();
            }

            Object equippable = CompatibilityLib.getItemUtils().getEquippable(item);
            if (equippable != null) {
                genericExtraData = new GenericExtraData();
                genericExtraData.setEquippable(equippable);
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

    public void copyTo(MaterialAndData other) {
        other.material = material;
        other.data = data;
        // Note: shallow copies!
        other.customModelData = customModelData;
        other.itemModel = itemModel;
        other.extraData = extraData;
        other.blockData = blockData;
        other.isValid = isValid;
        other.isTargetValid = isTargetValid;
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public void update(String materialKey) {
        if (materialKey == null || materialKey.length() == 0) {
            isValid = false;
            return;
        }

        // Check for block data
        String originalKey = materialKey;
        String blockData = null;
        String[] blockPieces = StringUtils.split(materialKey, "?");
        if (blockPieces.length > 1) {
            materialKey = blockPieces[0];
            blockData = blockPieces[1];
        }

        MaterialExtraData extraData = null;
        int jsonStart = materialKey.indexOf('{');
        if (jsonStart > 0) {
            String fullKey = materialKey;
            materialKey = fullKey.substring(0, jsonStart);
            String json = fullKey.substring(jsonStart);
            int jsonEnd = json.lastIndexOf('}');
            if (jsonEnd != json.length() - 1) {
                materialKey += json.substring(jsonEnd + 1);
                json = json.substring(0, jsonEnd + 1);
            }
            if (!json.contains(":")) {
                try {
                    customModelData = Integer.parseInt(json.substring(1, json.length() - 1));
                } catch (Exception ex) {
                    Bukkit.getLogger().warning("[Magic] Error parsing item custom model data: " + json + " : " + ex.getMessage());
                }
            } else {
                try {
                    // TODO: Support more tags ?
                    JsonReader reader = new JsonReader(new StringReader(json));
                    reader.setLenient(true);
                    Map<String, Object> tags = getGson().fromJson(reader, Map.class);
                    CompatibilityLib.getNBTUtils().convertIntegers(tags);
                    for (Map.Entry<String, Object> entry : tags.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        switch (key) {
                            case "item_model":
                            case "ItemModel":
                                try {
                                    itemModel = value.toString();
                                } catch (Exception ex) {
                                    Bukkit.getLogger().info("[Magic] [" + originalKey + "] Unsupported ItemModel value: " + value);
                                }
                                break;
                            case "custom_model_data":
                            case "CustomModelData":
                                if (value instanceof Integer) {
                                    customModelData = (int)(Integer)value;
                                } else {
                                    Bukkit.getLogger().info("[Magic] [" + originalKey + "] Unsupported CustomModelData value: " + value);
                                }
                                break;
                            case "Potion":
                                try {
                                    PotionEffectType effectType = PotionEffectType.getByName(value.toString().toUpperCase());
                                    PotionEffect effect = new PotionEffect(effectType, 20 * 60, 0);
                                    List<PotionEffect> effects = new ArrayList<>();
                                    effects.add(effect);
                                    extraData = new PotionData(null, effects);
                                } catch (Exception ignore) {
                                    Bukkit.getLogger().info("[Magic] [" + originalKey + "] Error parsing potion effect data: " + value);
                                }
                                break;
                            case "Color":
                                ColorHD color = new ColorHD(value.toString());
                                extraData = new ColoredData(color.getColor());
                                break;
                            default:
                                Bukkit.getLogger().info("[Magic] [" + originalKey + "] Custom NBT tags on items are no longer supported. Unknown tag key: " + key);
                                break;
                        }
                    }
                } catch (Throwable ex) {
                    Bukkit.getLogger().warning("[Magic] Error parsing item json: " + json + " : " + ex.getMessage());
                }
            }
        }
        String[] pieces = splitMaterialKey(materialKey);
        Short data = 0;
        Material material = null;
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
                        materialKey = materialKey.toUpperCase();
                        material = Material.getMaterial(materialKey);
                        if (DEBUG && material == null) {
                            Bukkit.getLogger().warning("Unknown material id: " + materialKey);
                        }
                    }
                }
            } catch (Exception ex) {
                material = null;
                Bukkit.getLogger().log(Level.WARNING, "Error parsing material key: " + materialKey, ex);
            }
        }
        try {
            if (pieces.length > 1) {
                String dataString = pieces[1];

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
                        item = CompatibilityLib.getInventoryUtils().setSkullURL(item, dataString);
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta instanceof SkullMeta) {
                            SkullMeta skullMeta = (SkullMeta)itemMeta;
                            PlayerProfile playerProfile = skullMeta.getOwnerProfile();
                            if (playerProfile != null) {
                                extraData = new BlockSkull(playerProfile);
                            }
                        }
                    } else {
                        try {
                            data = Short.parseShort(dataString);
                        } catch (Exception ex) {
                            data = 3;
                            extraData = new BlockSkull(dataString);
                        }
                    }
                } else if (material == Material.LEATHER_BOOTS || material == Material.LEATHER_CHESTPLATE
                        || material == Material.LEATHER_HELMET || material == Material.LEATHER_LEGGINGS
                        || material.name().equals("LEATHER_HORSE_ARMOR")) {
                    String[] colorPieces = StringUtils.split(dataString, ',');
                    for (String piece : colorPieces) {
                        if (piece.startsWith("#")) {
                            Color color = ConfigurationUtils.toColor(piece);
                            if (color != null) {
                                extraData = new LeatherArmorData(color);
                            }
                        } else {
                            try {
                                data = Short.parseShort(dataString);
                            } catch (Exception ex) {
                                data = 0;
                            }
                        }
                    }
                } else if (material == Material.POTION || material == Material.TIPPED_ARROW) {
                    String[] colorPieces = StringUtils.split(dataString, ',');
                    Color color = Color.WHITE;
                    List<PotionEffect> effects = new ArrayList<>();
                    PotionEffectType effectType = null;
                    Integer amplifier = null;
                    Integer duration = null;
                    for (String piece : colorPieces) {
                        if (piece.startsWith("#")) {
                            color = ConfigurationUtils.toColor(piece);
                        } else {
                            if (effectType == null) {
                                try {
                                    effectType = PotionEffectType.getByName(piece.toUpperCase());
                                } catch (Exception ignore) {
                                    effectType = null;
                                }
                            } else {
                                if (duration == null) {
                                    try {
                                        duration = Integer.parseInt(piece) / 50;
                                    } catch (Exception ignore) {
                                    }
                                } else if (amplifier == null) {
                                    try {
                                        amplifier = Integer.parseInt(piece);
                                    } catch (Exception ignore) {
                                    }
                                }
                            }
                        }
                    }
                    if (effectType != null) {
                        effects.add(new PotionEffect(effectType, duration == null ? 20 * 60 : duration, amplifier == null ? 0 : amplifier));
                    }
                    extraData = new PotionData(color, effects);
                } else if (material == DefaultMaterials.getFireworkStar()) {
                    Color color = ConfigurationUtils.toColor(dataString);
                    extraData = new ColoredData(color);
                } else if (material == Material.ENCHANTED_BOOK) {
                    Map<Enchantment, Integer> enchants = new HashMap<>();
                    String[] list = StringUtils.split(dataString, ",");
                    for (String enchantKey : list) {
                        int level = 1;
                        String[] enchantPieces = StringUtils.split(enchantKey, ":");
                        if (enchantPieces.length > 1) {
                            try {
                                enchantKey = enchantPieces[0];
                                level = Integer.parseInt(enchantPieces[1]);
                            } catch (Exception ex) {
                                level = 1;
                            }
                        }
                        Enchantment enchantment = CompatibilityLib.getCompatibilityUtils().getEnchantmentByKey(enchantKey);
                        if (enchantment != null) {
                            enchants.put(enchantment, level);
                        }
                    }
                    if (!enchants.isEmpty()) {
                        extraData = new EnchantmentData(enchants);
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

    @Override
    public int hashCode() {
        // Note that this does not incorporate any metadata!
        return (material == null ? -1 : (material.ordinal() << 16)) | (data == null ? -1 : data);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof com.elmakers.mine.bukkit.api.block.MaterialAndData)) {
            return false;
        }

        com.elmakers.mine.bukkit.api.block.MaterialAndData other = (com.elmakers.mine.bukkit.api.block.MaterialAndData)obj;
        MaterialAndData o = (MaterialAndData) other;
        return Objects.equal(o.getData(), data)
                && o.getMaterial() == material
                && customModelData == o.customModelData
                && Objects.equal(itemModel, o.itemModel);
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
            isTargetValid = o.isTargetValid;
            customModelData = o.customModelData;
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
        isValid = material != null;
        isTargetValid = true;
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
        if (!CompatibilityLib.getCompatibilityUtils().checkChunk(block.getLocation())) {
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
        data = 0;

        try {
            BlockState blockState = block.getState();
            if (material == Material.FLOWER_POT
                    || blockState instanceof InventoryHolder
                    || blockState instanceof Sign
                    || blockState instanceof Jukebox) {
                extraData = new BlockTileEntity(CompatibilityLib.getCompatibilityUtils().getTileEntityData(block.getLocation()));
            } else if (blockState instanceof CommandBlock) {
                // This seems to occasionally throw exceptions...
                CommandBlock command = (CommandBlock)blockState;
                extraData = new BlockCommand(command.getCommand(), command.getName());
            } else if (blockState instanceof Skull) {
                Skull skull = (Skull)blockState;
                data = CompatibilityLib.getDeprecatedUtils().getSkullType(skull);
                PlayerProfile playerProfile = skull.getOwnerProfile();
                extraData = new BlockSkull(playerProfile, skull.getRotation());
            } else if (blockState instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner)blockState;
                extraData = new BlockMobSpawner(spawner.getCreatureTypeName());
            } else if (DefaultMaterials.isBanner(blockMaterial)) {
                if (blockState != null && blockState instanceof Banner) {
                    Banner banner = (Banner)blockState;
                    extraData = new BlockBanner(banner.getPatterns());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        blockData = CompatibilityLib.getCompatibilityUtils().getBlockData(block);
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
        isTargetValid = true;
    }

    /**
     * Determines whether items in a block state should be cleared before
     * breaking a block.
     *
     * @param block
     *            The block state to check.
     * @return Whether
     *         {@link CompatibilityUtils#clearItems(org.bukkit.Location)} should
     *         be called before setting a block to air, to ensure no items will
     *         be dropped.
     */
    public static boolean shouldClearItemsIn(@Nonnull BlockState block) {
        return block instanceof InventoryHolder
                || block instanceof Jukebox
                || block.getType() == Material.FLOWER_POT;
    }

    public static void clearItems(BlockState block) {
        if (block != null && shouldClearItemsIn(block)) {
            CompatibilityLib.getCompatibilityUtils().clearItems(block.getLocation());
        }
    }

    @SuppressWarnings("deprecation")
    public void modifyFast(Block block) {
        Material material = this.material == null ? block.getType() : this.material;
        if (material != block.getType()) {
            CompatibilityLib.getCompatibilityUtils().setBlockFast(block, material, 0);
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
        if (!isValid || !isTargetValid) return;

        try {
            BlockState blockState = block.getState();
            if (material != null) {
                Material currentMaterial = block.getType();

                String extendedBlockData = this.blockData;
                if (data == null && extendedBlockData == null) {
                    extendedBlockData = CompatibilityLib.getCompatibilityUtils().getBlockData(block);
                }

                // Clear chests and flower pots so they don't dump their contents.
                clearItems(blockState);

                CompatibilityLib.getDeprecatedUtils().setTypeAndData(block, material, (byte)0, applyPhysics);
                if (extendedBlockData != null) {
                    if (currentMaterial != material) {
                        String currentBlockData =  CompatibilityLib.getCompatibilityUtils().getBlockData(block);
                        if (currentBlockData != null) {
                            // Hacky, yes... is there a better way?
                            // Is this going to cause some real strange behavior?
                            String[] currentData = StringUtils.split(currentBlockData, "[", 2);
                            String[] newData = StringUtils.split(extendedBlockData, "[", 2);
                            if (newData.length > 1) {
                                extendedBlockData = currentData[0] + "[" + newData[1];
                            }
                        }
                    }

                    CompatibilityLib.getCompatibilityUtils().setBlockData(block, extendedBlockData);
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
                if (allowContainers() || (material != Material.FLOWER_POT && !(blockState instanceof InventoryHolder))) {
                    CompatibilityLib.getCompatibilityUtils().setTileEntityData(block.getLocation(), ((BlockTileEntity) extraData).data);
                }
            } else if (blockState != null && DefaultMaterials.isBanner(material) && extraData != null && extraData instanceof BlockBanner) {
                if (blockState instanceof Banner) {
                    BlockBanner bannerData = (BlockBanner)extraData;
                    Banner banner = (Banner)blockState;
                    if (bannerData.patterns != null) {
                        banner.setPatterns(bannerData.patterns);
                    }
                }
                blockState.update(true, false);
            } else if (blockState != null && blockState instanceof Skull && extraData != null && extraData instanceof BlockSkull) {
                Skull skull = (Skull)blockState;
                BlockSkull skullData = (BlockSkull)extraData;
                if (data != null && data != 0) {
                    CompatibilityLib.getDeprecatedUtils().setSkullType(skull, data);
                }
                org.bukkit.block.BlockFace rotation = skullData.getRotation();
                if (rotation != null && rotation != org.bukkit.block.BlockFace.SELF) {
                    skull.setRotation(rotation);
                }
                PlayerProfile profile = skullData.getProfile();
                String playerName = skullData.getPlayerName();
                if (profile != null) {
                    skull.setOwnerProfile(profile);
                    skull.update(true, false);
                } else if (playerName != null) {
                    CompatibilityLib.getDeprecatedUtils().setOwner(skull, playerName);
                }
            } else if (blockState != null && blockState instanceof CreatureSpawner && extraData != null && extraData instanceof BlockMobSpawner) {
                BlockMobSpawner spawnerData = (BlockMobSpawner)extraData;
                if (spawnerData.mobName != null && !spawnerData.mobName.isEmpty())
                {
                    CreatureSpawner spawner = (CreatureSpawner)blockState;
                    spawner.setCreatureTypeByName(spawnerData.mobName);
                    spawner.update();
                }
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

    @Nullable
    @Override
    public String getModernBlockData() {
        return blockData;
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
                String playerName = skullData.getPlayerName();
                if (playerName != null) {
                    materialKey += ":" + playerName;
                } else {
                    PlayerProfile profile = skullData.getProfile();
                    URL skinURL = profile == null ? null : profile.getTextures().getSkin();
                    if (skinURL != null) {
                        materialKey += ":" + skinURL.toString();
                    }
                }
            }
            else if (DefaultMaterials.isMobSpawner(material) && extraData != null && extraData instanceof BlockMobSpawner) {
                BlockMobSpawner spawnerData = (BlockMobSpawner)extraData;
                if (spawnerData.mobName != null && !spawnerData.mobName.isEmpty()) {
                    materialKey += ":" + spawnerData.mobName;
                }
            } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                    || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS
                    || this.material.name().equals("LEATHER_HORSE_ARMOR")) {
                if (data != 0)
                    materialKey += ":" + data;
                if (extraData != null && extraData instanceof LeatherArmorData) {
                    Color color = ((LeatherArmorData)extraData).getColor();
                    if (data != 0) {
                        materialKey += ",#" + TextUtils.toHexString(color.asRGB());
                    } else {
                        materialKey += ":#" + TextUtils.toHexString(color.asRGB());
                    }
                }
            } else if (this.material == Material.POTION || this.material == Material.TIPPED_ARROW || this.material == DefaultMaterials.getFireworkStar()) {
                if (extraData != null && extraData instanceof ColoredData) {
                    Color color = ((ColoredData)extraData).getColor();
                    if (color != null) {
                        materialKey += ":" + TextUtils.toHexString(color.asRGB());
                    }
                }
            } else if (extraData != null && extraData instanceof EnchantmentData) {
                Map<Enchantment, Integer> enchants = ((EnchantmentData)extraData).getEnchantments();
                if (!enchants.isEmpty()) {
                    List<String> enchantKeys = new ArrayList<>();
                    for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                        String enchantKey = CompatibilityLib.getCompatibilityUtils().getEnchantmentKey(entry.getKey());
                        int level = entry.getValue();
                        if (level != 1) {
                            enchantKey = enchantKey + "|" + level;
                        }
                        enchantKeys.add(enchantKey);
                    }
                    materialKey += ":" + StringUtils.join(enchantKeys, ",");
                }
            } else if (data != 0) {
                materialKey += ":" + data;
            }
        }
        if (blockData != null) {
            materialKey += "?" + blockData;
        }
        if (itemModel != null && !itemModel.isEmpty()) {
            materialKey += "{item_model:" + itemModel + "}";
        } else if (customModelData != 0) {
            materialKey += "{" + customModelData + "}";
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
        return material == block.getType();
    }

    @Override
    public boolean isDifferent(Material blockMaterial) {
        return (material != null && blockMaterial != material);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isDifferent(Block block) {
        Material blockMaterial = block.getType();
        if (material != null && blockMaterial != material) {
            return true;
        }
        // Special cases
        if (DefaultMaterials.isBanner(material)) {
            // Can't compare patterns for now
            return true;
        }
        if (blockData != null) {
            String currentData = CompatibilityLib.getCompatibilityUtils().getBlockData(block);
            if (currentData == null || !blockData.equals(currentData)) {
                return true;
            }
        }
        // Error on the side of caution if we stored some extra data
        if (extraData != null) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isDifferent(ItemStack itemStack) {
        if (getMaterial() != itemStack.getType()) return true;
        if (getData() != CompatibilityLib.getDeprecatedUtils().getItemDamage(itemStack)) return true;
        int itemModelData = CompatibilityLib.getItemUtils().getCustomModelData(itemStack);
        if (customModelData != itemModelData) return true;
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

        ItemStack stack = CompatibilityLib.getDeprecatedUtils().createItemStack(material, amount, data == null ? 0 : data);
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
            CompatibilityLib.getDeprecatedUtils().setItemDamage(stack, data);
        }

        if (customModelData != 0) {
            CompatibilityLib.getItemUtils().setCustomModelData(stack, customModelData);
        }

        if (itemModel != null) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                meta.setItemModel(NamespacedKey.fromString(itemModel));
                stack.setItemMeta(meta);
            }
        }

        if (DefaultMaterials.isPlayerSkull(this))
        {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof SkullMeta && extraData != null && extraData instanceof BlockSkull)
            {
                BlockSkull skullData = (BlockSkull)extraData;
                PlayerProfile profile = skullData.getProfile();
                String playerName = skullData.getPlayerName();
                if (profile != null) {
                    SkullMeta skullMeta = (SkullMeta)meta;
                    skullMeta.setOwnerProfile(profile);
                    stack.setItemMeta(meta);
                } else if (playerName != null) {
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
                    CompatibilityLib.getDeprecatedUtils().setSkullOwner(stack, playerName, skullCallback);
                }
            }
        } else if (DefaultMaterials.isBanner(material)) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof BannerMeta && extraData != null && extraData instanceof BlockBanner)
            {
                BannerMeta banner = (BannerMeta)meta;
                BlockBanner bannerData = (BlockBanner)extraData;
                if (bannerData.patterns != null) {
                    banner.setPatterns(bannerData.patterns);
                }
                stack.setItemMeta(meta);
            }
        } else if (this.material == Material.LEATHER_BOOTS || this.material == Material.LEATHER_CHESTPLATE
                || this.material == Material.LEATHER_HELMET || this.material == Material.LEATHER_LEGGINGS
                || this.material.name().equals("LEATHER_HORSE_ARMOR")) {
            ItemMeta meta = stack.getItemMeta();
            if (extraData != null && extraData instanceof LeatherArmorData && meta != null && meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta)meta).setColor(((LeatherArmorData)extraData).getColor());
                stack.setItemMeta(meta);
            }
        } else if (this.material == Material.POTION || this.material == Material.TIPPED_ARROW) {
            ItemMeta meta = stack.getItemMeta();
            if (extraData != null && extraData instanceof PotionData && meta != null && meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta)meta;
                PotionData potionData = (PotionData)extraData;
                Color potionColor = potionData.getColor();
                if (potionColor != null) {
                    CompatibilityLib.getCompatibilityUtils().setColor(potionMeta, potionData.getColor());
                }
                potionMeta.clearCustomEffects();
                for (PotionEffect effect : potionData.getEffects()) {
                    potionMeta.addCustomEffect(effect, true);
                }
                stack.setItemMeta(meta);
            } else if (extraData != null && extraData instanceof ColoredData && meta != null && meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta)meta;
                ColoredData colorData = (ColoredData)extraData;
                Color potionColor = colorData.getColor();
                if (potionColor != null) {
                    CompatibilityLib.getCompatibilityUtils().setColor(potionMeta, colorData.getColor());
                }
                stack.setItemMeta(meta);
            }
        } else if (extraData != null && extraData instanceof EnchantmentData) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null && meta instanceof EnchantmentStorageMeta) {
                EnchantmentData enchantmentData = (EnchantmentData)extraData;
                EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta)meta;
                for (Map.Entry<Enchantment, Integer> entry : enchantmentData.getEnchantments().entrySet()) {
                    enchantmentStorage.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
                stack.setItemMeta(meta);
            }
        } else if (this.material == DefaultMaterials.getFireworkStar()) {
            ItemMeta meta = stack.getItemMeta();
            if (extraData != null && extraData instanceof ColoredData && meta != null && meta instanceof FireworkEffectMeta) {
                FireworkEffectMeta effectMeta = (FireworkEffectMeta)meta;
                ColoredData coloredData = (ColoredData)extraData;
                FireworkEffect existingEffect = effectMeta.getEffect();
                if (existingEffect != null) {
                    FireworkEffect.Type existingType = existingEffect.getType();
                    FireworkEffect fireworkEffect = FireworkEffect.builder()
                            .withColor(coloredData.getColor())
                            .flicker(existingEffect.hasFlicker())
                            .trail(existingEffect.hasTrail())
                            .with(existingType == null ? FireworkEffect.Type.BALL : existingType)
                            .build();
                    effectMeta.setEffect(fireworkEffect);
                } else {
                    effectMeta.setEffect(FireworkEffect.builder().withColor(coloredData.getColor()).build());
                }
                stack.setItemMeta(meta);
            }
        } else if (this.material == Material.WRITTEN_BOOK) {
            ItemMeta meta = stack.getItemMeta();
            if (extraData != null && extraData instanceof WrittenBookData && meta != null && meta instanceof BookMeta) {
                BookMeta book = (BookMeta) meta;
                WrittenBookData data = (WrittenBookData)extraData;
                data.applyTo(book);
                stack.setItemMeta(meta);
            }
        }
        if (genericExtraData != null) {
            CompatibilityLib.getItemUtils().setEquippable(stack, genericExtraData.getEquippable());
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

    public static String getMaterialName(ItemStack item, Messages messages) {
        MaterialAndData material = new MaterialAndData(item);
        return material.getName(messages);
    }

    public static String getMaterialName(Block block, Messages messages) {
        MaterialAndData material = new MaterialAndData(block);
        return material.getName(messages);
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

    @Nonnull
    @Override
    public String getName(@Nullable Messages messages) {
        if (!isValid()) return "";

        boolean localized = false;
        String materialName = material == null ? "?" : material.name();
        if (messages != null && material != null) {
            String localizedName = messages.get("materials." + material.name().toLowerCase(), "");
            if (!localizedName.isEmpty()) {
                materialName = localizedName;
                localized = true;
            }
        }
        if (data == null && messages != null) {
            materialName = materialName + messages.get("materials.wildcard");
        }

        if (!localized) {
            materialName = materialName.toLowerCase().replace('_', ' ');
            materialName = WordUtils.capitalize(materialName);
        }
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
        if (data == null) {
            this.extraData = null;
            this.blockData = null;
        }
    }

    @Override
    public void setRawData(Object data) {
        this.extraData = new BlockTileEntity(data);
    }

    public boolean matches(Material material, short data) {
        return (this.material == null || this.material == material)
                && (this.data == null || this.data == data);
    }

    @Nullable
    @Override
    public Map<String, Object> getTags() {
        return null;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    @Override
    public String toString() {
        return (isValid() ? material + (data != null && data != 0 ? "@" + data : "") : "invalid");
    }

    protected boolean allowContainers() {
        return true;
    }
}
