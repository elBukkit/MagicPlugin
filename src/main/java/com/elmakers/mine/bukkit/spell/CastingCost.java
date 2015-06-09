package com.elmakers.mine.bukkit.spell;

import java.util.HashMap;
import java.util.Map;

import com.elmakers.mine.bukkit.api.magic.Messages;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.block.MaterialAndData;

public class CastingCost implements com.elmakers.mine.bukkit.api.spell.CastingCost
{
    protected MaterialAndData item;
    protected int amount;
    protected int xp;
    protected int mana;

    public CastingCost(String key, int cost)
    {
        if (key.toLowerCase().equals("xp")) {
            this.xp = cost;
        } else if (key.toLowerCase().equals("mana")) {
            this.mana = cost;
        } else {
            this.item = new MaterialAndData(key);
            this.amount = cost;
        }
    }

    public CastingCost(Material item, int amount)
    {
        this.item = new MaterialAndData(item, (byte)0);
        this.amount = amount;
    }

    public CastingCost(Material item, byte data, int amount)
    {
        this.item = new MaterialAndData(item, data);
        this.amount = amount;
    }

    public MaterialAndData getMaterial() {
        return item;
    }

    public Map<String, Object> export()
    {
        Map<String, Object> cost = new HashMap<String, Object>();
        cost.put("material", item.getName());
        cost.put("amount", amount);
        cost.put("xp", xp);
        cost.put("mana", mana);

        return cost;
    }

    public boolean has(Spell spell)
    {
        if (!(spell instanceof MageSpell)) return false;
        Mage mage = ((MageSpell)spell).getMage();
        Inventory inventory = mage.getInventory();
        int amount = getAmount(spell);
        boolean hasItem = item == null || amount <= 0 || inventory.containsAtLeast(item.getItemStack(amount), amount);
        boolean hasXp = xp <= 0 || mage.getExperience() >= getXP(spell);
        boolean hasMana = mana <= 0 || mage.getMana() >= getMana(spell);

        return hasItem && hasXp && hasMana;
    }

    public void use(Spell spell)
    {
        if (!(spell instanceof MageSpell)) return;
        Mage mage = ((MageSpell)spell).getMage();
        Inventory inventory = mage.getInventory();
        int amount = getAmount(spell);
        if (item != null && amount > 0) {
            ItemStack itemStack = getItemStack(spell);
            inventory.removeItem(itemStack);
        }
        int xp = getXP(spell);
        if (xp > 0) {
            mage.removeExperience(xp);
        }
        float mana = getMana(spell);
        if (mana > 0) {
            mage.removeMana(mana);
        }
    }

    protected ItemStack getItemStack()
    {
        return item.getItemStack(getAmount());
    }

    protected ItemStack getItemStack(CostReducer reducer)
    {
        return item.getItemStack(getAmount(reducer));
    }

    public int getAmount()
    {
        return amount;
    }

    public int getXP()
    {
        return xp;
    }

    public int getMana()
    {
        return mana;
    }

    public int getAmount(CostReducer reducer)
    {
        return getRoundedCost(amount, reducer);
    }

    public int getXP(CostReducer reducer)
    {
        return getRoundedCost(xp, reducer);
    }

    public float getMana(CostReducer reducer)
    {
        return getReducedCost(mana, reducer);
    }

    protected int getRoundedCost(int cost, CostReducer reducer) {
        return (int)Math.ceil(getReducedCost(cost, reducer));
    }

    protected float getReducedCost(int cost, CostReducer reducer)
    {
        float reducedAmount = cost;
        float reduction = reducer == null ? 0 : reducer.getCostReduction();
        if (reduction >= 1) {
            return 0;
        }
        if (reduction > 0) {
            reducedAmount = (1.0f - reduction) * reducedAmount;
        }
        reducedAmount = reducedAmount * reducer.getCostScale();
        return reducedAmount;
    }

    public boolean hasCosts(CostReducer reducer)
    {
        return (item != null && getAmount(reducer) > 0) || getXP(reducer) > 0 || getMana(reducer) > 0;
    }

    public String getDescription(Messages messages, CostReducer reducer)
    {
        if (item != null && getAmount() != 0) {
            return item.getName();
        }
        if (xp > 0) {
            return messages.get("costs.xp");
        }

        if (mana > 0) {
            return messages.get("costs.mana");
        }

        return "";
    }

    public String getFullDescription(Messages messages, CostReducer reducer)
    {
        if (item != null) {
            return getAmount(reducer) + " " + item.getName();
        }
        if (xp > 0) {
            return messages.get("costs.xp_amount").replace("$amount", ((Integer)getXP(reducer)).toString());
        }
        if (mana > 0) {
            return messages.get("costs.mana_amount").replace("$amount", ((Integer)(int)Math.ceil(getMana(reducer))).toString());
        }
        return "";
    }
}
