package com.elmakers.mine.bukkit.utility;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.utility.platform.Platform;

public abstract class PlayerProfile {
    protected final Platform platform;
    protected UUID uniqueId;
    protected String name;
    protected String skinURL;
    protected boolean saveProfile = true;

    protected PlayerProfile(Platform platform, UUID uniqueId, String name, String skinURL) {
        this.platform = platform;
        this.uniqueId = uniqueId;
        this.name = name;
        this.skinURL = skinURL;
    }

    protected PlayerProfile(Platform platform, OfflinePlayer player) {
        this.platform = platform;
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();
    }

    protected PlayerProfile(Platform platform, ConfigurationSection configuration) {
        this.platform = platform;
        final String uuidString = configuration.getString("uuid");
        this.uniqueId = uuidString == null || uuidString.isEmpty() ? null : UUID.fromString(uuidString);
        this.name = configuration.getString("name");
        this.skinURL = configuration.getString("skin");
    }

    public abstract boolean isComplete();
    public abstract String getDisguiseFormat();
    public abstract PlayerProfile update() throws ExecutionException, InterruptedException;
    public abstract void update(Skull skull);
    public abstract void update(SkullMeta skullMeta);

    public void save(ConfigurationSection configuration) {
        if (uniqueId != null) {
            configuration.set("uuid", uniqueId.toString());
        }
        configuration.set("skin", skinURL);
        configuration.set("name", name);
    }

    public String getSkinURL() {
        return skinURL;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setSaveProfile(boolean saveProfile) {
        this.saveProfile = saveProfile;
    }
}
