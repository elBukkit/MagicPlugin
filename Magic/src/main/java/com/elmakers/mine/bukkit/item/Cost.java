package com.elmakers.mine.bukkit.item;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.CostReducer;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.integration.VaultController;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;

public class Cost implements com.elmakers.mine.bukkit.api.item.Cost {
    public enum Type {
        ITEM,
        XP,
        SP,
        MANA,
        CURRENCY,
        HEALTH,
        HUNGER,
        LEVELS,
        CUSTOM
    }

    protected ItemStack item;
    protected boolean itemWildcard;
    protected double amount;
    protected Type type;
    protected String customType;

    public Cost(MageController controller, String key, double cost)
    {
        this.amount = cost;
        this.setType(controller, key);
    }

    protected void setType(MageController controller, String key) {
        if (key.toLowerCase().equals("xp")) {
            this.type = Type.XP;
        } else if (key.toLowerCase().equals("sp")) {
            this.type = Type.SP;
        } else if (key.toLowerCase().equals("mana")) {
            this.type = Type.MANA;
        } else if (key.toLowerCase().equals("currency")) {
            this.type = Type.CURRENCY;
        } else if (key.toLowerCase().equals("health")) {
            this.type = Type.HEALTH;
        } else if (key.toLowerCase().equals("hunger")) {
            this.type = Type.HUNGER;
        } else if (key.toLowerCase().equals("levels")) {
            this.type = Type.LEVELS;
        } else if (key.toLowerCase().equals("item")) {
            this.type = Type.ITEM;
            itemWildcard = false;
            this.item = controller.getWorthItem().clone();
            if (this.item != null) {
                this.item.setAmount((int)Math.ceil(amount));
            }
        } else {
            Set<String> customCurrencies = controller.getCustomCurrencies();
            if (customCurrencies.contains(key)) {
                customType = key;
                type = Type.CUSTOM;
            } else {
                if (key.endsWith(":*")) {
                    key = key.substring(0,key.length() - 2);
                    itemWildcard = true;
                } else {
                    itemWildcard = false;
                }
                this.item = controller.createItem(key, true);
                if (this.item != null) {
                    this.item.setAmount((int)Math.ceil(amount));
                }
                this.type = Type.ITEM;
            }
        }
    }

    public Cost(Cost copy) {
        this.item = copy.item;
        this.itemWildcard = copy.itemWildcard;
        this.amount = copy.amount;
        this.type = copy.type;
        this.customType = copy.customType;
    }

    @Override
    public boolean isEmpty(CostReducer reducer) {
        switch (this.type) {
            case ITEM:
                return item == null || getReducedCost(item.getAmount(), reducer) == 0;
            case LEVELS:
            case XP:
            case SP:
                return getRoundedAmount(reducer) == 0;
            case MANA:
                return getMana(reducer) == 0;
            case CURRENCY:
            case CUSTOM:
            default:
                return getAmount(reducer) == 0;
        }
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, CostReducer reducer) {

        Player player = mage.getPlayer();
        switch (type) {
            case ITEM:
                return isConsumeFree(reducer) || mage.hasItem(getItemStack(reducer), itemWildcard);
            case XP:
                return mage.getExperience() >= getRoundedAmount(reducer);
            case LEVELS:
                return player != null && player.getLevel() >= getRoundedAmount(reducer);
            case MANA:
                return caster == null ? mage.getMana() >= getMana(reducer) : caster.getMana() >= getMana(reducer);
            case CURRENCY:
                VaultController vault = VaultController.getInstance();
                return vault != null && vault.has(mage.getPlayer(), getAmount(reducer));
            case SP:
                return mage.getSkillPoints() >= getRoundedAmount(reducer);
            case HEALTH:
                LivingEntity living = mage.getLivingEntity();
                return living != null && living.getHealth() >= getAmount(reducer);
            case HUNGER:
                return player != null && player.getFoodLevel() >= getAmount(reducer);
            case CUSTOM:
                return mage.getCurrency(customType) >= getAmount(reducer);
        }

        return false;
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
        Player player = mage.getPlayer();
        switch (type) {
            case ITEM:
                if (!isConsumeFree(reducer)) {
                    ItemStack itemStack = getItemStack(reducer);
                    mage.removeItem(itemStack, itemWildcard);
                }
                break;
            case XP:
                mage.removeExperience(getRoundedAmount(reducer));
                break;
            case LEVELS:
                if (player != null) {
                    int newLevel = Math.max(0, player.getLevel() - getRoundedAmount(reducer));
                    player.setLevel(newLevel);
                }
                break;
            case MANA:
                if (caster != null) {
                    caster.removeMana(getMana(reducer));
                } else {
                    mage.removeMana(getMana(reducer));
                }
                break;
            case CURRENCY:
                VaultController vault = VaultController.getInstance();
                if (vault != null) {
                    vault.withdrawPlayer(mage.getPlayer(), getAmount(reducer));
                }
                break;
            case SP:
                mage.addSkillPoints(-getRoundedAmount(reducer));
                break;
            case HEALTH:
                LivingEntity living = mage.getLivingEntity();
                if (living != null) {
                    living.setHealth(Math.max(0, living.getHealth() - getAmount(reducer)));
                }
                break;
            case HUNGER:
                if (player != null) {
                    player.setFoodLevel(Math.max(0, player.getFoodLevel() - getRoundedAmount(reducer)));
                }
                break;
            case CUSTOM:
                mage.removeCurrency(customType, getAmount(reducer));
                break;
        }
    }

