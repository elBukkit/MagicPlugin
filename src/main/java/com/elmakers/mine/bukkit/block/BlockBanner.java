package com.elmakers.mine.bukkit.block;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;

import java.util.List;

public class BlockBanner extends BlockExtraData {
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
    public BlockExtraData clone() {
        return new BlockBanner(patterns, baseColor);
    }
}
