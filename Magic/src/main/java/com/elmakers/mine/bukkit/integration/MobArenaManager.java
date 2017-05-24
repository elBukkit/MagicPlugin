package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.garbagemule.MobArena.util.ItemParser;
import com.garbagemule.MobArena.util.ItemProvider;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class MobArenaManager implements ItemProvider {
    private final MageController controller;

    public MobArenaManager(MageController controller) {
        this.controller = controller;
        ItemParser.registerItemProvider(this);

        Set<String> magicMobKeys = controller.getMobKeys();
        for (String mob : magicMobKeys) {
            new MagicMACreature(controller, mob, controller.getMob(mob));
        }
    }

    @Override
    public ItemStack getItem(String s) {
        if (!s.startsWith("magic:")) return null;
        s = s.substring(6);
        return controller.createItem(s);
    }
}
