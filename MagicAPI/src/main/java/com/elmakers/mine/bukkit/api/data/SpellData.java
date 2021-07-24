package com.elmakers.mine.bukkit.api.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.api.spell.SpellKey;

public class SpellData {
    private SpellKey spellKey;
    private boolean isActive;
    private boolean isEnabled = true;
    private long lastCast;
    private long lastEarn;
    private long castCount;
    private long cooldownExpiration;
    private double charges;
    private ConfigurationSection variables;

    public SpellData(SpellKey spellKey) {
        this.spellKey = spellKey;
    }

    public SpellData(String spellKey) {
        this(new SpellKey(spellKey));
    }

    @Deprecated
    public void setExtraData(ConfigurationSection data) {
       this.setVariables(data);
    }

    @Deprecated
    public ConfigurationSection getExtraData() {
        return getVariables();
    }

    public void setVariables(ConfigurationSection data) {
        this.variables = data;
    }

    public ConfigurationSection getVariables() {
        if (variables == null) {
            variables = new MemoryConfiguration();
        }
        return variables;
    }

    public SpellKey getKey() {
        return spellKey;
    }

    public void setKey(String spellKey) {
        this.spellKey = new SpellKey(spellKey);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public long getLastCast() {
        return lastCast;
    }

    public void setLastCast(long lastCast) {
        this.lastCast = lastCast;
    }

    public boolean useCharge(double regenerationRate, double maxCharges) {
        if (maxCharges <= 0 || regenerationRate <= 0) return true;
        double newCharges = getCharges(regenerationRate, maxCharges);
        if (newCharges < 1) {
            return false;
        }
        charges = newCharges - 1;
        return true;
    }

    public long getTimeToRecharge(double regenerationRate, double maxCharges) {
        if (maxCharges <= 0 || regenerationRate <= 0) return 0;
        double charges = getCharges(regenerationRate, maxCharges);
        if (charges >= 1) return 0;
        return (long)(1000.0 * (1.0 - charges) / regenerationRate);
    }

    public double getCharges(double regenerationRate, double maxCharges) {
        return Math.min(charges + regenerationRate * (System.currentTimeMillis() - lastCast) / 1000, maxCharges);
    }

    public double getCharges() {
        return charges;
    }

    public void setCharges(double charges) {
        this.charges = charges;
    }

    public long getCastCount() {
        return castCount;
    }

    public void setCastCount(long castCount) {
        this.castCount = castCount;
    }

    public void addCast() {
        castCount++;
    }

    public long getCooldownExpiration() {
        return cooldownExpiration;
    }

    public void setCooldownExpiration(long cooldownExpiration) {
        this.cooldownExpiration = cooldownExpiration;
    }

    public long getLastEarn() {
        return lastEarn;
    }

    public void setLastEarn(long lastEarn) {
        this.lastEarn = lastEarn;
    }
}
