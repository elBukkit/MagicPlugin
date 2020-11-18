package com.elmakers.mine.bukkit.resourcepack;

import java.util.Date;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.io.BaseEncoding;

public class ResourcePack {
    private final String key;
    private final String url;
    private final byte[] hash;
    private final Date modified;

    public ResourcePack(String url, byte[] hash, Date modified) {
        this.key = url.replace(".", "_");
        this.url = url;
        this.hash = hash;
        this.modified = modified;
    }

    public ResourcePack(String key, ConfigurationSection configuration) {
        this.key = key;
        this.url = configuration.getString("url");
        String hashString = configuration.getString("sha1");

        // Ignore old encoding, we will need to update
        if (hashString != null && hashString.length() < 40) {
            hash = BaseEncoding.base64().decode(hashString);
        } else {
            hash = null;
        }
        Long modifiedEpoch = configuration.getLong("modified");
        this.modified =  new Date(modifiedEpoch);
    }

    public String getUrl() {
        return url;
    }

    public byte[] getHash() {
        return hash;
    }

    public Date getModified() {
        return modified;
    }

    public String getKey() {
        return key;
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("url", url);
        configuration.set("modified", modified.getTime());
        if (hash != null) {
            String resourcePackHash = BaseEncoding.base64().encode(hash);
            configuration.set("sha1", resourcePackHash);
        }
    }
}
