package com.elmakers.mine.bukkit.block;

import org.bukkit.DyeColor;

// TODO: 1.12-only, I guess.
public class BlockBed extends MaterialExtraData {
    protected DyeColor baseColor;

    public BlockBed(DyeColor color) {
        this.baseColor = color;
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockBed(baseColor);
    }
}
