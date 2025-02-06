package com.elmakers.mine.bukkit.requirements;

import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;

class ItemRequirement extends RangedRequirement {
    public final String itemKey;
    private ItemStack itemStack;

    private ItemRequirement(ConfigurationSection configuration) {
        super(configuration);
        itemKey = configuration.getString("item", "");
    }

    private ItemRequirement(String[] configValues) {
        super(configValues[configValues.length - 1]);
        itemKey = configValues[0];

    }

    private ItemRequirement(String configValue) {
        this(StringUtils.split(configValue, " "));
    }

    @Nullable
    public static ItemRequirement parse(ConfigurationSection configuration, String key) {
        ConfigurationSection rangedConfig = ConfigurationUtils.getConfigurationSection(configuration, key);
        if (rangedConfig != null) {
            return new ItemRequirement(rangedConfig);
        }
        if (configuration.contains(key)) {
            return new ItemRequirement(configuration.getString(key));
        }
        return null;
    }

    public boolean check(Mage mage) {
        ItemStack itemStack = getItemStack(mage.getController());
        if (itemStack == null) {
            return false;
        }
        return check((double)mage.getItemCount(itemStack));
    }

    @Nullable
    public ItemStack getItemStack(MageController controller) {
        if (itemStack == null && itemKey != null) {
            ItemData itemData = controller.getOrCreateItem(itemKey);
            if (itemData == null) {
                controller.getLogger().warning("Invalid item in requirement: " + itemKey);
            } else {
                itemStack = itemData.getItemStack(1);
            }
        }
        return itemStack;
    }

    public String getKey() {
        return itemKey;
    }

    @Override
    public String toString() {
        return "[Item " + itemKey + "=" + value + " from (" + min + " to " + max + ")]";
    }
}
