package com.elmakers.mine.bukkit.block;

public class BlockMobSpawner extends MaterialExtraData {
    protected String mobName;

    public BlockMobSpawner(String mobName) {
        this.mobName = mobName;
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockMobSpawner(mobName);
    }
}
