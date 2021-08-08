package com.elmakers.mine.bukkit.arena;

public class DefaultStage extends ArenaStageTemplate {
    public DefaultStage(Arena arena) {
        super(arena);
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
