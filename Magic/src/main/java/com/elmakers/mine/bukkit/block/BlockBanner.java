package com.elmakers.mine.bukkit.block;

import java.util.List;

import org.bukkit.block.banner.Pattern;

public class BlockBanner extends MaterialExtraData {
    protected List<Pattern> patterns;

    public BlockBanner(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockBanner(patterns);
    }
}
