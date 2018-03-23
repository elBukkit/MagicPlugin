package com.elmakers.mine.bukkit.block;

import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;

public class BlockSkull extends BlockExtraData {
    protected BlockFace rotation = null;
    protected SkullType skullType = null;
    protected Object profile = null;
    protected String playerName = null;

    public BlockSkull(String playerName) {
        this(null, playerName, SkullType.PLAYER, BlockFace.SELF);
    }

    public BlockSkull(Object profile, SkullType type) {
        this(profile, type, BlockFace.SELF);
    }

    public BlockSkull(Object profile, SkullType type, BlockFace rotation) {
        this.skullType = type;
        this.profile = profile;
        this.rotation = rotation;
    }

    private BlockSkull(Object profile, String playerName, SkullType skullType, BlockFace rotation) {
        this.skullType = skullType;
        this.profile = profile;
        this.rotation = rotation;
        this.playerName = playerName;
    }

    @Override
    public BlockExtraData clone() {
        return new BlockSkull(profile, playerName, skullType, rotation);
    }
}
