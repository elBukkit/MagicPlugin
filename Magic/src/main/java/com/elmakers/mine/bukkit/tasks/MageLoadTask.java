package com.elmakers.mine.bukkit.tasks;

import org.bukkit.Bukkit;

import com.elmakers.mine.bukkit.api.data.MageData;
import com.elmakers.mine.bukkit.api.event.MageLoadEvent;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.Mage;

public class MageLoadTask implements Runnable {
    private final Mage mage;
    private final MageData data;

    public MageLoadTask(Mage mage, MageData data) {
        this.mage = mage;
        this.data = data;
    }

    @Override
    public void run() {
        try {
            mage.onLoad(data);
            mage.setLoading(false);
            MageController controller = mage.getController();
            if (mage.isPlayer() && mage.isResourcePackEnabled()) {
                controller.promptResourcePack(mage.getPlayer());
            } else if (!mage.isResourcePackEnabledSet()) {
                controller.promptNoResourcePack(mage.getPlayer());
            }
            MageLoadEvent event = new MageLoadEvent(mage, data == null);
            Bukkit.getPluginManager().callEvent(event);
        } catch (Exception ex) {
            mage.getController().getLogger().warning("Failed to load mage data for player " + mage.getName());
            mage.setLoading(true);
        }
    }
}
