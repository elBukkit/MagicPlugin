package com.elmakers.mine.bukkit.block;

import org.bukkit.Color;

public class LeatherArmorData extends BlockExtraData {
    protected Color color;

    public LeatherArmorData(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public BlockExtraData clone() {
        return new LeatherArmorData(color);
    }
}
