package com.elmakers.mine.bukkit.world.spawn;

import javax.annotation.Nullable;

import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ValueParser;

public class SpawnOptionParser extends ValueParser<SpawnOption> {
    private static SpawnOptionParser instance = null;
    private final MagicController controller;

    private SpawnOptionParser(MagicController controller) {
        this.controller = controller;
    }

    public static SpawnOptionParser getInstance(MagicController controller) {
        if (instance == null) {
            instance = new SpawnOptionParser(controller);
        }
        return instance;
    }

    @Override
    @Nullable
    public SpawnOption parse(String value) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("none")) {
            return new SpawnOption(SpawnResult.SKIP);
        }
        try {
            SpawnResult optionType = SpawnResult.valueOf(value.toUpperCase());
            return new SpawnOption(optionType);
        } catch (Exception ignore) {
        }
        return new SpawnOption(controller.getMob(value));
    }
}
