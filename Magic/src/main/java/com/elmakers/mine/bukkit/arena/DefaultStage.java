package com.elmakers.mine.bukkit.arena;

import org.bukkit.configuration.ConfigurationSection;

public class DefaultStage extends ArenaStageTemplate {
    public DefaultStage(Arena arena) {
        super(arena);
    }

    public DefaultStage(Arena arena, ConfigurationSection configuration) {
        super(arena, configuration);
    }

    @Override
    public void setName(String name) {
        throw new IllegalStateException("Tried to set default stage name");
    }

    @Override
    public String getName() {
        return "(Default Stage)";
    }
}
