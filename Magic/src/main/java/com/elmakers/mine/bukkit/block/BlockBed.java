package com.elmakers.mine.bukkit.block;

import org.bukkit.DyeColor;

// TODO: 1.12-only, I guess.
public class BlockBed extends BlockExtraData {
    protected DyeColor baseColor;

    public BlockBed(DyeColor color) {
        this.baseColor = color;
    }

    @Override
    public BlockExtraData clone() {
        return new BlockBed(baseColor);
    }
}
