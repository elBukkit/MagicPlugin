package com.elmakers.mine.bukkit.economy;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.block.MaterialAndData;

public class ItemCurrency extends BaseMagicCurrency {
    private final ItemStack item;

    public ItemCurrency(MageController controller, ConfigurationSection configuration) {
        super(controller, "item", configuration);

        String itemKey = configuration.getString("item");
        if (itemKey == null || itemKey.isEmpty()) {
            item = null;
        } else {
            MaterialAndData material = new MaterialAndData(configuration.getString("item"));
            if (material.isValid()) {
                item = material.getItemStack(1);
            } else {
                item = null;
            }
        }
    }

    private ItemStack getItemStack(double amount) {
        item.setAmount(getRoundedAmount(amount));
        return item;
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        double balance = 0;
        Inventory inventory = mage.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && mage.getController().itemsAreEqual(this.item, item)) {
                balance += item.getAmount();
            }
        }
        return balance;
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        return mage.hasItem(getItemStack(amount));
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        mage.removeItem(getItemStack(amount));
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        return mage.giveItem(getItemStack(amount).clone(), true, false);
    }

    @Override
    public String formatAmount(double amount, Messages messages) {
        int rounded = getRoundedAmount(amount);
        String amountString = intFormatter.format(rounded);
        String label = rounded == 1 ? singularName : name;
        return amountString + " " + label;
    }

    @Override
    public boolean isValid() {
        return item != null;
    }
}
