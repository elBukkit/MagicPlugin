package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.Objects;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

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
}

