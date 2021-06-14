package com.elmakers.mine.bukkit.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.ItemUtils;

public class Cost implements com.elmakers.mine.bukkit.api.item.Cost {
    protected Currency currency;
    protected ItemStack item;
    protected boolean itemWildcard;
    protected double amount;
    protected String materialSetKey;

    public Cost(MageController controller, String key, double cost)
    {
        this.amount = cost;
        this.setType(controller, key);
    }

    protected void setType(MageController controller, String key) {
        currency = controller.getCurrency(key);
        if (currency == null) {
            if (key.endsWith(":*")) {
                key = key.substring(0, key.length() - 2);
                itemWildcard = true;
            } else {
                itemWildcard = false;
            }
            this.item = controller.createItem(key, true);
            if (this.item == null) {
                materialSetKey = key;
                MaterialSet materialSet = getMaterialSet(controller);
                if (materialSet == null) {
                    materialSetKey = null;
                    controller.getLogger().warning("Invalid cost type: " + key);
                }
            }
        }
    }

    public Cost(Cost copy) {
        this.item = copy.item;
        this.itemWildcard = copy.itemWildcard;
        this.amount = copy.amount;
        this.currency = copy.currency;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty(null);
    }

    @Override
    public boolean isEmpty(CostReducer reducer) {
        if (item == null && currency == null) {
            return true;
        }
        return getAmount(reducer) == 0;
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, CostReducer reducer) {
        if (item != null) {
            return isConsumeFree(reducer) || isCostFree(reducer) || mage.hasItem(getItemStack(reducer), itemWildcard);
        }
        if (materialSetKey != null) {
            if (isConsumeFree(reducer) || isCostFree(reducer)) {
                return true;
            }
            MaterialSet set = getMaterialSet(mage.getController());
            for (Material material : set.getMaterials()) {
                if (mage.hasItem(getItemStackFromMaterial(material, reducer), itemWildcard)) {
                    return true;
                }
            }
            return false;
        }
        if (currency != null) {
            return currency.has(mage, caster, getAmount(reducer));
        }
        return false;
    }

    @Override
    public boolean has(Mage mage, Wand wand) {
        return has(mage, (CasterProperties)wand, null);
    }

    @Override
    public boolean has(Mage mage, Wand wand, CostReducer reducer) {
        return has(mage, (CasterProperties)wand, reducer);
    }

    @Override
    public boolean has(Mage mage) {
        return has(mage, mage.getActiveWand(), null);
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, CostReducer reducer) {
        if (item != null && !isConsumeFree(reducer) && !isCostFree(reducer)) {
            ItemStack itemStack = getItemStack(reducer);
            mage.removeItem(itemStack, itemWildcard);
        }
        if (materialSetKey != null && !isConsumeFree(reducer) && !isCostFree(reducer)) {
            MaterialSet set = getMaterialSet(mage.getController());
            for (Material material : set.getMaterials()) {
                ItemStack item = getItemStackFromMaterial(material, reducer);
                if (mage.hasItem(item, itemWildcard)) {
                    mage.removeItem(item, itemWildcard);
                    break;
                }
            }
        }
        if (currency != null) {
            currency.deduct(mage, caster, getAmount(reducer));
        }
    }

    @Override
    public void deduct(Mage mage, Wand wand) {
        deduct(mage, (CasterProperties)wand, null);
    }

    @Override
    public void deduct(Mage mage, Wand wand, CostReducer reducer) {
        deduct(mage, (CasterProperties)wand, reducer);
    }

    @Override
    public void deduct(Mage mage) {
        deduct(mage, mage.getActiveWand(), null);
    }

    public boolean give(Mage mage, CasterProperties caster, CostReducer reducer) {
        boolean result = false;
        if (item != null) {
            ItemStack itemStack = getItemStack(reducer);
            mage.giveItem(itemStack);
            result = true;
        }
        if (materialSetKey != null) {
            // This may convert costs from one to the other, but can't see a way around that.
            MaterialSet set = getMaterialSet(mage.getController());
            for (Material material : set.getMaterials()) {
                ItemStack item = getItemStackFromMaterial(material, reducer);
                mage.giveItem(item);
                break;
            }
        }
        if (currency != null) {
            result = currency.give(mage, caster, getAmount(reducer)) || result;
        }
        return result;
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster) {
        return give(mage, caster, null);
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        if (item != null) {
            double balance = 0;
            ItemStack itemStack = getItemStack();
            Inventory inventory = mage.getInventory();
            for (ItemStack item : inventory.getContents()) {
                if (item != null && mage.getController().itemsAreEqual(itemStack, item)) {
                    balance += item.getAmount();
                }
            }
            return balance;
        }

        if (materialSetKey != null) {
            MaterialSet set = getMaterialSet(mage.getController());
            double balance = 0;
            Inventory inventory = mage.getInventory();
            for (ItemStack item : inventory.getContents()) {
                if (item != null && set.testItem(item)) {
                    balance += item.getAmount();
                }
            }
            return balance;
        }

        if (currency != null) {
            return currency.getBalance(mage, caster);
        }

        return 0;
    }

    protected int getRoundedCost(double cost, CostReducer reducer) {
        return (int)Math.ceil(getReducedCost(cost, reducer));
    }

    protected double getReducedCost(double cost, CostReducer reducer)
    {
        double reducedAmount = cost;
        double reduction = reducer == null ? 0 : reducer.getCostReduction();
        if (reduction >= 1) {
            return 0;
        }
        if (reduction != 0) {
            reducedAmount = (1.0f - reduction) * reducedAmount;
        }
        if (reducer != null) {
            reducedAmount = reducedAmount * reducer.getCostScale();
        }
        return reducedAmount;
    }

