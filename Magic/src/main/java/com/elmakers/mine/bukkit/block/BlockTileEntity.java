package com.elmakers.mine.bukkit.block;

public class BlockTileEntity extends MaterialExtraData {
    protected Object data;

    public BlockTileEntity(Object data) {
        this.data = data;
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockTileEntity(data);
    }
}
