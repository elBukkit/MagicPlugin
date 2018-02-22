package com.elmakers.mine.bukkit.utility;

import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;

public class SkinUtils extends NMSUtils {
    private static long holdoff = 0;
    private static boolean DEBUG = false;
    
    public static class ProfileResponse {
        private final String uuid;
        private final String skinURL;
        private final String profileJSON;

        private ProfileResponse(String uuid, String skinURL, String profileJSON) {
            this.uuid = uuid;
            this.skinURL = skinURL;
            this.profileJSON = profileJSON;
        }

        public String getUUID() {
            return uuid;
        }

        public String getSkinURL() {
            return skinURL;
        }

        public String getProfileJSON() {
            return profileJSON;
        }
    }
    
    public interface ProfileCallback {
        void result(ProfileResponse response);
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

                // Probably should just use gson here .... 
                String token1 = "textures:{SKIN:{url:\"";
                String token2 = "\"textures\":{\"SKIN\":{\"url\":\"";

                int start = decoded.indexOf(token1);
                int length = token1.length();
                if (start < 0) {
                    start = decoded.indexOf(token2);
                    length = token2.length();
                    if (start < 0) {
                        return null;
                    }
                }
                decoded = decoded.substring(start + length);
                int end = decoded.indexOf("\"}");
                if (end < 0) {
                    return null;
                }
                url = decoded.substring(0, end).trim();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return url;
    }

    public static String getOnlineSkinURL(Player player) {
        if (class_CraftPlayer_getProfileMethod == null) return null;
        try {
            Object profile = class_CraftPlayer_getProfileMethod.invoke(player);
            if (profile == null) return null;
            return getProfileURL(profile);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static String getOnlineSkinURL(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
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
        InputStream in = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String inputLine = "";
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        return response.toString();
    }
    
    private static String parseBetween(String s, String startToken, String endToken) {
        int start = s.indexOf(startToken);
        if (start < 0) {
            return null;
        }
        s = s.substring(start + startToken.length());
        int end = s.indexOf(endToken);
        if (end < 0) {
            return null;
        }
        return s.substring(0, end);
    }
    
    private static void engageHoldoff() {
        holdoff = 10 * 60000;
    }
    
    public static void fetchProfile(final Plugin plugin, final String playerName, final ProfileCallback callback) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getLogger().info("Fetching profile for " + playerName);
                try {
                    String uuidJSON = fetchURL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                    if (uuidJSON.isEmpty()) {
                        callback.result(null);
                        engageHoldoff();
                        if (DEBUG) plugin.getLogger().warning("Got empty UUID JSON");
                        return;
                    }
                    
                    // See above .. probably should use GSon. Not really sure if it's cross-version though. Shrug.
                    String uuid = parseBetween(uuidJSON, "\"id\":\"", "\"");
                    if (uuid == null) {
                        callback.result(null);
                        engageHoldoff();
                        if (DEBUG) plugin.getLogger().warning("Failed to parse UUID JSON");
                        return;
                    }
                    if (DEBUG) plugin.getLogger().info("Got UUID: " + uuid);

                    String profileJSON = fetchURL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
                    if (profileJSON.isEmpty()) {
                        callback.result(null);
                        engageHoldoff();
                        if (DEBUG) plugin.getLogger().warning("Failed to fetch profile JSON");
                        return;
                    }
                    if (DEBUG) plugin.getLogger().info("Got profile: " + profileJSON);
                    
                    String encodedTextures = parseBetween(profileJSON, "\"name\":\"textures\",\"value\":\"", "\"");
                    if (encodedTextures == null) {
                        callback.result(null);
                        engageHoldoff();
                        if (DEBUG) plugin.getLogger().warning("Failed to parse profile JSON");
                        return;
                    }
                    String decodedTextures = Base64Coder.decodeString(encodedTextures);
                    if (DEBUG) plugin.getLogger().info("Decoded textures: " + decodedTextures);
                    String skinURL = parseBetween(decodedTextures, "\"url\":\"", "\"");
                    if (skinURL == null) {
                        callback.result(null);
                        engageHoldoff();
                        if (DEBUG) plugin.getLogger().warning("Failed to parse textures JSON");
                        return;
                    }
                    if (DEBUG) plugin.getLogger().info("Got skin URL: " + skinURL);
                    callback.result(new ProfileResponse(uuid, skinURL, profileJSON));
                    holdoff = 0;
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Failed to fetch profile for: " + playerName);
                    engageHoldoff();
                    callback.result(null);
                }
            }
        }, holdoff);
    }
}
