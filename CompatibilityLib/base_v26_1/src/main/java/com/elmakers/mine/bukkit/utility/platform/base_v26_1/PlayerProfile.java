package com.elmakers.mine.bukkit.utility.platform.base_v26_1;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.utility.SkullLoadedCallback;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class PlayerProfile extends com.elmakers.mine.bukkit.utility.PlayerProfile {
    private static Gson gson;
    private static String CONFIG_KEY = "data";

    protected static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    private org.bukkit.profile.PlayerProfile playerProfile;

    public PlayerProfile(Platform platform, org.bukkit.profile.PlayerProfile profile) {
        super(platform, profile.getUniqueId(), profile.getName(), profile.getTextures().getSkin() == null ? null : profile.getTextures().getSkin().toString());
        this.playerProfile = profile;
    }

    public PlayerProfile(Platform platform, OfflinePlayer player) {
        this(platform, player.getPlayerProfile());
    }

    public PlayerProfile(Platform platform, ConfigurationSection configuration) {
        super(platform, configuration);
        playerProfile = configuration.getSerializable(CONFIG_KEY, org.bukkit.profile.PlayerProfile.class);
        if (playerProfile != null) {
            name = playerProfile.getName();
            uniqueId = playerProfile.getUniqueId();
            URL skinURL = playerProfile == null ? null : playerProfile.getTextures().getSkin();
            this.skinURL = skinURL == null ? null : skinURL.toString();
        } else if (skinURL != null) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            try {
                item = platform.getInventoryUtils().setSkullURL(item, new URL(skinURL), uniqueId, name);
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta)itemMeta;
                    playerProfile = skullMeta.getOwnerProfile();
                }
            } catch (Exception ignore) {
            }
        } else if (name != null) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            try {
                platform.getDeprecatedUtils().setSkullOwner(item, name, new SkullLoadedCallback() {
                    @Override
                    public void updated(ItemStack itemStack) {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta instanceof SkullMeta) {
                            SkullMeta skullMeta = (SkullMeta)itemMeta;
                            playerProfile = skullMeta.getOwnerProfile();
                        }
                    }
                });
            } catch (Exception ignore) {
            }
        } else if (uniqueId != null) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            try {
                platform.getDeprecatedUtils().setSkullOwner(item, uniqueId, new SkullLoadedCallback() {
                    @Override
                    public void updated(ItemStack itemStack) {
                        ItemMeta itemMeta = itemStack.getItemMeta();
                        if (itemMeta instanceof SkullMeta) {
                            SkullMeta skullMeta = (SkullMeta) itemMeta;
                            playerProfile = skullMeta.getOwnerProfile();
                        }
                    }
                });
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void save(ConfigurationSection configuration) {
        super.save(configuration);
        if (saveProfile) {
            configuration.set(CONFIG_KEY, playerProfile);
        }
    }

    @Override
    public boolean isComplete() {
        // The Spigot implementation of this looks correct, but when running on Paper
        // it seems to do something different and will return true for an unloaded profile.
        // return playerProfile.isComplete()
        return playerProfile != null && playerProfile.getUniqueId() != null
                && playerProfile.getName() != null && !playerProfile.getTextures().isEmpty();
    }

    @Override
    public PlayerProfile update() throws ExecutionException, InterruptedException {
        if (playerProfile == null) return this;
        return new PlayerProfile(platform, playerProfile.update().get());
    }

    @Override
    public void update(Skull skull) {
        if (playerProfile != null) {
            skull.setOwnerProfile(playerProfile);
        }
    }

    @Override
    public void update(SkullMeta skullMeta) {
        if (playerProfile != null) {
            skullMeta.setOwnerProfile(playerProfile);
        }
    }

    @Override
    public String getDisguiseFormat() {
        Gson gson = getGson();
        String bukkitFormat = gson.toJson(playerProfile);
        Type objectMapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> bukkitMap = gson.fromJson(bukkitFormat, objectMapType);
        Map<String, Object> disguiseMap = new HashMap<>();
        disguiseMap.put("uuid", playerProfile.getUniqueId());
        disguiseMap.put("name", playerProfile.getName());
        disguiseMap.put("textureProperties", bukkitMap.get("properties"));
        return gson.toJson(disguiseMap);
    }
}
