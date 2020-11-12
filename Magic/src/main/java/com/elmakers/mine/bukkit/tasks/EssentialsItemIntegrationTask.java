package com.elmakers.mine.bukkit.tasks;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.essentials.MagicItemDb;
import com.elmakers.mine.bukkit.magic.MagicController;

public class EssentialsItemIntegrationTask implements Runnable {
    private final MagicController controller;

    public EssentialsItemIntegrationTask(MagicController controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            Object essentials = controller.getPlugin().getServer().getPluginManager().getPlugin("Essentials");
            if (essentials != null) {
                Class<?> essentialsClass = essentials.getClass();
                essentialsClass.getMethod("getItemDb");
                if (MagicItemDb.register(controller, (Plugin)essentials)) {
                    controller.getLogger().info("Essentials found, hooked up custom item handler");
                } else {
                    controller.getLogger().warning("Essentials found, but something went wrong hooking up the custom item handler");
                }
            }
        } catch (Throwable ex) {
            controller.getLogger().warning("Essentials found, but is not up to date. Magic item integration will not work with this version of Magic. Please upgrade EssentialsX or downgrade Magic to 7.6.19");
        }
    }
}
