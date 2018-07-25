package com.elmakers.mine.bukkit.integration.mobarena;

import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.garbagemule.MobArena.things.ItemStackParser;

public class MagicItemStackParser implements ItemStackParser {
    private MageController controller;

    public MagicItemStackParser(MageController controller) {
        this.controller = controller;
        controller.getLogger().info("Registering magic thing parser");
    }

    @Override
    @Nullable
    public ItemStack parse(String itemKey) {
        if (!itemKey.startsWith("magic:")) return null;
        itemKey = itemKey.substring(6);

        return controller.createItem(itemKey);
    }
}
