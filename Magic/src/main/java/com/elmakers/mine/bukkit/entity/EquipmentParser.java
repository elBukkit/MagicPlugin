package com.elmakers.mine.bukkit.entity;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.item.ItemData;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.utility.random.ValueParser;

public class EquipmentParser extends ValueParser<ItemData> {
    private static EquipmentParser instance = null;
    private final MageController controller;

    private EquipmentParser(MageController controller) {
        this.controller = controller;
    }

    public static EquipmentParser getInstance(MageController controller) {
        if (instance == null) {
            instance = new EquipmentParser(controller);
        }
        return instance;
    }

    @Override
    @Nullable
    public ItemData parse(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("none")) {
            return null;
        }
        return controller.getOrCreateItem(value);
    }
}
