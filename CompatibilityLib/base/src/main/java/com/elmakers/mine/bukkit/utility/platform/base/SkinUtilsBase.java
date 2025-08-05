package com.elmakers.mine.bukkit.utility.platform.base;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.OfflinePlayerCallback;
import com.elmakers.mine.bukkit.utility.ProfileCallback;
import com.elmakers.mine.bukkit.utility.ProfileResponse;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class SkinUtilsBase implements SkinUtils {
    protected final Platform platform;
    protected final Map<UUID, ProfileResponse> responseCache = new HashMap<>();
    protected final Map<String, Object> loadingUUIDs = new HashMap<>();
    protected final Map<UUID, Object> loadingProfiles = new HashMap<>();
    protected long holdoff = 0;
    private static Gson gson;

    protected static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    protected SkinUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public String getOnlineSkinURL(Player player) {
        PlayerProfile playerProfile = player.getPlayerProfile();
        URL skinURL = playerProfile == null ? null : playerProfile.getTextures().getSkin();
        return skinURL == null ? null : skinURL.toString();
    }

    @Override
    public String getOnlineSkinURL(String playerName) {
        if (playerName.startsWith("http")) return playerName;
        Player player = platform.getDeprecatedUtils().getPlayerExact(playerName);
        String url = null;
        if (player != null) {
            url = getOnlineSkinURL(player);
        }
        return url;
    }

    private void engageHoldoff() {
        holdoff = 10 * 60000;
    }

    private void synchronizeCallbackOfflinePlayer(final OfflinePlayerCallback callback, final OfflinePlayer offlinePlayer) {
        Bukkit.getScheduler().runTask(platform.getPlugin(), new Runnable() {
            @Override
            public void run() {
                callback.result(offlinePlayer);
            }
        });
    }

    private void synchronizeCallbackProfile(final ProfileCallback callback, final ProfileResponse response) {
        Bukkit.getScheduler().runTask(platform.getPlugin(), new Runnable() {
            @Override
            public void run() {
                callback.result(response);
            }
        });
    }

    private void fetchOfflinePlayer(final String playerName, final OfflinePlayerCallback callback) {
        final Player onlinePlayer = platform.getDeprecatedUtils().getPlayerExact(playerName);
        if (onlinePlayer != null) {
            callback.result(onlinePlayer);
            return;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(platform.getPlugin(), new Runnable() {
            @Override
            public void run() {
                // Don't request the same player more than once at a time
                Object lock;
                synchronized (loadingUUIDs) {
                    lock = loadingUUIDs.get(playerName);
                    if (lock == null) {
                        lock = new Object();
                        loadingUUIDs.put(playerName, lock);
                    }
                }
                // Subsequent requests should hit the cache
                synchronized (lock) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                    if (offlinePlayer == null || offlinePlayer.getUniqueId() == null) {
                        engageHoldoff();
                        if (CompatibilityConstants.DEBUG) {
                            platform.getLogger().warning("Failed to request offline player data for " + playerName + ", will not retry for 10 minutes");
                        }
                    }
                    synchronizeCallbackOfflinePlayer(callback, offlinePlayer);
                }
            }
        }, holdoff / 50);
    }

    @Override
    public void fetchProfile(final String playerName, final ProfileCallback callback) {
        fetchOfflinePlayer(playerName, new OfflinePlayerCallback() {
            @Override
            public void result(OfflinePlayer offlinePlayer) {
                if (offlinePlayer != null) {
                    fetchProfile(offlinePlayer, callback);
                } else {
                    callback.result(null);
                }
            }
        });
    }

    @Override
    public void fetchProfile(final UUID uuid, final ProfileCallback callback) {
        // Note that if not cached this OfflinePlayer will not have a name assigned
        // Hopefully this is fine
        // The upside is this will never make a Mojang request so we don't need locking or async here.
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        fetchProfile(offlinePlayer, callback);
    }

    public void fetchProfile(final OfflinePlayer offlinePlayer, final ProfileCallback callback) {
        UUID uuid = offlinePlayer == null ? null : offlinePlayer.getUniqueId();
        if (uuid == null) {
            callback.result(null);
            return;
        }

        // Check the cache first
        ProfileResponse cached;
        synchronized (responseCache) {
            cached = responseCache.get(uuid);
        }
        if (cached != null) {
            callback.result(cached);
            return;
        }

        // Check for the updated profile, fetch if necessary, then cache it
        final Plugin plugin = platform.getPlugin();
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                Object lock;
                synchronized (loadingProfiles) {
                    lock = loadingProfiles.get(uuid);
                    if (lock == null) {
                        lock = new Object();
                        loadingProfiles.put(uuid, lock);
                    }
                }
                synchronized (lock) {
                    // See if another thread has cached it
                    ProfileResponse cached;
                    synchronized (responseCache) {
                        cached = responseCache.get(uuid);
                    }
                    if (cached != null) {
                        synchronizeCallbackProfile(callback, cached);
                        return;
                    }

                    // Check for a previously downloaded profile
                    // These were updated in 10.10.1 to include a serialized data property in place of the json profile
                    // If the playerProfile is not set, we will need to update the serialized cache file.
                    File cacheFolder = new File(platform.getPlugin().getDataFolder(), "data/profiles");
                    if (!cacheFolder.exists()) {
                        cacheFolder.mkdirs();
                    }
                    final File playerCache = new File(cacheFolder, uuid + ".yml");
                    if (playerCache.exists()) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerCache);
                        ProfileResponse fromCache = new ProfileResponse(config);
                        // Check for updated data that contains player profile
                        if (isComplete(fromCache.getPlayerProfile())) {
                            synchronized (responseCache) {
                                responseCache.put(uuid, fromCache);
                            }
                            synchronizeCallbackProfile(callback, fromCache);
                            return;
                        }
                    }

                    // Now we will have to fetch the profile and update the cache
                    PlayerProfile offlineProfile = offlinePlayer.getPlayerProfile();
                    if (!isComplete(offlineProfile)) {
                        if (CompatibilityConstants.DEBUG) {
                            platform.getLogger().info("Fetching offline player data for " + uuid);
                        }

                        try {
                            // We are already in a separate thread so we can update this synchronously
                            offlineProfile = offlineProfile.update().get();
                        } catch (Exception ex) {
                            if (CompatibilityConstants.DEBUG) {
                                platform.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid + ", will not retry for 10 minutes", ex);
                            } else {
                                platform.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid + ", will not retry for 10 minutes");
                            }
                            engageHoldoff();
                            synchronizeCallbackProfile(callback, null);
                            return;
                        }
                    }

                    // Check again for a valid skin
                    if (!isComplete(offlineProfile)) {
                        platform.getLogger().log(Level.WARNING, "Got an empty player profile for: " + uuid + ", will not retry for 10 minutes");
                        engageHoldoff();
                        synchronizeCallbackProfile(callback, null);
                        return;
                    }

                    // Update cache
                    platform.getLogger().info("Got skin URL: " + offlineProfile.getTextures().getSkin() + " for " + offlineProfile.getName());
                    ProfileResponse response = new ProfileResponse(offlineProfile);
                    synchronized (responseCache) {
                        responseCache.put(uuid, response);
                    }
                    try {
                        YamlConfiguration saveToCache = new YamlConfiguration();
                        response.save(saveToCache);
                        saveToCache.save(playerCache);
                    } catch (Exception ex) {
                        platform.getLogger().log(Level.WARNING, "Failed to save player profile to cache for: " + uuid, ex);
                    }
                    synchronizeCallbackProfile(callback, response);
                    holdoff = 0;
                }
            }
        }, holdoff / 50);
    }

    private boolean isComplete(PlayerProfile playerProfile) {
        // The Spigot implementation of this looks correct, but when running on Paper
        // it seems to do something different and will return true for an unloaded profile.
        // return playerProfile.isComplete()
        return playerProfile != null && playerProfile.getUniqueId() != null
                && playerProfile.getName() != null && !playerProfile.getTextures().isEmpty();
    }

    public String toDisguiseFormat(PlayerProfile profile) {
        Gson gson = getGson();
        String bukkitFormat = gson.toJson(profile);
        Type objectMapType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> bukkitMap = gson.fromJson(bukkitFormat, objectMapType);
        Map<String, Object> disguiseMap = new HashMap<>();
        disguiseMap.put("uuid", profile.getUniqueId());
        disguiseMap.put("name", profile.getName());
        disguiseMap.put("textureProperties", bukkitMap.get("properties"));
        return gson.toJson(disguiseMap);
    }
}
