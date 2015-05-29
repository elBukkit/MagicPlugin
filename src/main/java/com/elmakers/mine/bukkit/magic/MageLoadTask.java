package com.elmakers.mine.bukkit.magic;

import org.bukkit.configuration.ConfigurationSection;

public class MageLoadTask implements Runnable {
    private final Mage mage;

    public MageLoadTask(Mage mage) {
        this.mage = mage;
    }

    @Override
    public void run() {
        try {
            mage.onLoad();
            mage.setLoading(false);
        } catch (Exception ex) {
            mage.getController().getLogger().warning("Failed to load mage data for player " + mage.getName());
            mage.setLoading(true);
        }
    }
}