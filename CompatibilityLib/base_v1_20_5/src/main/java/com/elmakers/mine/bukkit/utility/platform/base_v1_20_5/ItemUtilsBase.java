package com.elmakers.mine.bukkit.utility.platform.base_v1_20_5;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.PlayerProfile;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.google.common.collect.Multimap;

public abstract class ItemUtilsBase implements ItemUtils {
    protected final Platform platform;

    protected ItemUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public void addGlow(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        stack.setItemMeta(meta);
    }

    @Override
    public void removeGlow(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setEnchantmentGlintOverride(null);
        stack.setItemMeta(meta);
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
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;
        if (!itemMeta.hasCustomModelData()) return 0;
        return itemMeta.getCustomModelData();
    }

    @Nullable
    @Override
    public String getItemModel(ItemStack item) {
        return null;
    }

    @Override
    public void setItemModel(ItemStack itemStack, String model) {

    }

    @Override
    public void setCustomModelData(ItemStack itemStack, int customModelData) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.setCustomModelData(customModelData);
        itemStack.setItemMeta(itemMeta);
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

        return stack;
    }

    @Override
    public boolean isUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta.isUnbreakable();
    }

    @Override
    public void makeUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
    }

    @Override
    public void removeUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(false);
        stack.setItemMeta(meta);
    }

    @Override
    public void hideFlags(ItemStack stack, int flags) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        ItemFlag[] flagArray = ItemFlag.values();
        for (int ordinal = 0; ordinal < flagArray.length; ordinal++) {
            ItemFlag flag = flagArray[ordinal];
            if ((flags & 1) == 1) {
                meta.addItemFlags(flag);
            } else {
                meta.removeItemFlags(flag);
            }
            flags >>= 1;
        }
        stack.setItemMeta(meta);
    }

    public Object getEquippable(ItemStack itemStack) {
        return null;
    }

    public void setEquippable(ItemStack itemStack, Object equippable) {
    }

    @Override
    public void loadMeta(MageController controller, ItemMeta itemMeta, ConfigurationSection configuration) {
        String potionKey = configuration.getString("potion");
        if (potionKey != null && !potionKey.isEmpty()) {
            Registry<PotionType> registry = controller.getPlugin().getServer().getRegistry(PotionType.class);
            if (itemMeta instanceof PotionMeta && registry != null) {
                NamespacedKey key = NamespacedKey.fromString(potionKey);
                PotionType effectType = registry.get(key);
                if (effectType != null) {
                    ((PotionMeta)itemMeta).setBasePotionType(effectType);
                }
            }
        }
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
                potionSection.set(effectType.getKey().toString(), effectParameters);
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
            if (potion.hasBasePotionType()) {
                PotionType baseType = potion.getBasePotionType();
                configuration.set("potion", baseType.getKey().toString());
                potion.setBasePotionType(null);
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
                }
            }
            itemMeta.removeEnchantments();
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
            skullMeta.setOwnerProfile(null);
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
                EquipmentSlotGroup slot = modifier.getSlotGroup();
                if (slot != null) {
                    attributeModifier.set("slot", slot.toString());
                }
                AttributeModifier.Operation operation = modifier.getOperation();
                if (operation != null) {
                    attributeModifier.set("operation", operation.toString());
                }
                UUID uniqueId = modifier.getUniqueId();
                if (uniqueId != null) {
                    attributeModifier.set("uuid", uniqueId.toString());
                }
                attributeModifier.set("name", modifier.getName());
                modifierList.add(attributeModifier);
                itemMeta.removeAttributeModifier(attribute, modifier);
            }
            configuration.set("attributes", modifierList);
        }
    }
}
