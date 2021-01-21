package com.elmakers.mine.bukkit.tasks;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import com.elmakers.mine.bukkit.api.magic.MageController;

public class DisguiseTask implements Runnable {
    private final MageController controller;
    private final Entity entity;
    private final ConfigurationSection disguise;

    public DisguiseTask(MageController controller, Entity entity, ConfigurationSection disguise) {
        this.controller = controller;
        this.entity = entity;
        this.disguise = disguise;
    }

    @Override
    public void run() {
        controller.disguise(entity, disguise);
    }
}