    @Override
    public void deduct(Mage mage, Wand wand, CostReducer reducer) {
        deduct(mage, (CasterProperties)wand, reducer);
    }

    @Override
    public void deduct(Mage mage) {
        deduct(mage, mage.getActiveWand(), null);
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster) {
        if (caster == null) {
            caster = mage.getActiveProperties();
        }
        boolean result = true;
        Player player = mage.getPlayer();
        switch (type) {
            case ITEM:
                ItemStack itemStack = getItemStack();
                mage.giveItem(itemStack);
                break;
            case XP:
                mage.giveExperience(getRoundedAmount());
                break;
            case LEVELS:
                if (player != null) {
                    int newLevel = player.getLevel() + getRoundedAmount();
                    player.setLevel(newLevel);
                }
                break;
            case MANA:
                if (caster.getMana() >= caster.getManaMax()) {
                    result = false;
                } else {
                    float newMana = (float)Math.min(caster.getManaMax(), caster.getMana() + amount);
                    caster.setMana(newMana);
                }
                break;
            case CURRENCY:
                VaultController vault = VaultController.getInstance();
                if (vault != null) {
                    vault.depositPlayer(mage.getPlayer(), amount);
                }
                break;
            case SP:
                if (mage.isAtMaxSkillPoints()) {
                    result = false;
                } else {
                    mage.addSkillPoints(getRoundedAmount());
                }
                break;
            case HEALTH:
                LivingEntity living = mage.getLivingEntity();
                if (living != null) {
                    double maxHealth = CompatibilityUtils.getMaxHealth(living);
                    if (living.getHealth() >= maxHealth) {
                        result = false;
                    } else {
                        living.setHealth(Math.min(maxHealth, living.getHealth() + amount));
                    }
                }
                break;
            case HUNGER:
                if (player != null) {
                    if (player.getFoodLevel() >= 10) {
                        result = false;
                    } else {
                        player.setFoodLevel(Math.min(10, player.getFoodLevel() + getRoundedAmount()));
                    }
                }
                break;
            case CUSTOM:
                if (mage.isAtMaxCurrency(customType)) {
                    result = false;
                } else {
                    mage.addCurrency(customType, amount);
                }
                break;
            default:
                result = false;
        }
        return result;
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        if (caster == null) {
            caster = mage.getActiveProperties();
        }
        double balance = 0;
        Player player = mage.getPlayer();
        switch (type) {
            case ITEM:
                ItemStack itemStack = getItemStack();
                Inventory inventory = mage.getInventory();
                for (ItemStack item : inventory.getContents()) {
                    if (item.equals(itemStack)) {
                        balance += item.getAmount();
                    }
                }
                break;
            case XP:
                balance = mage.getExperience();
                break;
            case LEVELS:
                balance = mage.getLevel();
                break;
            case MANA:
                balance = caster.getMana();
                break;
            case CURRENCY:
                VaultController vault = VaultController.getInstance();
                if (vault != null) {
                    balance = vault.getBalance(mage.getPlayer());
                }
                break;
            case SP:
                balance = mage.getSkillPoints();
                break;
            case HEALTH:
                LivingEntity living = mage.getLivingEntity();
                if (living != null) {
                    balance = living.getHealth();
                }
                break;
            case HUNGER:
                if (player != null) {
                    balance = player.getFoodLevel();
                }
                break;
            case CUSTOM:
                balance = mage.getCurrency(customType);
                break;
        }
        return balance;
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

    @Override
    public String getDescription(Messages messages) {
        return getDescription(messages, null);
    }

    @Override
    public String getDescription(Messages messages, CostReducer reducer) {
        if (amount == 0) return "";

        switch (type) {
            case ITEM:
                if (item != null && reducer.getConsumeReduction() < 1) {
                    return messages.describeItem(item);
                }
                break;
            case XP:
                return messages.get("costs.xp");
            case LEVELS:
                return messages.get("costs.levels");
            case SP:
                return messages.get("costs.sp");
            case MANA:
                return messages.get("costs.mana");
            case HUNGER:
                return messages.get("costs.hunger");
            case HEALTH:
                return messages.get("costs.health");
            case CURRENCY:
                return messages.getCurrencyPlural();
            case CUSTOM:
                return messages.get("currency." + customType + ".name", customType);
        }
        return "";
    }

    @Override
    public String getFullDescription(Messages messages) {
        return getFullDescription(messages, null);
    }

    @Override
    public String getFullDescription(Messages messages, CostReducer reducer) {
        double amount = getAmount(reducer);
        if (amount == 0) return "";

        switch (type) {
            case ITEM:
                if (item != null && !isConsumeFree(reducer)) {
                    return getRoundedAmount(reducer) + " " + messages.describeItem(item);
                }
                break;
            case XP:
                return messages.get("costs.xp_amount").replace("$amount", Integer.toString(getRoundedAmount(reducer)));
            case LEVELS:
                return messages.get("costs.levels_amount").replace("$amount", Integer.toString(getRoundedAmount(reducer)));
            case SP:
                return messages.get("costs.sp_amount").replace("$amount", Integer.toString(getRoundedAmount(reducer)));
            case MANA:
                return messages.get("costs.mana_amount").replace("$amount", Integer.toString(getMana(reducer)));
            case HEALTH:
                return messages.get("costs.health_amount").replace("$amount", Integer.toString(getRoundedAmount(reducer)));
            case HUNGER:
                return messages.get("costs.hunger_amount").replace("$amount", Integer.toString(getRoundedAmount(reducer)));
            case CURRENCY:
                return messages.get("costs.currency_amount").replace("$amount", Integer.toString(getRoundedAmount(reducer)));
            case CUSTOM:
                return messages.get("currency." + customType + ".amount", customType).replace("$amount", Integer.toString(getRoundedAmount(reducer)));
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

    @Deprecated
    public int getXP(CostReducer reducer)
    {
        return type == Type.XP ? getRoundedCost(amount, reducer) : 0;
    }

    public int getMana(CostReducer reducer)
    {
        return  type == Type.MANA ? getRoundedCost(amount, reducer) : 0;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        if (type == Type.ITEM && item != null) {
            item.setAmount((int)Math.max(1, Math.ceil(amount)));
        }
    }

    @Override
    public void scale(double scale) {
        setAmount(amount * scale);
    }

    @Override
    public String toString() {
        return type + ":" + amount;
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

    public boolean checkSupported(MageController controller, String fallbackType) {
        switch (type) {
            case SP:
                if (!controller.isSPEnabled()) {
                    setType(controller, fallbackType);
                    return true;
                }
                return false;
            case CURRENCY:
                if (!controller.isVaultCurrencyEnabled()) {
                    setType(controller, fallbackType);
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    @Override
    public boolean checkSupported(MageController controller, String fallbackType, String secondaryFallbackType) {
        boolean modified = checkSupported(controller, fallbackType);
        if (modified) {
            checkSupported(controller, secondaryFallbackType);

            switch (type) {
                case XP:
                    scale(1.0 / controller.getWorthXP());
                    break;
                case SP:
                    scale(1.0 / controller.getWorthSkillPoints());
                    break;
                case ITEM:
                    scale(1.0 / controller.getWorthItemAmount());
                    break;
                default:
                    break;
            }
        }

        return modified;
    }
}
