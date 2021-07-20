package com.elmakers.mine.bukkit.wand;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.economy.Currency;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.MagicAttribute;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.spell.BaseSpell;

public class WandDisplayMode {
    public static WandDisplayMode NONE = new WandDisplayMode(DisplayType.NONE);
    public static WandDisplayMode COOLDOWN = new WandDisplayMode(DisplayType.COOLDOWN);
    public static WandDisplayMode MANA = new WandDisplayMode(DisplayType.MANA);
    public static WandDisplayMode SP = new WandDisplayMode(DisplayType.CURRENCY, "sp");

    private enum DisplayType { NONE, COOLDOWN, MANA, CURRENCY, ATTRIBUTE }

    private final DisplayType displayType;
    private final String currencyKey;
    private final String attributeKey;

    private WandDisplayMode(DisplayType displayType, String extraData) {
        this.displayType = displayType;
        switch (displayType) {
            case CURRENCY:
                currencyKey = extraData;
                attributeKey = null;
                break;
            case ATTRIBUTE:
                attributeKey = extraData;
                currencyKey = null;
                break;
            default:
                attributeKey = null;
                currencyKey = null;
                break;
        }
    }

    public WandDisplayMode(DisplayType displayType) {
        this(displayType, null);
    }

    public static WandDisplayMode getCurrency(String currencyKey) {
        return new WandDisplayMode(DisplayType.CURRENCY, currencyKey);
    }

    public static WandDisplayMode parse(MageController controller, ConfigurationSection config, String modeKey) {
        String displayMode = config.getString(modeKey);
        if (displayMode == null || displayMode.isEmpty()) {
            return null;
        }
        DisplayType displayType = DisplayType.valueOf(displayMode.toUpperCase());
        switch (displayType) {
            case COOLDOWN:
                return COOLDOWN;
            case MANA:
                return MANA;
            case NONE:
                return NONE;
            case CURRENCY:
                String currencyType = config.getString("currency");
                if (currencyType == null || currencyType.isEmpty()) {
                    throw new IllegalArgumentException("Currency type requires currency definition");
                }
                if (currencyType.equals("sp")) {
                    return SP;
                }
                if (controller.getCurrency(currencyType) == null) {
                    throw new IllegalArgumentException("Unknown currency in display mode: " + currencyType);
                }
                return new WandDisplayMode(DisplayType.CURRENCY, currencyType);
            case ATTRIBUTE:
                String attributeType = config.getString("attribute");
                if (attributeType == null || attributeType.isEmpty()) {
                    throw new IllegalArgumentException("Currency type requires attribute definition");
                }
                return new WandDisplayMode(DisplayType.ATTRIBUTE, attributeType);
            default:
                throw new IllegalArgumentException("Unknown display type: " + displayType);
        }
    }

    public double getProgress(Wand wand) {
        Mage mage = wand.getMage();
        double progress = 1;
        switch (displayType) {
            case COOLDOWN:
                Spell spell = wand.getActiveSpell();
                if (spell != null && spell instanceof BaseSpell) {
                    BaseSpell baseSpell = (BaseSpell)spell;
                    long timeToCast = baseSpell.getTimeToCast(mage);
                    long maxTimeToCast = baseSpell.getMaxTimeToCast(mage);
                    if (maxTimeToCast > 0) {
                        progress = (double)(maxTimeToCast - timeToCast) / maxTimeToCast;
                    }
                }
                break;
            case MANA:
                double maxMana = mage.getEffectiveManaMax();
                if (maxMana > 0 && !wand.isCostFree()) {
                    progress = mage.getMana() / maxMana;
                }
                break;
            case CURRENCY:
                Currency currency = mage.getController().getCurrency(currencyKey);
                progress = mage.getCurrency(currencyKey);
                if (currency != null && currency.hasMaxValue()) {
                    progress = progress / currency.getMaxValue();
                }
                break;
            case ATTRIBUTE:
                MagicAttribute attribute = mage.getController().getAttribute(attributeKey);
                progress = mage.getAttribute(attributeKey);
                Double maxValue = attribute != null ? attribute.getMax() : null;
                if (maxValue != null) {
                    progress = progress / maxValue;
                }
                break;
            case NONE:
                break;
        }
        return progress;
    }

    public double getValue(Wand wand) {
        Mage mage = wand.getMage();
        double value = 1;
        switch (displayType) {
            case COOLDOWN:
                Spell spell = wand.getActiveSpell();
                if (spell != null && spell instanceof BaseSpell) {
                    BaseSpell baseSpell = (BaseSpell)spell;
                    value = baseSpell.getTimeToCast(mage) / 1000;
                }
                break;
            case MANA:
                value = mage.getMana();
                break;
            case CURRENCY:
                value = mage.getCurrency(currencyKey);
                break;
            case ATTRIBUTE:
                value = mage.getAttribute(attributeKey);
                break;
        }
        return value;
    }

    public boolean isEnabled(Wand wand) {
        switch (displayType) {
            case COOLDOWN:
                return !wand.isCooldownFree();
            case MANA:
                return wand.usesMana();
            case CURRENCY:
                if (currencyKey.equals("sp")) {
                    return wand.usesSP();
                }
                return true;
            case ATTRIBUTE:
                return true;
            default:
                return false;
        }
    }

    public boolean usesCurrency(String currencyKey) {
        return (displayType == DisplayType.CURRENCY && currencyKey.equals(currencyKey));
    }

    public boolean usesMana() {
        return displayType == DisplayType.MANA;
    }
}
