package com.elmakers.mine.bukkit.block;

import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;

public class BlockSkull extends BlockExtraData {
    protected BlockFace rotation = null;
    protected SkullType skullType = null;
    protected Object profile = null;

    public BlockSkull(Object profile, SkullType type) {
        this(profile, type, BlockFace.SELF);
    }

    public BlockSkull(Object profile, SkullType type, BlockFace rotation) {
        this.skullType = type;
        this.profile = profile;
        this.rotation = rotation;
    }

    @Override
    public BlockExtraData clone() {
        return new BlockSkull(profile, skullType, rotation);
    }
}
