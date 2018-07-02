package com.elmakers.mine.bukkit.block;

import org.bukkit.Color;

public class PotionData extends BlockExtraData {
    protected Color color;

    public PotionData(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public BlockExtraData clone() {
        return new PotionData(color);
    }
}
