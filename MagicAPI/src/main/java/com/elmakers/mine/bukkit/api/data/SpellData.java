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
    private long chargesUsed;
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

    public long getChargesUsed() {
        return chargesUsed;
    }

    public void setChargesUsed(long chargesUsed) {
        this.chargesUsed = chargesUsed;
    }
}
