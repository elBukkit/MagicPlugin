package com.elmakers.mine.bukkit.utility.platform;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ProfileCallback;
import com.elmakers.mine.bukkit.utility.ProfileResponse;
import com.elmakers.mine.bukkit.utility.UUIDCallback;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SkinUtils {
    public boolean DEBUG = false;

    private Plugin plugin;
    private Gson gson;
    private long holdoff = 0;
    private static final Map<UUID, ProfileResponse> responseCache = new HashMap<>();
    private static final Map<String, UUID> uuidCache = new HashMap<>();
    private static final Map<String, Object> loadingUUIDs = new HashMap<>();
    private static final Map<UUID, Object> loadingProfiles = new HashMap<>();

    public void initialize(Plugin owner) {
        SkinUtils.this.plugin = owner;
    }

    public Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

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
    
    public String getProfileURL(Object profile)
    {
        String url = null;
        if (profile == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Multimap<String, Object> properties = (Multimap<String, Object>) NMSUtils.class_GameProfile_properties.get(profile);
            Collection<Object> textures = properties.get("textures");
            if (textures != null && textures.size() > 0)
            {
                Object textureProperty = textures.iterator().next();
                String texture = (String) NMSUtils.class_GameProfileProperty_value.get(textureProperty);
                String decoded = Base64Coder.decodeString(texture);
                url = SkinUtils.this.getTextureURL(decoded);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return url;
    }

    public Object getProfile(Player player) {
        if (NMSUtils.class_CraftPlayer_getProfileMethod == null) return null;
        try {
            return NMSUtils.class_CraftPlayer_getProfileMethod.invoke(player);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public JsonElement getProfileJson(Object gameProfile) throws IllegalAccessException {
        JsonElement profileJson = getGson().toJsonTree(gameProfile);
        if (profileJson.isJsonObject()) {
            JsonObject profileObject = (JsonObject) profileJson;
            @SuppressWarnings("unchecked")
            Multimap<String, Object> properties = (Multimap<String, Object>) NMSUtils.class_GameProfile_properties.get(gameProfile);
            JsonArray propertiesArray = new JsonArray();

            for (Map.Entry<String, Object> entry : properties.entries()) {
                JsonObject newObject = new JsonObject();
                newObject.addProperty("name", entry.getKey());
                String value = (String) NMSUtils.class_GameProfileProperty_value.get(entry.getValue());
                newObject.addProperty("value", value);
                String signature = (String) NMSUtils.class_GameProfileProperty_signature.get(entry.getValue());
                newObject.addProperty("signature", signature);
                propertiesArray.add(newObject);
            }
            profileObject.add("properties", propertiesArray);
        }
        return profileJson;
    }

    public String getOnlineSkinURL(Player player) {
        Object profile = SkinUtils.this.getProfile(player);
        return profile == null ? null : SkinUtils.this.getProfileURL(profile);
    }

    public String getOnlineSkinURL(String playerName) {
        if (playerName.startsWith("http")) return playerName;
        Player player = CompatibilityLib.getDeprecatedUtils().getPlayerExact(playerName);
        String url = null;
        if (player != null) {
            url = SkinUtils.this.getOnlineSkinURL(player);
        }
        return url;
    }
    
    private String fetchURL(String urlString) throws IOException {
        StringBuffer response = new StringBuffer();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
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
        SkinUtils.this.holdoff = 10 * 60000;
    }

    private void synchronizeCallbackUUID(final UUIDCallback callback, final UUID uuid) {
        Bukkit.getScheduler().runTask(SkinUtils.this.plugin, new Runnable() {
            @Override
            public void run() {
                callback.result(uuid);
            }
        });
    }

    private void synchronizeCallbackProfile(final ProfileCallback callback, final ProfileResponse response) {
        Bukkit.getScheduler().runTask(SkinUtils.this.plugin, new Runnable() {
            @Override
            public void run() {
                callback.result(response);
            }
        });
    }

    public void fetchUUID(final String playerName, final UUIDCallback callback) {
        final Player onlinePlayer = CompatibilityLib.getDeprecatedUtils().getPlayerExact(playerName);
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
                Bukkit.getScheduler().runTaskAsynchronously(SkinUtils.this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        File cacheFolder = new File(SkinUtils.this.plugin.getDataFolder(), "data/profiles");
                        if (!cacheFolder.exists()) {
                            cacheFolder.mkdirs();
                        }

                        try{
                            File playerCache = new File(cacheFolder, playerName + ".yml");
                            YamlConfiguration config = new YamlConfiguration();
                            config.set("uuid", uuid.toString());
                            config.save(playerCache);
                        } catch (IOException ex) {
                            CompatibilityLib.getLogger().log(Level.WARNING, "Error saving to player UUID cache", ex);
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(SkinUtils.this.plugin, new Runnable() {
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
                    File cacheFolder = new File(SkinUtils.this.plugin.getDataFolder(), "data/profiles");
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
                            String uuidJSON = SkinUtils.this.fetchURL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                            if (uuidJSON.isEmpty()) {
                                if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().warning("Got empty UUID JSON for " + playerName);
                                SkinUtils.this.synchronizeCallbackUUID(callback, null);
                                return;
                            }

                            String uuidString = null;
                            JsonElement element = new JsonParser().parse(uuidJSON);
                            if (element != null && element.isJsonObject()) {
                                uuidString = element.getAsJsonObject().get("id").getAsString();
                            }
                            if (uuidString == null) {
                                SkinUtils.this.engageHoldoff();
                                if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().warning("Failed to parse UUID JSON for " + playerName + ", will not retry for 10 minutes");
                                SkinUtils.this.synchronizeCallbackUUID(callback, null);
                                return;
                            }
                            if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().info("Got UUID: " + uuidString + " for " + playerName);
                            uuid = UUID.fromString(SkinUtils.this.addDashes(uuidString));

                            YamlConfiguration config = new YamlConfiguration();
                            config.set("uuid", uuid.toString());
                            config.save(playerCache);
                        }

                        synchronized (uuidCache) {
                            uuidCache.put(playerName, uuid);
                        }
                    } catch (Exception ex) {
                        if (SkinUtils.this.DEBUG) {
                            CompatibilityLib.getLogger().log(Level.WARNING, "Failed to fetch UUID for: " + playerName + ", will not retry for 10 minutes", ex);
                        } else {
                            CompatibilityLib.getLogger().log(Level.WARNING, "Failed to fetch UUID for: " + playerName + ", will not retry for 10 minutes");
                        }
                        SkinUtils.this.engageHoldoff();
                        uuid = null;
                    }

                    SkinUtils.this.synchronizeCallbackUUID(callback, uuid);
                }
            }
         }, SkinUtils.this.holdoff / 50);
    }

    private String addDashes(String uuidString) {
        StringBuilder builder = new StringBuilder(uuidString);
        for(int i=8, j=0; i<=20; i+=4, j++)
            builder.insert(i+j, '-');
        return builder.toString();
    }

    public void fetchProfile(final String playerName, final ProfileCallback callback) {
        SkinUtils.this.fetchUUID(playerName, new UUIDCallback() {
            @Override
            public void result(UUID uuid) {
                if (uuid != null) {
                    SkinUtils.this.fetchProfile(uuid, callback);
                } else {
                    callback.result(null);
                }
            }
        });
    }
    
    public void fetchProfile(final UUID uuid, final ProfileCallback callback) {
        final Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            boolean contains;
            final ProfileResponse response = new ProfileResponse(onlinePlayer);
            synchronized (responseCache) {
                contains = responseCache.containsKey(uuid);
                if (!contains) {
                    responseCache.put(uuid, response);
                }
            }
            if (!contains) {
                Bukkit.getScheduler().runTaskAsynchronously(SkinUtils.this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        File cacheFolder = new File(SkinUtils.this.plugin.getDataFolder(), "data/profiles");
                        if (!cacheFolder.exists()) {
                            cacheFolder.mkdirs();
                        }

                        try{
                            File playerCache = new File(cacheFolder, uuid + ".yml");
                            YamlConfiguration config = new YamlConfiguration();
                            response.save(config);
                            config.save(playerCache);
                        } catch (IOException ex) {
                            CompatibilityLib.getLogger().log(Level.WARNING, "Error saving to player profile cache", ex);
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(SkinUtils.this.plugin, new Runnable() {
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
                    File cacheFolder = new File(SkinUtils.this.plugin.getDataFolder(), "data/profiles");
                    if (!cacheFolder.exists()) {
                        cacheFolder.mkdirs();
                    }
                    final File playerCache = new File(cacheFolder, uuid + ".yml");
                    if (playerCache.exists()) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerCache);
                        ProfileResponse fromCache = new ProfileResponse(config);
                        synchronized (responseCache) {
                            responseCache.put(uuid, fromCache);
                        }
                        SkinUtils.this.synchronizeCallbackProfile(callback, fromCache);
                        return;
                    }

                    if (SkinUtils.this.DEBUG) {
                        CompatibilityLib.getLogger().info("Fetching profile for " + uuid);
                    }
                    try {
                        String profileJSON = SkinUtils.this.fetchURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
                        if (profileJSON.isEmpty()) {
                            SkinUtils.this.synchronizeCallbackProfile(callback, null);
                            SkinUtils.this.engageHoldoff();
                            if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().warning("Failed to fetch profile JSON for " + uuid + ", will not retry for 10 minutes");
                            return;
                        }
                        if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().info("Got profile: " + profileJSON);
                        JsonElement element = new JsonParser().parse(profileJSON);
                        if (element == null || !element.isJsonObject()) {
                            SkinUtils.this.synchronizeCallbackProfile(callback, null);
                            SkinUtils.this.engageHoldoff();
                            if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().warning("Failed to parse profile JSON for " + uuid + ", will not retry for 10 minutes");
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
                            SkinUtils.this.synchronizeCallbackProfile(callback, null);
                            SkinUtils.this.engageHoldoff();
                            if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().warning("Failed to find textures in profile JSON, will not retry for 10 minutes");
                            return;
                        }
                        String decodedTextures = Base64Coder.decodeString(encodedTextures);
                        if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().info("Decoded textures: " + decodedTextures);
                        String skinURL = SkinUtils.this.getTextureURL(decodedTextures);

                        // A null skin URL here is normal if the player has no skin.
                        if (SkinUtils.this.DEBUG) CompatibilityLib.getLogger().info("Got skin URL: " + skinURL + " for " + profileJson.get("name").getAsString());
                        ProfileResponse response = new ProfileResponse(uuid, profileJson.get("name").getAsString(), skinURL, profileJSON);
                        synchronized (responseCache) {
                            responseCache.put(uuid, response);
                        }
                        YamlConfiguration saveToCache = new YamlConfiguration();
                        response.save(saveToCache);
                        saveToCache.save(playerCache);
                        SkinUtils.this.synchronizeCallbackProfile(callback, response);
                        SkinUtils.this.holdoff = 0;
                    } catch (Exception ex) {
                        if (SkinUtils.this.DEBUG) {
                            CompatibilityLib.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid + ", will not retry for 10 minutes", ex);
                        } else {
                            CompatibilityLib.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid + ", will not retry for 10 minutes");
                        }
                        SkinUtils.this.engageHoldoff();
                        SkinUtils.this.synchronizeCallbackProfile(callback, null);
                    }
                }
            }
        }, SkinUtils.this.holdoff / 50);
    }

    public Object getGameProfile(UUID uuid, String playerName, String profileJSON) {
        Object gameProfile = null;
        try {
            gameProfile = NMSUtils.class_GameProfile_constructor.newInstance(uuid, playerName);
            @SuppressWarnings("unchecked")
            Multimap<String, Object> properties = (Multimap<String, Object>) NMSUtils.class_GameProfile_properties.get(gameProfile);
            JsonElement json = new JsonParser().parse(profileJSON);
            if (json != null && json.isJsonObject()) {
                JsonObject profile = json.getAsJsonObject();
                if (profile.has("properties")) {
                    JsonArray propertiesJson = profile.getAsJsonArray("properties");
                    for (int i = 0; i < propertiesJson.size(); i++) {
                        JsonObject property = propertiesJson.get(i).getAsJsonObject();
                        if (property != null && property.has("name") && property.has("value")) {
                            String name = property.get("name").getAsString();
                            String value = property.get("value").getAsString();
                            String signature = property.has("signature") ? property.get("signature").getAsString() : null;
                            Object newProperty = NMSUtils.class_GameProfileProperty_constructor.newInstance(name, value, signature);
                            properties.put(name, newProperty);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            CompatibilityLib.getLogger().log(Level.WARNING, "Error creating GameProfile", ex);
        }
        if (DEBUG) {
            CompatibilityLib.getLogger().info("Got profile: " + gameProfile);
            CompatibilityLib.getLogger().info(CompatibilityLib.getSkinUtils().getProfileURL(gameProfile));
        }
        return gameProfile;
    }
}
