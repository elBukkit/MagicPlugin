package com.elmakers.mine.bukkit.block;

import org.bukkit.Color;

public class ColoredData extends MaterialExtraData {
    protected Color color;

    public ColoredData(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public MaterialExtraData clone() {
        return new ColoredData(color);
    }
}
