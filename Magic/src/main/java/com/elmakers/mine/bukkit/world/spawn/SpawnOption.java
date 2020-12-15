package com.elmakers.mine.bukkit.world.spawn;

import com.elmakers.mine.bukkit.api.entity.EntityData;

public class SpawnOption {
    private final EntityData replacement;
    private final SpawnResult type;

    public SpawnOption(EntityData entity) {
        this.replacement = entity;
        this.type = replacement == null ? SpawnResult.SKIP : SpawnResult.REPLACE;
    }

    public SpawnOption(SpawnResult type) {
        this.type = type;
        this.replacement = null;
    }

    public EntityData getReplacement() {
        return replacement;
    }

    public SpawnResult getType() {
        return type;
    }

    public String describe() {
        if (replacement != null) {
            return replacement.describe();
        }
        return "(" + type.name().toLowerCase() + ")";
    }
}
