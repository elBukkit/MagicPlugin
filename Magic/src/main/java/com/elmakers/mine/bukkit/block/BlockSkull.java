package com.elmakers.mine.bukkit.block;

import org.bukkit.block.BlockFace;

public class BlockSkull extends MaterialExtraData {
    protected BlockFace rotation = null;
    protected Object profile = null;
    protected String playerName = null;

    public BlockSkull(String playerName) {
        this(null, playerName, BlockFace.SELF);
    }

    public BlockSkull(Object profile) {
        this(profile, BlockFace.SELF);
    }

    public BlockSkull(Object profile, BlockFace rotation) {
        this.profile = profile;
        this.rotation = rotation;
    }

    private BlockSkull(Object profile, String playerName, BlockFace rotation) {
        this.profile = profile;
        this.rotation = rotation;
        this.playerName = playerName;
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockSkull(profile, playerName, rotation);
    }
}
