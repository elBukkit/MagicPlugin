package com.elmakers.mine.bukkit.magic.command.config;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.platform.SkinUtils;

public class Session {
    protected String type;
    protected String contents;
    protected String key;
    protected PlayerInformation player;
    protected boolean isLegacyIcons;
    protected String magicVersion;
    protected int[] minecraftVersion;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLegacyIcons(boolean legacyIcons) {
        isLegacyIcons = legacyIcons;
    }

    public String getContents() {
        return contents;
    }

    public String getKey() {
        return key;
    }

    public PlayerInformation getPlayer() {
        return player;
    }

    public void setPlayer(PlayerInformation player) {
        this.player = player;
    }

    public boolean isLegacyIcons() {
        return isLegacyIcons;
    }

    public String getMagicVersion() {
        return magicVersion;
    }

    public void setMagicVersion(String magicVersion) {
        this.magicVersion = magicVersion;
    }

    public int[] getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(int[] minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void setBukkitPlayer(Player bukkitPlayer) {
        player = new PlayerInformation();
        player.id = bukkitPlayer.getUniqueId().toString();
        player.name = bukkitPlayer.getName();
        player.skinUrl = SkinUtils.getOnlineSkinURL(player.name);
    }
}
