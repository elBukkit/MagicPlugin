package com.elmakers.mine.bukkit.kit;

import org.bukkit.configuration.ConfigurationSection;

public class GivenItem {
    private final String itemKey;
    private int amount;
    private long lastGive;

    public GivenItem(String itemKey, int itemAmount) {
        this.itemKey = itemKey;
        this.amount = itemAmount;
        this.lastGive = System.currentTimeMillis();
    }

    public GivenItem(String itemKey, ConfigurationSection config) {
        this.itemKey = itemKey;
        if (config != null) {
            this.amount = config.getInt("amount");
            this.lastGive = config.getLong("last_give");
        }
    }

    public void add(int amount) {
        this.amount += amount;
        this.lastGive = System.currentTimeMillis();
    }

    public void saveTo(ConfigurationSection config) {
        ConfigurationSection section = config.createSection(itemKey);
        section.set("amount", amount);
        section.set("last_give", lastGive);
    }

    public int getAmount() {
        return amount;
    }
}
