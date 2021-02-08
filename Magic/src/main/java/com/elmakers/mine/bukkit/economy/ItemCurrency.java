package com.elmakers.mine.bukkit.economy;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;

public class ItemCurrency extends BaseMagicCurrency {
    private final ItemStack item;
    private final String singularName;

    public ItemCurrency(MageController controller, ItemStack item, double worth, String name, String pluralName) {
        super("item", worth, pluralName, pluralName);
        this.item = item;
        this.singularName = name;
    }

    private ItemStack getItemStack(double amount) {
        item.setAmount(getRoundedAmount(amount));
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
        return mage.giveItem(getItemStack(amount).clone());
    }

    @Override
    public String formatAmount(double amount, Messages messages) {
        int rounded = getRoundedAmount(amount);
        String amountString = intFormatter.format(rounded);
        String label = rounded == 1 ? singularName : name;
        return amountString + " " + label;
    }
}
