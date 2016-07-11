package com.elmakers.mine.bukkit.api.data;

import com.elmakers.mine.bukkit.api.spell.SpellKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public class SpellData {
    private SpellKey spellKey;
    private boolean isActive;
    private long lastCast;
    private long lastEarn;
    private long castCount;
    private long cooldownExpiration;
    private ConfigurationSection data;

    public SpellData(SpellKey spellKey) {
        this.spellKey = spellKey;
    }

    public SpellData(String spellKey) {
        this(new SpellKey(spellKey));
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
}
