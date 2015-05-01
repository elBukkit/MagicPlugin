package com.elmakers.mine.bukkit.magic;

import org.bukkit.configuration.ConfigurationSection;

public class MageLoadTask implements Runnable {
    private final Mage mage;
    private final ConfigurationSection mageData;

    public MageLoadTask(Mage mage, ConfigurationSection data) {
        this.mage = mage;
        this.mageData = data;
    }

    @Override
    public void run() {
        try {
            mage.setLoading(false);
            mage.load(mageData);
        } catch (Exception ex) {
            mage.getController().getLogger().warning("Failed to load mage data for player " + mage.getName());
            mage.setLoading(true);
        }
    }
}