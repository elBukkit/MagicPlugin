package com.elmakers.mine.bukkit.kit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class MagicKit {
    private final MagicController controller;
    private Collection<Requirement> requirements;
    private final String key;
    private final boolean isStarter;
    private final boolean isKeep;
    private final boolean isRemove;
    private final boolean isPartial;
    private final boolean isWelcomeWand;

    // Items can be mapped by slot, or not, or a mix of both
    private Map<Integer, ItemData> slotItems;
    private Collection<ItemData> items;

    public MagicKit(MagicController controller, String key, ConfigurationSection configuration) {
        this.controller = controller;
        this.key = key;
        isStarter = configuration.getBoolean("starter");
        isKeep = configuration.getBoolean("keep");
        isRemove = configuration.getBoolean("remove");
        isPartial = configuration.getBoolean("partial");
        isWelcomeWand = configuration.getBoolean("welcome_wand");
        requirements = ConfigurationUtils.getRequirements(configuration);
        List<? extends Object> itemList = configuration.getList("items");
        for (Object itemObject : itemList) {
            if (itemObject instanceof String) {
                addItemFromString((String)itemObject);
            } else if (itemObject instanceof ConfigurationSection || itemObject instanceof Map) {
                ConfigurationSection itemConfig;
                if (itemObject instanceof Map) {
                    itemConfig = ConfigurationUtils.toConfigurationSection(configuration, (Map<?,?>)itemObject);
                } else {
                    itemConfig = (ConfigurationSection)itemObject;
                }
                String itemKey = itemConfig.getString("item");
                if (itemKey == null || itemKey.isEmpty()) {
                    controller.getLogger().warning("Skipping empty item in kit " + key);
                    continue;
                }
                Integer slot = null;
                if (itemConfig.contains("slot")) {
                    slot = itemConfig.getInt("slot");
                }
                addItemFromString(itemKey, slot);
            }
        }
    }

    private void addItemFromString(String itemKey) {
        addItemFromString(itemKey, null);
    }

    private void addItemFromString(String itemKey, Integer slot) {
        ItemData item = controller.getOrCreateItem(itemKey);
        if (item == null) {
            controller.getLogger().warning("Invalid item key in key " + key + ": " + itemKey);
        } else {
            if (slot == null) {
                if (items == null) {
                    items = new ArrayList<>();
                }
                items.add(item);
            } else {
                if (slotItems == null) {
                    slotItems = new HashMap<>();
                }
                slotItems.put(slot, item);
            }
        }
    }

    public boolean isStarter() {
        return isStarter;
    }

    public boolean isKeep() {
        return isKeep;
    }

    public boolean isRemove() {
        return isRemove;
    }

    public boolean isAllowed(Mage mage) {
        if (requirements != null && controller.checkRequirements(mage.getContext(), requirements) != null) {
            return false;
        }
        if (isWelcomeWand && mage.hasGivenWelcomeWand()) {
            return false;
        }
        return true;
    }

    public void checkGive(Mage mage) {
        MageKit kit = mage.getKit(key);
        if (kit != null && !isPartial) return;
        give(mage, true, true, kit);
    }

    public void giveMissing(Mage mage) {
        give(mage, true, false, null);
    }

    public void give(Mage mage) {
        give(mage, false, false, null);
    }

    private void give(Mage mage, boolean onlyIfMissing, boolean useKit, MageKit givenKit) {
        if (!isAllowed(mage)) {
            return;
        }
        Collection<ItemStack> giveItems = new ArrayList<>();
        if (slotItems != null) {
            for (Map.Entry<Integer, ItemData> slotItem : slotItems.entrySet()) {
                int slot = slotItem.getKey();
                ItemData itemData = slotItem.getValue();
                ItemStack itemStack = itemData.getItemStack();
                if (CompatibilityUtils.isEmpty(itemStack)) continue;
                if (onlyIfMissing) {
                    itemStack = checkGiveIfMissing(itemData.getBaseKey(), itemStack, mage, useKit, givenKit);
                    if (itemStack == null) continue;
                }
                ItemStack existingSlot = mage.getItem(slot);
                mage.gaveItemFromKit(key, itemData.getBaseKey(), itemStack.getAmount());
                if (CompatibilityUtils.isEmpty(existingSlot)) {
                    mage.setItem(slot, itemStack);
                } else {
                    if (giveItems == null) {
                        giveItems = new ArrayList<>();
                    }
                    giveItems.add(itemStack);
                }
            }
        }
        if (giveItems != null) {
            for (ItemStack giveItem : giveItems) {
                mage.giveItem(giveItem);
            }
        }
        if (items != null) {
            for (ItemData itemData : items) {
                ItemStack itemStack = itemData.getItemStack();
                if (CompatibilityUtils.isEmpty(itemStack)) continue;
                if (onlyIfMissing) {
                    itemStack = checkGiveIfMissing(itemData.getBaseKey(), itemStack, mage, useKit, givenKit);
                    if (itemStack == null) continue;
                }
                mage.gaveItemFromKit(key, itemData.getBaseKey(), itemStack.getAmount());
                mage.giveItem(itemStack);
            }
        }
    }

    @Nullable
    private ItemStack checkGiveIfMissing(String itemKey, ItemStack itemStack, Mage mage, boolean useKit, MageKit givenKit) {
        if (useKit) {
            int givenAmount = givenKit == null ? 0 : givenKit.getGivenAmount(itemKey);
            if (givenAmount >= itemStack.getAmount()) {
                return null;
            }
            if (givenAmount > 0) {
                if (!isPartial) {
                    return null;
                }
                itemStack.setAmount(itemStack.getAmount() - givenAmount);
            }
        } else {
            if (mage.hasItem(itemStack)) {
                return null;
            }
        }
        return itemStack;
    }
}
