package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.wand.Wand;

import java.util.Collection;

public abstract class CasterProperties extends BaseMagicConfigurable {
    protected int effectiveManaMax = 0;
    protected int effectiveManaRegeneration = 0;

    public CasterProperties(MagicPropertyType type, MageController controller) {
        super(type, controller);
    }

    public boolean hasOwnMana() {
        return hasOwnProperty("mana_max");
    }

    public int getManaRegeneration() {
        return getInt("mana_regeneration", getInt("xp_regeneration"));
    }

    public int getManaMax() {
        return getInt("mana_max", getInt("xp_max"));
    }

    public void setMana(float mana) {
        if (isCostFree()) {
            setProperty("mana", null);
        } else {
            setProperty("mana", Math.max(0, mana));
        }
    }

    public void setManaMax(int manaMax) {
        setProperty("mana_max", Math.max(0, manaMax));
    }

    public void setManaRegeneration(int manaRegeneration) {
        setProperty("mana_regeneration", Math.max(0, manaRegeneration));
    }

    public float getMana() {
        return getFloat("mana", getFloat("xp"));
    }

    public void removeMana(float amount) {
        setMana(getMana() - amount);
    }

    public float getManaRegenerationBoost() {
        return getFloat("mana_regeneration_boost", getFloat("xp_regeneration_boost"));
    }

    public float getManaMaxBoost() {
        return getFloat("mana_max_boost", getFloat("xp_max_boost"));
    }

    public float getCostReduction() {
        if (isCostFree()) return 1.0f;
        return controller.getCostReduction() + getFloat("cost_reduction") * controller.getMaxCostReduction();
    }

    public boolean isCostFree() {
        return getFloat("cost_reduction") > 1;
    }

    public int getEffectiveManaMax() {
        return effectiveManaMax;
    }

    public int getEffectiveManaRegeneration() {
        return effectiveManaRegeneration;
    }

    protected long getLastManaRegeneration() {
        return getLong("mana_timestamp");
    }

    public void loadProperties() {
        updateMaxMana(null);
    }

    protected boolean updateMaxMana(Mage mage) {
        if (!usesMana()) {
            return false;
        }
        int currentMana = effectiveManaMax;
        int currentManaRegen = effectiveManaRegeneration;

        float effectiveBoost = getManaMaxBoost();
        float effectiveRegenBoost = getManaRegenerationBoost();
        if (mage != null)
        {
            Collection<Wand> activeArmor = mage.getActiveArmor();
            for (Wand armorWand : activeArmor) {
                effectiveBoost += armorWand.getManaMaxBoost();
                effectiveRegenBoost += armorWand.getManaRegenerationBoost();
            }
            Wand activeWand = mage.getActiveWand();
            if (activeWand != null && !activeWand.isPassive()) {
                effectiveBoost += activeWand.getManaMaxBoost();
                effectiveRegenBoost += activeWand.getManaRegenerationBoost();
            }
            Wand offhandWand = mage.getOffhandWand();
            if (offhandWand != null && !offhandWand.isPassive()) {
                effectiveBoost += offhandWand.getManaMaxBoost();
                effectiveRegenBoost += offhandWand.getManaRegenerationBoost();
            }
        }
        effectiveManaMax = getManaMax();
        if (effectiveBoost != 0) {
            effectiveManaMax = (int)Math.ceil(effectiveManaMax + effectiveBoost * effectiveManaMax);
        }
        effectiveManaRegeneration = getManaRegeneration();
        if (effectiveRegenBoost != 0) {
            effectiveManaRegeneration = (int)Math.ceil(effectiveManaRegeneration + effectiveRegenBoost * effectiveManaRegeneration);
        }

        return (currentMana != effectiveManaMax || effectiveManaRegeneration != currentManaRegen);
    }

    public boolean usesMana() {
        if (isCostFree()) return false;
        return getManaMax() > 0;
    }

    public boolean tickMana() {
        boolean updated = false;
        if (usesMana() && hasOwnMana()) {
            long now = System.currentTimeMillis();
            long lastManaRegeneration = getLastManaRegeneration();
            int manaRegeneration = getManaRegeneration();
            int effectiveManaRegeneration = getEffectiveManaRegeneration();
            if (manaRegeneration > 0 && lastManaRegeneration > 0 && effectiveManaRegeneration > 0)
            {
                long delta = now - lastManaRegeneration;
                int effectiveManaMax = getEffectiveManaMax();
                int manaMax = getManaMax();
                float mana = getMana();
                if (effectiveManaMax == 0 && manaMax > 0) {
                    effectiveManaMax = manaMax;
                }
                setMana(Math.min(effectiveManaMax, mana + (float) effectiveManaRegeneration * (float)delta / 1000));
                updated = true;
            }
            lastManaRegeneration = now;
            setProperty("mana_timestamp", lastManaRegeneration);
        }

        return updated;
    }

    public void tick() {
        tickMana();
    }
}
