package com.elmakers.mine.bukkit.utility.platform.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.ProfileCallback;
import com.elmakers.mine.bukkit.utility.ProfileResponse;
import com.elmakers.mine.bukkit.utility.UUIDCallback;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.SkinUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class SkinUtilsBase implements SkinUtils {
    protected final Platform platform;
    protected final Map<UUID, ProfileResponse> responseCache = new HashMap<>();
    protected final Map<String, UUID> uuidCache = new HashMap<>();
    protected final Map<String, Object> loadingUUIDs = new HashMap<>();
    protected final Map<UUID, Object> loadingProfiles = new HashMap<>();
    protected Gson gson;
    protected long holdoff = 0;

    protected SkinUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    @Override
    public String getTextureURL(String texturesJson) {
        String url = null;
        JsonElement element = new JsonParser().parse(texturesJson);
        if (element != null && element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonObject texturesObject = object.getAsJsonObject("textures");
            if (texturesObject != null && texturesObject.has("SKIN")) {
                JsonObject skin = texturesObject.getAsJsonObject("SKIN");
                if (skin != null && skin.has("url")) {
                    url = skin.get("url").getAsString();
                }
            }
        }
        return url;
    }

    @Override
    public String getOnlineSkinURL(Player player) {
        Object profile = getProfile(player);
        return profile == null ? null : getProfileURL(profile);
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

    private String fetchURL(String urlString) throws IOException {
        StringBuffer response = new StringBuffer();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);
        InputStream in = null;
        try {
            in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            String inputLine = "";
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return response.toString();
    }

    private void engageHoldoff() {
        holdoff = 10 * 60000;
    }

    private void synchronizeCallbackUUID(final UUIDCallback callback, final UUID uuid) {
        Bukkit.getScheduler().runTask(platform.getPlugin(), new Runnable() {
            @Override
            public void run() {
                callback.result(uuid);
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

    @Override
    public void fetchUUID(final String playerName, final UUIDCallback callback) {
        final Player onlinePlayer = platform.getDeprecatedUtils().getPlayerExact(playerName);
        if (onlinePlayer != null) {
            final UUID uuid = onlinePlayer.getUniqueId();
            boolean contains;
            synchronized (uuidCache) {
                contains = uuidCache.containsKey(playerName);
                if (!contains) {
                    uuidCache.put(playerName, onlinePlayer.getUniqueId());
                }
            }
            if (!contains) {
                Bukkit.getScheduler().runTaskAsynchronously(platform.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        File cacheFolder = new File(platform.getPlugin().getDataFolder(), "data/profiles");
                        if (!cacheFolder.exists()) {
                            cacheFolder.mkdirs();
                        }

                        try {
                            File playerCache = new File(cacheFolder, playerName + ".yml");
                            YamlConfiguration config = new YamlConfiguration();
                            config.set("uuid", uuid.toString());
                            config.save(playerCache);
                        } catch (IOException ex) {
                            platform.getLogger().log(Level.WARNING, "Error saving to player UUID cache", ex);
                        }
                    }
                });
            }
            callback.result(onlinePlayer.getUniqueId());
            return;
        }

        UUID cached;
        synchronized (uuidCache) {
            cached = uuidCache.get(playerName);
        }
        if (cached != null) {
            callback.result(cached);
            return;
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(platform.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Object lock;
                synchronized (loadingUUIDs) {
                    lock = loadingUUIDs.get(playerName);
                    if (lock == null) {
                        lock = new Object();
                        loadingUUIDs.put(playerName, lock);
                    }
                }
                synchronized (lock) {
                    UUID cached;
                    synchronized (uuidCache) {
                        cached = uuidCache.get(playerName);
                    }
                    if (cached != null) {
                        callback.result(cached);
                        return;
                    }
                    File cacheFolder = new File(platform.getPlugin().getDataFolder(), "data/profiles");
                    if (!cacheFolder.exists()) {
                        cacheFolder.mkdirs();
                    }

                    UUID uuid;
                    final File playerCache = new File(cacheFolder, playerName + ".yml");
                    try {
                        if (playerCache.exists()) {
                            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerCache);
                            uuid = UUID.fromString(config.getString("uuid"));
                        } else {
                            String uuidJSON = fetchURL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                            if (uuidJSON.isEmpty()) {
                                if (CompatibilityConstants.DEBUG)
                                    platform.getLogger().warning("Got empty UUID JSON for " + playerName);
                                synchronizeCallbackUUID(callback, null);
                                return;
                            }

                            String uuidString = null;
                            JsonElement element = new JsonParser().parse(uuidJSON);
                            if (element != null && element.isJsonObject()) {
                                uuidString = element.getAsJsonObject().get("id").getAsString();
                            }
                            if (uuidString == null) {
                                engageHoldoff();
                                if (CompatibilityConstants.DEBUG)
                                    platform.getLogger().warning("Failed to parse UUID JSON for " + playerName + ", will not retry for 10 minutes");
                                synchronizeCallbackUUID(callback, null);
                                return;
                            }
                            if (CompatibilityConstants.DEBUG)
                                platform.getLogger().info("Got UUID: " + uuidString + " for " + playerName);
                            uuid = UUID.fromString(addDashes(uuidString));

                            YamlConfiguration config = new YamlConfiguration();
                            config.set("uuid", uuid.toString());
                            config.save(playerCache);
                        }

                        synchronized (uuidCache) {
                            uuidCache.put(playerName, uuid);
                        }
                    } catch (Exception ex) {
                        if (CompatibilityConstants.DEBUG) {
                            platform.getLogger().log(Level.WARNING, "Failed to fetch UUID for: " + playerName + ", will not retry for 10 minutes", ex);
                        } else {
                            platform.getLogger().log(Level.WARNING, "Failed to fetch UUID for: " + playerName + ", will not retry for 10 minutes");
                        }
                        engageHoldoff();
                        uuid = null;
                    }

                    synchronizeCallbackUUID(callback, uuid);
                }
            }
        }, holdoff / 50);
    }

    private String addDashes(String uuidString) {
        StringBuilder builder = new StringBuilder(uuidString);
        for (int i = 8, j = 0; i <= 20; i += 4, j++)
            builder.insert(i + j, '-');
        return builder.toString();
    }

    @Override
    public void fetchProfile(final String playerName, final ProfileCallback callback) {
        fetchUUID(playerName, new UUIDCallback() {
            @Override
            public void result(UUID uuid) {
                if (uuid != null) {
                    fetchProfile(uuid, callback);
                } else {
                    callback.result(null);
                }
            }
        });
    }

    @Override
    public void fetchProfile(final UUID uuid, final ProfileCallback callback) {
        final Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            boolean contains;
            final ProfileResponse response = new ProfileResponse(this, platform.getLogger(), onlinePlayer);
            synchronized (responseCache) {
                contains = responseCache.containsKey(uuid);
                if (!contains) {
                    responseCache.put(uuid, response);
                }
            }
            if (!contains) {
                Bukkit.getScheduler().runTaskAsynchronously(platform.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        File cacheFolder = new File(platform.getPlugin().getDataFolder(), "data/profiles");
                        if (!cacheFolder.exists()) {
                            cacheFolder.mkdirs();
                        }

                        try {
                            File playerCache = new File(cacheFolder, uuid + ".yml");
                            YamlConfiguration config = new YamlConfiguration();
                            response.save(config);
                            config.save(playerCache);
                        } catch (IOException ex) {
                            platform.getLogger().log(Level.WARNING, "Error saving to player profile cache", ex);
                        }
                    }
                });
            }
            callback.result(response);
            return;
        }

        ProfileResponse cached;
        synchronized (responseCache) {
            cached = responseCache.get(uuid);
        }
        if (cached != null) {
            callback.result(cached);
            return;
        }
        final com.elmakers.mine.bukkit.utility.platform.SkinUtils skinUtils = this;
        Bukkit.getScheduler().runTaskLaterAsynchronously(platform.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Object lock;
                synchronized (loadingUUIDs) {
                    lock = loadingProfiles.get(uuid);
                    if (lock == null) {
                        lock = new Object();
                        loadingProfiles.put(uuid, lock);
                    }
                }
                synchronized (lock) {
                    ProfileResponse cached;
                    synchronized (responseCache) {
                        cached = responseCache.get(uuid);
                    }
                    if (cached != null) {
                        callback.result(cached);
                        return;
                    }
                    File cacheFolder = new File(platform.getPlugin().getDataFolder(), "data/profiles");
                    if (!cacheFolder.exists()) {
                        cacheFolder.mkdirs();
                    }
                    final File playerCache = new File(cacheFolder, uuid + ".yml");
                    if (playerCache.exists()) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerCache);
                        ProfileResponse fromCache = new ProfileResponse(skinUtils, config);
                        synchronized (responseCache) {
                            responseCache.put(uuid, fromCache);
                        }
                        synchronizeCallbackProfile(callback, fromCache);
                        return;
                    }

                    if (CompatibilityConstants.DEBUG) {
                        platform.getLogger().info("Fetching profile for " + uuid);
                    }
                    try {
                        String profileJSON = fetchURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
                        if (profileJSON.isEmpty()) {
                            synchronizeCallbackProfile(callback, null);
                            engageHoldoff();
                            if (CompatibilityConstants.DEBUG)
                                platform.getLogger().warning("Failed to fetch profile JSON for " + uuid + ", will not retry for 10 minutes");
                            return;
                        }
                        if (CompatibilityConstants.DEBUG) platform.getLogger().info("Got profile: " + profileJSON);
                        JsonElement element = new JsonParser().parse(profileJSON);
                        if (element == null || !element.isJsonObject()) {
                            synchronizeCallbackProfile(callback, null);
                            engageHoldoff();
                            if (CompatibilityConstants.DEBUG)
                                platform.getLogger().warning("Failed to parse profile JSON for " + uuid + ", will not retry for 10 minutes");
                            return;
                        }

                        JsonObject profileJson = element.getAsJsonObject();
                        JsonArray properties = profileJson.getAsJsonArray("properties");
                        String encodedTextures = null;
                        for (int i = 0; i < properties.size(); i++) {
                            JsonElement property = properties.get(i);
                            if (property.isJsonObject()) {
                                JsonObject objectProperty = property.getAsJsonObject();
                                if (objectProperty.has("name") && objectProperty.has("value")) {
                                    if (objectProperty.get("name").getAsString().equals("textures")) {
                                        encodedTextures = objectProperty.get("value").getAsString();
                                        break;
                                    }
                                }
                            }
                        }

                        if (encodedTextures == null) {
                            synchronizeCallbackProfile(callback, null);
                            engageHoldoff();
                            if (CompatibilityConstants.DEBUG)
                                platform.getLogger().warning("Failed to find textures in profile JSON, will not retry for 10 minutes");
                            return;
                        }
                        String decodedTextures = Base64Coder.decodeString(encodedTextures);
                        if (CompatibilityConstants.DEBUG)
                            platform.getLogger().info("Decoded textures: " + decodedTextures);
                        String skinURL = getTextureURL(decodedTextures);

                        // A null skin URL here is normal if the player has no skin.
                        if (CompatibilityConstants.DEBUG)
                            platform.getLogger().info("Got skin URL: " + skinURL + " for " + profileJson.get("name").getAsString());
                        ProfileResponse response = new ProfileResponse(skinUtils, uuid, profileJson.get("name").getAsString(), skinURL, profileJSON);
                        synchronized (responseCache) {
                            responseCache.put(uuid, response);
                        }
                        YamlConfiguration saveToCache = new YamlConfiguration();
                        response.save(saveToCache);
                        saveToCache.save(playerCache);
                        synchronizeCallbackProfile(callback, response);
                        holdoff = 0;
                    } catch (Exception ex) {
                        if (CompatibilityConstants.DEBUG) {
                            platform.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid + ", will not retry for 10 minutes", ex);
                        } else {
                            platform.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid + ", will not retry for 10 minutes");
                        }
                        engageHoldoff();
                        synchronizeCallbackProfile(callback, null);
                    }
                }
            }
        }, holdoff / 50);
    }
}
