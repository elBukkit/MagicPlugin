package com.elmakers.mine.bukkit.block;

import org.bukkit.block.BlockFace;
import org.bukkit.profile.PlayerProfile;

public class BlockSkull extends MaterialExtraData {
    private BlockFace rotation = null;
    private PlayerProfile profile = null;
    private String playerName = null;

    public BlockSkull(String playerName) {
        this(null, playerName, BlockFace.SELF);
    }

    public BlockSkull(PlayerProfile profile) {
        this(profile, BlockFace.SELF);
    }

    public BlockSkull(PlayerProfile profile, BlockFace rotation) {
        this.profile = profile;
        this.rotation = rotation;
    }

    private BlockSkull(PlayerProfile profile, String playerName, BlockFace rotation) {
        this.profile = profile;
        this.rotation = rotation;
        this.playerName = playerName;
    }

    public BlockFace getRotation() {
        return rotation;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public MaterialExtraData clone() {
        return new BlockSkull(profile, playerName, rotation);
    }
}
