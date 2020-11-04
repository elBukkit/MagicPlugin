package com.elmakers.mine.bukkit.magic.command.config;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.SkinUtils;

public class NewSessionRequest {
    public static class PlayerInformation {
        public String id;
        public String name;
        public String skinUrl;
    }

    private final String type;
    private String contents;
    private String name;
    private PlayerInformation player;
    private boolean isLegacyIcons;
    private String magicVersion;
    private int[] minecraftVersion;

    public NewSessionRequest(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayer(Player bukkitPlayer) {
        player = new PlayerInformation();
        player.id = bukkitPlayer.getUniqueId().toString();
        player.name = bukkitPlayer.getName();
        player.skinUrl = SkinUtils.getOnlineSkinURL(player.name);
    }

    public void setLegacyIcons(boolean legacyIcons) {
        isLegacyIcons = legacyIcons;
    }

    public String getContents() {
        return contents;
    }

    public String getName() {
        return name;
    }

    public PlayerInformation getPlayer() {
        return player;
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
}
