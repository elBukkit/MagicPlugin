package com.elmakers.mine.bukkit.magic;

import com.elmakers.mine.bukkit.api.magic.MageController;
import org.bukkit.configuration.ConfigurationSection;

public abstract class CasterProperties extends BaseMagicConfigurable {
    protected int manaRegeneration = 0;
    protected int manaMax = 0;
    protected long lastManaRegeneration = 0;
    protected float mana = 0;

    protected int effectiveManaMax = 0;
    protected int effectiveManaRegeneration = 0;

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

    protected void loadProperties(ConfigurationSection config) {
        manaRegeneration = config.getInt("mana_regeneration", config.getInt("xp_regeneration"));
        manaMax = config.getInt("mana_max", config.getInt("xp_max"));
        mana = config.getInt("mana", config.getInt("xp"));
        lastManaRegeneration = config.getLong("mana_timestamp");
    }
}
