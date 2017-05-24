package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.garbagemule.MobArena.util.ItemParser;
import com.garbagemule.MobArena.util.ItemProvider;
import org.bukkit.inventory.ItemStack;

public class MobArenaManager implements ItemProvider {
    private final MageController controller;

    public MobArenaManager(MageController controller) {
        this.controller = controller;
        ItemParser.registerItemProvider(this);
    }

    @Override
    public ItemStack getItem(String s) {
        if (!s.startsWith("magic:")) return null;
        s = s.substring(6);
        return controller.createItem(s);
    }
}
