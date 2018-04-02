package com.elmakers.mine.bukkit.integration.mobarena;

import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.garbagemule.MobArena.things.Thing;
import com.garbagemule.MobArena.things.ThingParser;

public class MagicThingParser implements ThingParser {
    private MageController controller;

    public MagicThingParser(MageController controller) {
        this.controller = controller;
        org.bukkit.Bukkit.getLogger().info("Registering magic thing parser");
    }

    @Override
    @Nullable
    public Thing parse(String itemKey) {
        if (!itemKey.startsWith("magic:")) return null;
        itemKey = itemKey.substring(6);

        ItemStack item = controller.createItem(itemKey);
        return item == null ? null : new MagicItemThing(item);
    }
}
