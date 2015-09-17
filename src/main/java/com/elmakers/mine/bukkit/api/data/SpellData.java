package com.elmakers.mine.bukkit.api.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class SpellData {
    private String spellKey;
    private boolean isActive;
    private long lastCast;
    private long castCount;
    private ConfigurationSection data;

    public SpellData(String spellKey) {
        this.spellKey = spellKey;
    }

    public void setExtraData(ConfigurationSection data) {
        this.data = data;
    }

    public ConfigurationSection getExtraData() {
        if (data == null) {
            data = new MemoryConfiguration();
        }
        return data;
    }

    public String getKey() {
        return spellKey;
    }

    public void setKey(String spellKey) {
        this.spellKey = spellKey;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
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
}
