package com.elmakers.mine.bukkit.block;

import org.bukkit.Color;

public class LeatherArmorData extends MaterialExtraData {
    protected Color color;

    public LeatherArmorData(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public MaterialExtraData clone() {
        return new LeatherArmorData(color);
    }
}
