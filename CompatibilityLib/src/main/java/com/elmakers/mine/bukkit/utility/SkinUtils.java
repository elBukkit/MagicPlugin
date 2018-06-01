package com.elmakers.mine.bukkit.utility;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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

public class SkinUtils extends NMSUtils {
    private static Plugin plugin;
    private static Gson gson;
    private static long holdoff = 0;
    private static boolean DEBUG = false;
    private static final Map<UUID, ProfileResponse> responseCache = new HashMap<>();
    private static final Map<String, UUID> uuidCache = new HashMap<>();
    private static final Map<String, Object> loadingUUIDs = new HashMap<>();
    private static final Map<UUID, Object> loadingProfiles = new HashMap<>();
    
    public static class ProfileResponse {
        private final UUID uuid;
        private final String playerName;
        private final String skinURL;
        private final String profileJSON;

        private ProfileResponse(UUID uuid, String playerName, String skinURL, String profileJSON) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.skinURL = skinURL;
            this.profileJSON = profileJSON;
        }

        private ProfileResponse(ConfigurationSection configuration) {
            this.uuid = UUID.fromString(configuration.getString("uuid"));
            this.playerName = configuration.getString("name");
            this.skinURL = configuration.getString("skin");
            this.profileJSON = configuration.getString("profile");
        }

        private ProfileResponse(Player onlinePlayer) {
            this.uuid = onlinePlayer.getUniqueId();
            Object gameProfile = getProfile(onlinePlayer);
            JsonElement profileJson = getGson().toJsonTree(gameProfile);
            if (profileJson.isJsonObject()) {
                JsonObject profileObject = (JsonObject)profileJson;
                try {
                    @SuppressWarnings("unchecked")
                    Multimap<String, Object> properties = (Multimap<String, Object>)class_GameProfile_properties.get(gameProfile);
                    JsonArray propertiesArray = new JsonArray();

                    for (Map.Entry<String, Object> entry : properties.entries()) {
                        JsonObject newObject = new JsonObject();
                        newObject.addProperty("name", entry.getKey());
                        String value = (String)class_GameProfileProperty_value.get(entry.getValue());
                        newObject.addProperty("value", value);
                        String signature = (String)class_GameProfileProperty_signature.get(entry.getValue());
                        newObject.addProperty("signature", signature);
                        propertiesArray.add(newObject);
                    }
                    profileObject.add("properties", propertiesArray);
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Error serializing profile for " + onlinePlayer.getName(), ex);
                }
            }

            this.profileJSON = getGson().toJson(profileJson);
            this.skinURL = getProfileURL(gameProfile);
            this.playerName = onlinePlayer.getName();
        }

        private void save(ConfigurationSection configuration) {
            configuration.set("uuid", uuid.toString());
            configuration.set("skin", skinURL);
            configuration.set("profile", profileJSON);
            configuration.set("name", playerName);
        }

        public UUID getUUID() {
            return uuid;
        }

        public String getSkinURL() {
            return skinURL;
        }

        public String getProfileJSON() {
            return profileJSON;
        }

