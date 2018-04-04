package com.elmakers.mine.bukkit.block;

import java.util.List;

import org.bukkit.block.banner.Pattern;

public class BlockBanner extends BlockExtraData {
    protected List<Pattern> patterns;

    public BlockBanner(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public BlockExtraData clone() {
        return new BlockBanner(patterns);
    }
}
