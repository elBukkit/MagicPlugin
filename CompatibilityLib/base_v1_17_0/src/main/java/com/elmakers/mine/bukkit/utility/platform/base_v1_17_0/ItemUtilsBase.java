package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.PlayerProfile;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.google.common.collect.Multimap;

public class ItemUtilsBase implements ItemUtils {
    protected final Platform platform;

    protected ItemUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public void addGlow(ItemStack stack) {
        if (isEmpty(stack)) return;

        try {
            ItemMeta meta = stack.getItemMeta();
            meta.addEnchant(Enchantment.LUCK, 1, true);
            stack.setItemMeta(meta);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeGlow(ItemStack stack) {
        if (isEmpty(stack)) return;

        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasEnchant(Enchantment.LUCK)) {
                meta.removeEnchant(Enchantment.LUCK);
                stack.setItemMeta(meta);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void makeTemporary(ItemStack itemStack, String message) {
        platform.getNBTUtils().setString(itemStack, "temporary", message);
    }

    @Override
    public boolean isTemporary(ItemStack itemStack) {
        return platform.getNBTUtils().containsTag(itemStack, "temporary");
    }

    @Override
    public void makeUnplaceable(ItemStack itemStack) {
        platform.getNBTUtils().setString(itemStack, "unplaceable", "true");
    }

    @Override
    public void removeUnplaceable(ItemStack itemStack) {
        platform.getNBTUtils().removeMeta(itemStack, "unplaceable");
    }

    @Override
    public boolean isUnplaceable(ItemStack itemStack) {
        return platform.getNBTUtils().containsTag(itemStack, "unplaceable");
    }

    @Override
    public String getTemporaryMessage(ItemStack itemStack) {
        return platform.getNBTUtils().getString(itemStack, "temporary");
    }

    @Override
    public void setReplacement(ItemStack itemStack, ItemStack replacement) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("item", replacement);
        platform.getNBTUtils().setString(itemStack, "replacement", configuration.saveToString());
    }

    @Override
    public ItemStack getReplacement(ItemStack itemStack) {
        String serialized = platform.getNBTUtils().getString(itemStack, "replacement");
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        YamlConfiguration configuration = new YamlConfiguration();
        ItemStack replacement = null;
        try {
            configuration.loadFromString(serialized);
            replacement = configuration.getItemStack("item");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return replacement;
    }

    @Override
    public boolean isSameItem(ItemStack first, ItemStack second) {
        if (first.getType() != second.getType()) return false;
        DeprecatedUtils deprecatedUtils = platform.getDeprecatedUtils();
        if (deprecatedUtils.getItemDamage(first) != deprecatedUtils.getItemDamage(second)) return false;
        return hasSameTags(first, second);
    }

    @Override
    public boolean hasSameTags(ItemStack first, ItemStack second) {
        Object firstTag = getTag(first);
        Object secondTag = getTag(second);
        return Objects.equals(firstTag, secondTag);
    }

    @Override
    public int getCustomModelData(ItemStack itemStack) {
        return platform.getNBTUtils().getInt(itemStack, "CustomModelData", 0);
    }

    @Override
    public void setCustomModelData(ItemStack itemStack, int customModelData) {
        platform.getNBTUtils().setInt(itemStack, "CustomModelData", customModelData);
    }

    @Override
    public Object getOrCreateTag(Object mcItemStack) {
        return getTag(mcItemStack);
    }

    @Override
    public Object getOrCreateTag(ItemStack itemStack) {
        return getTag(itemStack);
    }

    @Override
    public Object getHandle(ItemStack stack) {
        Object handle = null;
        try {
            handle = NMSUtils.class_CraftItemStack_getHandleField.get(stack);
        } catch (Throwable ex) {
            handle = null;
        }
        return handle;
    }

    @Override
    public Object getTag(Object mcItemStack) {
        Object tag = null;
        try {
            tag = NMSUtils.class_ItemStack_tagField.get(mcItemStack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    @Override
    public Object getTag(ItemStack itemStack) {
        Object tag = null;
        try {
            Object mcItemStack = getHandle(itemStack);
            if (mcItemStack == null) {
                if (itemStack.hasItemMeta()) {
                    itemStack = makeReal(itemStack);
                    mcItemStack = getHandle(itemStack);
                }
            }
            if (mcItemStack == null) return null;
            tag = NMSUtils.class_ItemStack_tagField.get(mcItemStack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    protected Object getNMSCopy(ItemStack stack) {
        Object nms = null;
        try {
            nms = NMSUtils.class_CraftItemStack_copyMethod.invoke(null, stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return nms;
    }

    @Override
    public ItemStack getCopy(ItemStack stack) {
        if (stack == null) return null;
        if (NMSUtils.class_CraftItemStack_mirrorMethod == null) return stack;

        try {
            Object craft = getNMSCopy(stack);
            stack = (ItemStack) NMSUtils.class_CraftItemStack_mirrorMethod.invoke(null, craft);
        } catch (Throwable ex) {
            stack = null;
        }

        return stack;
    }

    @Override
    public ItemStack makeReal(ItemStack stack) {
        if (stack == null) return null;
        Object nmsStack = getHandle(stack);
        if (nmsStack == null) {
            stack = getCopy(stack);
            nmsStack = getHandle(stack);
        }
        if (nmsStack == null) {
            return null;
        }
        try {
            Object tag = NMSUtils.class_ItemStack_tagField.get(nmsStack);
            if (tag == null) {
                NMSUtils.class_ItemStack_tagField.set(nmsStack, NMSUtils.class_NBTTagCompound_constructor.newInstance());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return stack;
    }

    @Override
    public boolean isUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return false;
        Boolean unbreakableFlag = null;
        try {
            Object tagObject = getTag(stack);
            if (tagObject == null) return false;
            unbreakableFlag = platform.getNBTUtils().getOptionalBoolean(tagObject, "Unbreakable");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return unbreakableFlag != null && unbreakableFlag;
    }

    @Override
    public void makeUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return;

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getOrCreateTag(craft);
            if (tagObject == null) return;

            Object unbreakableFlag = null;
            unbreakableFlag = NMSUtils.class_NBTTagByte_constructor.newInstance((byte) 1);
            NMSUtils.class_NBTTagCompound_setMethod.invoke(tagObject, "Unbreakable", unbreakableFlag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeUnbreakable(ItemStack stack) {
        platform.getNBTUtils().removeMeta(stack, "Unbreakable");
    }

    @Override
    public void hideFlags(ItemStack stack, int flags) {
        if (isEmpty(stack)) return;

        try {
            Object craft = getHandle(stack);
            if (craft == null) return;
            Object tagObject = getOrCreateTag(craft);
            if (tagObject == null) return;

            Object hideFlag = null;
            hideFlag = NMSUtils.class_NBTTagInt_constructor.newInstance(flags);
            NMSUtils.class_NBTTagCompound_setMethod.invoke(tagObject, "HideFlags", hideFlag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isEmpty(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return true;
        if (NMSUtils.class_ItemStack_isEmptyMethod == null) return false;
        try {
            Object handle = getHandle(itemStack);
            if (handle == null) return false;
            return (Boolean) NMSUtils.class_ItemStack_isEmptyMethod.invoke(handle);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    protected Object getTagString(String value) {
        try {
            return NMSUtils.class_NBTTagString_consructor.newInstance(value);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;
    }

    @Override
    public Object setStringList(Object nbtBase, String tag, Collection<String> values) {
        if (nbtBase == null) return null;
        Object listMeta = null;
        try {
            listMeta = NMSUtils.class_NBTTagList_constructor.newInstance();

            for (String value : values) {
                Object nbtString = getTagString(value);
                platform.getNBTUtils().addToList(listMeta, nbtString);
            }

            NMSUtils.class_NBTTagCompound_setMethod.invoke(nbtBase, tag, listMeta);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
        return listMeta;
    }

    @Override
    public List<String> getStringList(Object nbtBase, String key) {
        List<String> list = new ArrayList<>();
        try {
            Object listTag = NMSUtils.class_NBTTagCompound_getListMethod.invoke(nbtBase, key, CompatibilityConstants.NBT_TYPE_STRING);
            if (listTag != null) {
                int size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(listTag);
                for (int i = 0; i < size; i++) {
                    Object item = NMSUtils.class_NBTTagList_getStringMethod.invoke(listTag, i);
                    if (item != null && item instanceof String) {
                        list.add((String)item);
                    }
                }
            }
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading list from tag", ex);
        }
        return list;
    }

    @Override
    public ItemStack getItem(Object itemTag) {
        if (itemTag == null) return null;
        ItemStack item = null;
        try {
            Object nmsStack = null;
            if (NMSUtils.class_ItemStack_consructor != null) {
                nmsStack = NMSUtils.class_ItemStack_consructor.newInstance(itemTag);
            } else {
                nmsStack = NMSUtils.class_ItemStack_createStackMethod.invoke(null, itemTag);
            }
            item = (ItemStack) NMSUtils.class_CraftItemStack_mirrorMethod.invoke(null, nmsStack);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    @Override
    public ItemStack[] getItems(Object rootTag, String tagName) {
        try {
            Object itemList = NMSUtils.class_NBTTagCompound_getListMethod.invoke(rootTag, tagName, CompatibilityConstants.NBT_TYPE_COMPOUND);
            Integer size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(itemList);
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                try {
                    Object itemData = NMSUtils.class_NBTTagList_getMethod.invoke(itemList, i);
                    if (itemData != null) {
                        items[i] = getItem(itemData);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return items;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public String getItemModel(ItemStack itemStack) {
        return "";
    }

    @Override
    public void setItemModel(ItemStack itemStack, String model) {

    }

    @Override
    public Object getEquippable(ItemStack itemStack) {
        return null;
    }

    @Override
    public void setEquippable(ItemStack itemStack, Object equippable) {

    }

    @Override
    public void removeCustomData(ItemStack itemStack) {

    }

    @Override
    public void loadMeta(MageController controller, ItemMeta itemMeta, ConfigurationSection configuration) {
    }

    @Override
    public void saveMeta(MageController controller, ItemMeta itemMeta, ConfigurationSection configuration) {
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta)itemMeta;
            List<PotionEffect> effects = potion.getCustomEffects();
            ConfigurationSection potionSection = configuration.createSection("potion_effects");
            for (PotionEffect effect : effects) {
                PotionEffectType effectType = effect.getType();
                String effectParameters = effect.getDuration() + "," + effect.getAmplifier();
                potionSection.set(effectType.getName(), effectParameters);
            }
            if (potionSection.getKeys(false).isEmpty()) {
                configuration.set("potion_effects", null);
            }
            if (potion.hasColor()) {
                ConfigurationSection colorSection = configuration.createSection("color");
                Color color = potion.getColor();
                colorSection.set("red", color.getRed());
                colorSection.set("green", color.getGreen());
                colorSection.set("blue", color.getBlue());
                potion.setColor(null);
            }

            potion.clearCustomEffects();
        }

        final Map<Enchantment, Integer> enchantments = itemMeta.getEnchants();
        if (!enchantments.isEmpty()) {
            // Prefer to save as list if all level one
            boolean simple = true;
            for (Integer level : enchantments.values()) {
                if (level > 1) {
                    simple = false;
                    break;
                }
            }
            if (simple) {
                List<String> enchantIds = new ArrayList<>();
                for (Enchantment enchantment : enchantments.keySet()) {
                    enchantIds.add(enchantment.getKey().toString());
                }
                configuration.set("enchantments", enchantIds);
            } else {
                ConfigurationSection enchantSection = configuration.createSection("enchantments");
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    enchantSection.set(entry.getKey().getKey().toString(), entry.getValue());
                    itemMeta.removeEnchant(entry.getKey());
                }
            }
        }

        if (itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta)itemMeta;
            SkinUtils skinUtils = platform.getSkinUtils();
            PlayerProfile playerProfile = skinUtils.getPlayerProfile(skullMeta);
            if (playerProfile != null) {
                // Don't save the full profile here
                playerProfile.setSaveProfile(false);
                ConfigurationSection playerConfig = configuration.createSection("player");
                playerProfile.save(playerConfig);
            }
        }


        Multimap<Attribute, AttributeModifier> attributeModifiers = itemMeta.getAttributeModifiers();
        if (attributeModifiers != null && !attributeModifiers.isEmpty()) {
            List<ConfigurationSection> modifierList = new ArrayList<>();
            for (Map.Entry<Attribute, AttributeModifier> entry : attributeModifiers.entries()) {
                Attribute attribute = entry.getKey();
                ConfigurationSection attributeModifier = new MemoryConfiguration();
                attributeModifier.set("attribute", attribute.getKey().toString());
                AttributeModifier modifier = entry.getValue();
                attributeModifier.set("amount", modifier.getAmount());
                attributeModifier.set("slot", modifier.getSlot().toString());
                attributeModifier.set("operation", modifier.getOperation().toString());
                attributeModifier.set("uuid", modifier.getUniqueId().toString());
                attributeModifier.set("name", modifier.getName());
                modifierList.add(attributeModifier);
                itemMeta.removeAttributeModifier(attribute, modifier);
            }
            configuration.set("attributes", modifierList);
        }

        // TODO: Make this work
        if (itemMeta instanceof BlockStateMeta) {
            BlockStateMeta blockStateMeta = (BlockStateMeta)itemMeta;
            if (blockStateMeta.hasBlockState()) {
                BlockState blockState = blockStateMeta.getBlockState();
                configuration.set("block", blockState);
                // Replace with default block state to make it as empty as possible
                // blockStateMeta.setBlockState(Bukkit.createBlockData(itemStack.getType()).createBlockState());
            }
        }
    }
}

