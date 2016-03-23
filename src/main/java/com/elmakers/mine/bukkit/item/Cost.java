package com.elmakers.mine.bukkit.item;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import org.bukkit.inventory.ItemStack;

public class Cost implements com.elmakers.mine.bukkit.api.item.Cost {
    protected enum Type {
        ITEM,
        XP,
        SP,
        MANA,
        CURRENCY
    };
    
    protected ItemStack item;
    protected double amount;
    protected Type type;

    public Cost(MageController controller, String key, double cost)
    {
        this.amount = cost;
        if (key.toLowerCase().equals("xp")) {
            this.type = Type.XP;
        } if (key.toLowerCase().equals("sp")) {
            this.type = Type.SP;
        } else if (key.toLowerCase().equals("mana")) {
            this.type = Type.MANA;
        } else if (key.toLowerCase().equals("currency")) {
            this.type = Type.CURRENCY;
        } else {
            this.item = controller.createItem(key, true);
            if (this.item != null) {
                this.item.setAmount((int)Math.ceil(amount));
            }
            this.type = Type.ITEM;
        }
    }

    @Override
    public boolean isEmpty(CostReducer reducer) {
        switch (this.type) {
            case ITEM:
                return item == null || getReducedCost(item.getAmount(), reducer) == 0;
            case XP:
                return getXP(reducer) == 0;
            case MANA:
                return getMana(reducer) == 0;
            case CURRENCY: 
                return getCurrency(reducer) == 0;
            case SP:
                return getSkillPoints(reducer) == 0;
        }
        
        return true;
    }
    
    @Override
    public boolean has(Mage mage, Wand wand, CostReducer reducer) {
        switch (type) {
            case ITEM:
                return isConsumeFree(reducer) || mage.hasItem(getItemStack(reducer), true);
            case XP:
                return mage.getExperience() >= getXP(reducer);
            case MANA:
                return wand == null ? mage.getMana() >= getMana(reducer) : wand.getMana() >= getMana(reducer);
            case CURRENCY:
                VaultController vault = VaultController.getInstance();
                return vault.has(mage.getPlayer(), getCurrency(reducer));
            case SP:
                return mage.getSkillPoints() >= getSkillPoints(reducer);
        }
        
        return false;
    }

    @Override
    public boolean has(Mage mage) {
        return has(mage, mage.getActiveWand(), null);
    }

    @Override
    public void deduct(Mage mage, Wand wand, CostReducer reducer) {
        switch (type) {
            case ITEM:
                if (!isConsumeFree(reducer)) {
                    ItemStack itemStack = getItemStack(reducer);
                    mage.removeItem(itemStack, true);
                }
                break;
            case XP:
                mage.removeExperience(getXP(reducer));
                break;
            case MANA:
                if (wand != null) {
                    wand.removeMana(getMana(reducer));
                } else {
                    mage.removeMana(getMana(reducer));
                }
                break;
            case CURRENCY:
                VaultController vault = VaultController.getInstance();
                vault.withdrawPlayer(mage.getPlayer(), getCurrency(reducer));
                break;
            case SP:
                mage.addSkillPoints(-getSkillPoints(reducer));
                break;
        }
    }

    @Override
    public void deduct(Mage mage) {
        deduct(mage, mage.getActiveWand(), null);
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
        if (reduction > 0) {
            reducedAmount = (1.0f - reduction) * reducedAmount;
        }
        if (reducer != null) {
            reducedAmount = reducedAmount * reducer.getCostScale();
        }
        return reducedAmount;
    }
    
    public int getAmount(CostReducer reducer)
    {
        return getRoundedCost(amount, reducer);
    }

    public boolean isConsumeFree(CostReducer reducer)
    {
        return reducer != null && reducer.getConsumeReduction() >= 1;
    }
    
    @Override
    public String getDescription(Messages messages) {
        return getDescription(messages, null);
    }

    @Override
    public String getFullDescription(Messages messages)
    {
        return getFullDescription(messages, null);
    }

    public String getDescription(Messages messages, CostReducer reducer)
    {
        if (amount == 0) return "";
        
        switch (type) {
            case ITEM:
                if (item != null && reducer.getConsumeReduction() < 1) {
                    return messages.describeItem(item);
                }
                break;
            case XP:
                return messages.get("costs.xp");
            case SP:
                return messages.get("costs.sp");
            case MANA:
                return messages.get("costs.mana");
            case CURRENCY:
                return messages.getCurrencyPlural();
        }
        return "";
    }

    public String getFullDescription(Messages messages, CostReducer reducer)
    {
        if (getAmount(reducer) == 0) return "";

        switch (type) {
            case ITEM:
                if (item != null && !isConsumeFree(reducer)) {
                    return getAmount(reducer) + " " + messages.describeItem(item);
                }
                break;
            case XP:
                return messages.get("costs.xp_amount").replace("$amount", ((Integer)getXP(reducer)).toString());
            case SP:
                return messages.get("costs.sp_amount").replace("$amount", ((Integer)(int)Math.ceil(getSkillPoints(reducer))).toString());
            case MANA:
                return messages.get("costs.mana_amount").replace("$amount", ((Integer)(int)Math.ceil(getMana(reducer))).toString());
            case CURRENCY:
                return messages.get("costs.currency_amount").replace("$amount", ((Integer)(int)Math.ceil(getCurrency(reducer))).toString());
        }
        return "";
    }

    @Override
    public boolean isItem() {
        return type == Type.ITEM && item != null;
    }

    @Override
    public ItemStack getItemStack()
    {
        return CompatibilityUtils.getCopy(item);
    }

    protected ItemStack getItemStack(CostReducer reducer)
    {
        ItemStack item = getItemStack();
        if (item != null) {
            item.setAmount(getRoundedCost(item.getAmount(), reducer));
        }
        return item;
    }

    public int getXP(CostReducer reducer)
    {
        return type == Type.XP ? getRoundedCost(amount, reducer) : 0;
    }
    
    public int getSkillPoints(CostReducer reducer)
    {
        return type == Type.SP ? getRoundedCost(amount, reducer) : 0;
    }

    public double getCurrency(CostReducer reducer)
    {
        return type == Type.CURRENCY ? getReducedCost(amount, reducer) : 0;
    }

    public int getMana(CostReducer reducer)
    {
        return  type == Type.MANA ? getRoundedCost(amount, reducer) : 0;
    }
}