        public Object getGameProfile() {
            Object gameProfile = null;
            try {
                gameProfile = class_GameProfile_constructor.newInstance(uuid, playerName);
                @SuppressWarnings("unchecked")
                Multimap<String, Object> properties = (Multimap<String, Object>)class_GameProfile_properties.get(gameProfile);
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
                                Object newProperty = class_GameProfileProperty_constructor.newInstance(name, value, signature);
                                properties.put(name, newProperty);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Error creating GameProfile", ex);
            }
            if (DEBUG) {
                plugin.getLogger().info("Got profile: " + gameProfile);
                plugin.getLogger().info(getProfileURL(gameProfile));
            }
            return gameProfile;
        }
    }
    
    public interface ProfileCallback {
        void result(ProfileResponse response);
    }

    public interface UUIDCallback {
        void result(UUID response);
    }

    public static void initialize(Plugin owner) {
        plugin = owner;
    }

    private static Gson getGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }

    public static String getTextureURL(String texturesJson) {
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
    
    public static String getProfileURL(Object profile)
    {
        String url = null;
        if (profile == null) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            Multimap<String, Object> properties = (Multimap<String, Object>)class_GameProfile_properties.get(profile);
            Collection<Object> textures = properties.get("textures");
            if (textures != null && textures.size() > 0)
            {
                Object textureProperty = textures.iterator().next();
                String texture = (String)class_GameProfileProperty_value.get(textureProperty);
                String decoded = Base64Coder.decodeString(texture);
                url = getTextureURL(decoded);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return url;
    }

    private static Object getProfile(Player player) {
        if (class_CraftPlayer_getProfileMethod == null) return null;
        try {
            return class_CraftPlayer_getProfileMethod.invoke(player);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getOnlineSkinURL(Player player) {
        Object profile = getProfile(player);
        return profile == null ? null : getProfileURL(profile);
    }

    public static String getOnlineSkinURL(String playerName) {
        Player player = DeprecatedUtils.getPlayerExact(playerName);
        String url = null;
        if (player != null) {
            url = getOnlineSkinURL(player);
        }
        return url;
    }
    
    private static String fetchURL(String urlString) throws IOException {
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
    
    private static void engageHoldoff() {
        holdoff = 10 * 60000;
    }

    private static void synchronizeCallback(final UUIDCallback callback, final UUID uuid) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                callback.result(uuid);
            }
        });
    }

    private static void synchronizeCallback(final ProfileCallback callback, final ProfileResponse response) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                callback.result(response);
            }
        });
    }

    public static void fetchUUID(final String playerName, final UUIDCallback callback) {
        final Player onlinePlayer = DeprecatedUtils.getPlayerExact(playerName);
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
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        File cacheFolder = new File(plugin.getDataFolder(), "data/profiles");
                        if (!cacheFolder.exists()) {
                            cacheFolder.mkdirs();
                        }

                        try{
                            File playerCache = new File(cacheFolder, playerName + ".yml");
                            YamlConfiguration config = new YamlConfiguration();
                            config.set("uuid", uuid.toString());
                            config.save(playerCache);
                        } catch (IOException ex) {
                            plugin.getLogger().log(Level.WARNING, "Error saving to player UUID cache", ex);
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
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
                    File cacheFolder = new File(plugin.getDataFolder(), "data/profiles");
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
                                engageHoldoff();
                                if (DEBUG) plugin.getLogger().warning("Got empty UUID JSON for " + playerName);
                                synchronizeCallback(callback, null);
                                return;
                            }

                            String uuidString = null;
                            JsonElement element = new JsonParser().parse(uuidJSON);
                            if (element != null && element.isJsonObject()) {
                                uuidString = element.getAsJsonObject().get("id").getAsString();
                            }
                            if (uuidString == null) {
                                engageHoldoff();
                                if (DEBUG) plugin.getLogger().warning("Failed to parse UUID JSON for " + playerName);
                                synchronizeCallback(callback, null);
                                return;
                            }
                            if (DEBUG) plugin.getLogger().info("Got UUID: " + uuidString + " for " + playerName);
                            uuid = UUID.fromString(addDashes(uuidString));

                            YamlConfiguration config = new YamlConfiguration();
                            config.set("uuid", uuid.toString());
                            config.save(playerCache);
                        }

                        synchronized (uuidCache) {
                            uuidCache.put(playerName, uuid);
                        }
                    } catch (Exception ex) {
                        if (DEBUG) {
                            plugin.getLogger().log(Level.WARNING, "Failed to fetch UUID for: " + playerName, ex);
                        } else {
                            plugin.getLogger().log(Level.WARNING, "Failed to fetch UUID for: " + playerName);
                        }
                        engageHoldoff();
                        uuid = null;
                    }

                    synchronizeCallback(callback, uuid);
                }
            }
         }, holdoff);
    }

    private static String addDashes(String uuidString) {
        StringBuilder builder = new StringBuilder(uuidString);
        for(int i=8, j=0; i<=20; i+=4, j++)
            builder.insert(i+j, '-');
        return builder.toString();
    }

    public static void fetchProfile(final String playerName, final ProfileCallback callback) {
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
    
    public static void fetchProfile(final UUID uuid, final ProfileCallback callback) {
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
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        File cacheFolder = new File(plugin.getDataFolder(), "data/profiles");
                        if (!cacheFolder.exists()) {
                            cacheFolder.mkdirs();
                        }

                        try{
                            File playerCache = new File(cacheFolder, uuid + ".yml");
                            YamlConfiguration config = new YamlConfiguration();
                            response.save(config);
                            config.save(playerCache);
                        } catch (IOException ex) {
                            plugin.getLogger().log(Level.WARNING, "Error saving to player profile cache", ex);
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
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
                    File cacheFolder = new File(plugin.getDataFolder(), "data/profiles");
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
                        synchronizeCallback(callback, fromCache);
                        return;
                    }

                    if (DEBUG) {
                        plugin.getLogger().info("Fetching profile for " + uuid);
                    }
                    try {
                        String profileJSON = fetchURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
                        if (profileJSON.isEmpty()) {
                            synchronizeCallback(callback, null);
                            engageHoldoff();
                            if (DEBUG) plugin.getLogger().warning("Failed to fetch profile JSON for " + uuid);
                            return;
                        }
                        if (DEBUG) plugin.getLogger().info("Got profile: " + profileJSON);
                        JsonElement element = new JsonParser().parse(profileJSON);
                        if (element == null || !element.isJsonObject()) {
                            synchronizeCallback(callback, null);
                            engageHoldoff();
                            if (DEBUG) plugin.getLogger().warning("Failed to parse profile JSON for " + uuid);
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
                            synchronizeCallback(callback, null);
                            engageHoldoff();
                            if (DEBUG) plugin.getLogger().warning("Failed to find textures in profile JSON");
                            return;
                        }
                        String decodedTextures = Base64Coder.decodeString(encodedTextures);
                        if (DEBUG) plugin.getLogger().info("Decoded textures: " + decodedTextures);
                        String skinURL = getTextureURL(decodedTextures);

                        // A null skin URL here is normal if the player has no skin.
                        if (DEBUG) plugin.getLogger().info("Got skin URL: " + skinURL + " for " + profileJson.get("name").getAsString());
                        ProfileResponse response = new ProfileResponse(uuid, profileJson.get("name").getAsString(), skinURL, profileJSON);
                        synchronized (responseCache) {
                            responseCache.put(uuid, response);
                        }
                        YamlConfiguration saveToCache = new YamlConfiguration();
                        response.save(saveToCache);
                        saveToCache.save(playerCache);
                        synchronizeCallback(callback, response);
                        holdoff = 0;
                    } catch (Exception ex) {
                        if (DEBUG) {
                            plugin.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid, ex);
                        } else {
                            plugin.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + uuid);
                        }
                        engageHoldoff();
                        synchronizeCallback(callback, null);
                    }
                }
            }
        }, holdoff);
    }
}
