package com.elmakers.mine.bukkit.kit;

import org.bukkit.configuration.ConfigurationSection;

public class GivenItem {
    private final String itemKey;
    private int amount;
    private long lastGive;
    private long lastTook;

    public GivenItem(String itemKey) {
        this.itemKey = itemKey;
    }

    public GivenItem(String itemKey, ConfigurationSection config) {
        this.itemKey = itemKey;
        if (config != null) {
            this.amount = config.getInt("amount");
            this.lastGive = config.getLong("last_give");
            this.lastTook = config.getLong("last_took");
        }
    }

    public void took() {
        this.lastTook = System.currentTimeMillis();
    }

    public void add(int amount) {
        this.amount += amount;
        this.lastGive = System.currentTimeMillis();
    }

    public void saveTo(ConfigurationSection config) {
        ConfigurationSection section = config.createSection(itemKey);
        section.set("amount", amount);
        if (lastGive > 0) {
            section.set("last_give", lastGive);
        }
        if (lastTook > 0) {
            section.set("last_took", lastTook);
        }
    }

    public int getAmount() {
        return amount;
    }
}
