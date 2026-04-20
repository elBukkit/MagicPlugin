package com.elmakers.mine.bukkit.utility;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.meta.SkullMeta;

public abstract class PlayerProfile {
    protected UUID uniqueId;
    protected String name;
    protected String skinURL;
    protected boolean saveProfile = true;

    protected PlayerProfile(UUID uniqueId, String name, String skinURL) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.skinURL = skinURL;
    }

    protected PlayerProfile(OfflinePlayer player) {
        this.uniqueId = player.getUniqueId();
        this.name = player.getName();
    }

    protected PlayerProfile(ConfigurationSection configuration) {
        this.uniqueId = UUID.fromString(configuration.getString("uuid"));
        this.name = configuration.getString("name");
        this.skinURL = configuration.getString("skin");
    }

    public abstract boolean isComplete();
    public abstract String getDisguiseFormat();
    public abstract PlayerProfile update() throws ExecutionException, InterruptedException;
    public abstract void update(Skull skull);
    public abstract void update(SkullMeta skullMeta);

    public void save(ConfigurationSection configuration) {
        configuration.set("uuid", uniqueId.toString());
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
