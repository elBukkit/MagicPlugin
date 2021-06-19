package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.SkinUtilsBase;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SkinUtils extends SkinUtilsBase {
    public SkinUtils(Platform platform) {
        super(platform);
    }

    @Override
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
                url = getTextureURL(decoded);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return url;
    }

    @Override
    public Object getProfile(Player player) {
        if (NMSUtils.class_CraftPlayer_getProfileMethod == null) return null;
        try {
            return NMSUtils.class_CraftPlayer_getProfileMethod.invoke(player);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
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

    @Override
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
            platform.getLogger().log(Level.WARNING, "Error creating GameProfile", ex);
        }
        if (CompatibilityConstants.DEBUG) {
            platform.getLogger().info("Got profile: " + gameProfile);
            platform.getLogger().info(platform.getSkinUtils().getProfileURL(gameProfile));
        }
        return gameProfile;
    }
}
