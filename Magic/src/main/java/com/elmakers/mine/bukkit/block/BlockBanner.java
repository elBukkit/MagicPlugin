package com.elmakers.mine.bukkit.block;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;

public class BlockBanner extends MaterialExtraData {
    protected List<Pattern> patterns;
    protected DyeColor baseColor;

    public BlockBanner(List<Pattern> patterns, DyeColor color) {
        this.patterns = patterns;
        this.baseColor = color;
    }

    public BlockBanner(DyeColor color) {
        this(null, color);
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockBanner(patterns, baseColor);
    }
}
