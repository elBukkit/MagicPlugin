package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.wand.Wand;

import java.util.Collection;

public abstract class CasterProperties extends BaseMagicConfigurable {
    protected int manaRegeneration = 0;
    protected int manaMax = 0;
    protected long lastManaRegeneration = 0;
    protected float mana = 0;

    protected int effectiveManaMax = 0;
    protected int effectiveManaRegeneration = 0;

    protected float manaMaxBoost = 0;
    protected float manaRegenerationBoost = 0;

    protected float costReduction = 0;

    public CasterProperties(MagicPropertyType type, MageController controller) {
        super(type, controller);
    }

    public int getManaRegeneration() {
        return manaRegeneration;
    }

    public int getManaMax() {
        return manaMax;
    }

    public void setMana(float mana) {
        if (isCostFree()) {
            setProperty("mana", null);
        } else {
            this.mana = Math.max(0, mana);
            setProperty("mana", this.mana);
        }
    }

    public void setManaMax(int manaMax) {
        this.manaMax = Math.max(0, manaMax);
        setProperty("mana_max", this.manaMax);
    }

    public float getMana() {
        return mana;
    }

    public void removeMana(float amount) {
        setMana(mana - amount);
    }

    public float getManaRegenerationBoost() {
        return manaRegenerationBoost;
    }

    public float getManaMaxBoost() {
        return manaMaxBoost;
    }

    public float getCostReduction() {
        if (isCostFree()) return 1.0f;
        return controller.getCostReduction() + costReduction * controller.getMaxCostReduction();
    }

    public boolean isCostFree() {
        return costReduction > 1;
    }

    public int getEffectiveManaMax() {
        return effectiveManaMax;
    }

    public int getEffectiveManaRegeneration() {
        return effectiveManaRegeneration;
    }

    public void loadProperties() {
        manaRegeneration = getInt("mana_regeneration", getInt("xp_regeneration"));
        manaMax = getInt("mana_max", getInt("xp_max"));
        mana = getInt("mana", getInt("xp"));
        lastManaRegeneration = getLong("mana_timestamp");

        manaMaxBoost = (float)getDouble("mana_max_boost", getDouble("xp_max_boost"));
        manaRegenerationBoost = (float)getDouble("mana_regeneration_boost", getDouble("xp_regeneration_boost"));

        updateMaxMana(null);
    }

    protected boolean updateMaxMana(Mage mage) {
        if (!usesMana()) {
            return false;
        }
        int currentMana = effectiveManaMax;
        int currentManaRegen = effectiveManaRegeneration;

        float effectiveBoost = manaMaxBoost;
        float effectiveRegenBoost = manaRegenerationBoost;
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
        effectiveManaMax = manaMax;
        if (effectiveBoost != 0) {
            effectiveManaMax = (int)Math.ceil(effectiveManaMax + effectiveBoost * effectiveManaMax);
        }
        effectiveManaRegeneration = manaRegeneration;
        if (effectiveRegenBoost != 0) {
            effectiveManaRegeneration = (int)Math.ceil(effectiveManaRegeneration + effectiveRegenBoost * effectiveManaRegeneration);
        }

        return (currentMana != effectiveManaMax || effectiveManaRegeneration != currentManaRegen);
    }

    public boolean usesMana() {
        if (isCostFree()) return false;
        return manaMax > 0;
    }

    public boolean tickMana() {
        boolean updated = false;
        if (usesMana()) {
            long now = System.currentTimeMillis();
            if (manaRegeneration > 0 && lastManaRegeneration > 0 && effectiveManaRegeneration > 0)
            {
                long delta = now - lastManaRegeneration;
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
