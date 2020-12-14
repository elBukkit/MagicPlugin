package com.elmakers.mine.bukkit.world.spawn;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.api.entity.EntityData;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ValueParser;

public class EntityDataParser extends ValueParser<EntityData> {
    private static EntityDataParser instance = null;
    private final MagicController controller;

    private EntityDataParser(MagicController controller) {
        this.controller = controller;
    }

    public static EntityDataParser getInstance(MagicController controller) {
        if (instance == null) {
            instance = new EntityDataParser(controller);
        }
        return instance;
    }

    @Override
    @Nullable
    public EntityData parse(String value) {
        return controller.getMob(value);
    }
}