    public int getRoundedAmount() {
        return (int)Math.ceil(amount);
    }

    public int getRoundedAmount(CostReducer reducer)
    {
        return getRoundedCost(amount, reducer);
    }

    public double getAmount(CostReducer reducer) {
        return getReducedCost(amount, reducer);
    }

    public double getAmount() {
        return amount;
    }

    public boolean isConsumeFree(CostReducer reducer)
    {
        return reducer != null && reducer.getConsumeReduction() >= 1;
    }

    public boolean isCostFree(CostReducer reducer)
    {
        return reducer != null && reducer.getCostReduction() >= 1;
    }

    @Override
    public String getDescription(Messages messages) {
        return getDescription(messages, null);
    }

    @Override
    public String getDescription(Messages messages, CostReducer reducer) {
        if (item != null) {
            return messages.describeItem(item);
        }
        if (currency != null) {
            return currency.getName(messages);
        }
        if (materialSetKey != null) {
            return materialSetKey;
        }
        return "";
    }

    @Override
    public String getFullDescription(Messages messages) {
        return getFullDescription(messages, null);
    }

    @Override
    public String getFullDescription(Messages messages, CostReducer reducer) {
        if (item != null) {
            return getRoundedAmount(reducer) + " " + messages.describeItem(item);
        }
        if (currency != null) {
            return currency.formatAmount(getAmount(reducer), messages);
        }
        if (materialSetKey != null) {
            return getRoundedAmount(reducer) + " " + materialSetKey;
        }
        return "";
    }

    @Override
    public boolean isItem() {
        return item != null;
    }

    @Override
    public ItemStack getItemStack()
    {
        ItemStack item = ItemUtils.getCopy(this.item);
        if (item != null) {
            item.setAmount((int)Math.max(1, Math.ceil(amount)));
        }
        return item;
    }

    protected ItemStack getItemStack(CostReducer reducer)
    {
        ItemStack item = ItemUtils.getCopy(this.item);
        if (item != null) {
            item.setAmount(Math.max(1, getRoundedCost(amount, reducer)));
        }
        return item;
    }

    protected ItemStack getItemStackFromMaterial(Material material, CostReducer reducer)
    {
        ItemStack item = new ItemStack(material);
        if (item != null) {
            item.setAmount(Math.max(1, getRoundedCost(amount, reducer)));
        }
        return item;
    }

    private boolean isXP() {
        return currency != null && currency.getKey().equals("xp");
    }

    private boolean isMana() {
        return currency != null && currency.getKey().equals("mana");
    }

    @Deprecated
    public int getXP(CostReducer reducer)
    {
        return isXP() ? getRoundedCost(amount, reducer) : 0;
    }

    public int getMana(CostReducer reducer)
    {
        return isMana() ? getRoundedCost(amount, reducer) : 0;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public void scale(double scale) {
        setAmount(amount * scale);
    }

    @Override
    public String toString() {
        return getType() + ":" + amount;
    }

    @Nullable
    public static Cost parseCost(MageController controller, String costString, String defaultType) {
        Cost cost = null;
        if (costString != null && !costString.isEmpty()) {
            String[] pieces = StringUtils.split(costString, ' ');
            int amount = 0;
            try {
                amount = Integer.parseInt(pieces[0]);
            } catch (Exception ex) {
                controller.getLogger().warning("Invalid cost string: " + costString);
            }
            String type = pieces.length > 1 ? pieces[1] : defaultType;
            cost = new Cost(controller, type, amount);
        }

        return cost;
    }

    @Nullable
    private String getFallbackType(String fallbackType) {
        if (currency != null && !currency.isValid()) {
            return fallbackType;
        }
        return null;
    }

    private boolean checkSupportedType(MageController controller, String fallbackType) {
        String newType = getFallbackType(fallbackType);
        if (newType == null) return false;
        convert(controller, newType);
        return true;
    }

    @Override
    public boolean checkSupported(MageController controller, String...fallbackTypes) {
        boolean modified = false;
        for (String fallbackType : fallbackTypes) {
            boolean check = checkSupportedType(controller, fallbackType);
            if (check) {
                modified = check;
            } else {
                break;
            }
        }

        return modified;
    }

    @Override
    public void convert(MageController controller, String newType) {
        double currentWorth = (currency != null) ? currency.getWorth() : 1;
        setType(controller, newType);
        double newWorth = (currency != null) ? currency.getWorth() : 1;
        if (newWorth > 0 && currentWorth > 0) {
            scale(currentWorth / newWorth);
        }
    }

    @Nullable
    public static List<Cost> parseCosts(ConfigurationSection node, MageController controller) {
        if (node == null) {
            return null;
        }
        List<Cost> costs = new ArrayList<>();
        Collection<String> costKeys = node.getKeys(false);
        for (String key : costKeys) {
            costs.add(new Cost(controller, key, node.getInt(key, 1)));
        }

        return costs;
    }

    @Nullable
    public MaterialSet getMaterialSet(MageController controller) {
        return materialSetKey == null ? null : controller.getMaterialSetManager().getMaterialSet(materialSetKey);
    }

    @Override
    @Nonnull
    public String getType() {
        if (currency != null) {
            return currency.getKey();
        }
        if (item != null) {
            return item.getType().name();
        }
        if (materialSetKey != null) {
            return materialSetKey;
        }
        return "Unknown";
    }

    public boolean isVaultCurrency() {
        if (currency != null && currency.getKey().equalsIgnoreCase("currency")) {
            return true;
        }
        return false;
    }
}
