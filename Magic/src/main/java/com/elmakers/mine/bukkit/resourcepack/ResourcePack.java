package com.elmakers.mine.bukkit.resourcepack;

import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.io.BaseEncoding;

public class ResourcePack {
    private final @Nonnull String key;
    private final @Nonnull String url;
    private @Nonnull Date modified;
    private @Nullable byte[] hash;
    private boolean checked = false;

    public ResourcePack(String url) {
        this.key = getKey(url);
        this.url = url;
        this.modified = new Date(0L);
        this.hash = null;
    }

    public ResourcePack(String url, byte[] hash, Date modified) {
        this.key = getKey(url);
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

    @Nonnull
    public String getUrl() {
        return url;
    }

    @Nullable
    public byte[] getHash() {
        return hash;
    }

    @Nullable
    public String getHashString() {
        return hash == null ? null : BaseEncoding.base64().encode(hash);
    }

    @Nonnull
    public Date getModified() {
        return modified;
    }

    @Nonnull
    public static String getKey(String url) {
        return url.replace(".", "_");
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    public void save(ConfigurationSection configuration) {
        configuration.set("url", url);
        configuration.set("modified", modified.getTime());
        if (hash != null) {
            configuration.set("sha1", getHashString());
        }
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void update(byte[] hash, Date modified) {
        this.hash = hash;
        this.modified = modified;
    }
}
