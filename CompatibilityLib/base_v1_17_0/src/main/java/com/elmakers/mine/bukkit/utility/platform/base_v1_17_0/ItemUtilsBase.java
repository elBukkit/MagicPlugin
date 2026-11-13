package com.elmakers.mine.bukkit.utility.platform.base_v1_17_0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;
        if (!itemMeta.hasCustomModelData()) return 0;
        return itemMeta.getCustomModelData();
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
    public void removeUnbreakable(ItemStack stack) {
        platform.getNBTUtils().removeMeta(stack, "Unbreakable");
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
    public void removeDamage(ItemStack itemStack) {
        platform.getNBTUtils().removeMeta(itemStack, "Damage");
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
                EquipmentSlot slot = modifier.getSlot();
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

