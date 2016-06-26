package com.elmakers.mine.bukkit.block;

public class BlockMobSpawner extends BlockExtraData {
    protected String mobName;

    public BlockMobSpawner(String mobName) {
        this.mobName = mobName;
    }

    @Override
    public BlockExtraData clone() {
        return new BlockMobSpawner(mobName);
    }
}
