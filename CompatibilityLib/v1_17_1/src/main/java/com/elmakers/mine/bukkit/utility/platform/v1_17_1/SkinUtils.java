package com.elmakers.mine.bukkit.utility.platform.v1_17_1;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.SkinUtilsBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public class SkinUtils extends SkinUtilsBase {

    public SkinUtils(Platform platform) {
        super(platform);
    }

    @Override
    public String getProfileURL(Object profile) {
        String url = null;
        if (profile == null || !(profile instanceof GameProfile)) {
            return null;
        }
        GameProfile gameProfile = (GameProfile)profile;
        PropertyMap properties = gameProfile.getProperties();
        if (properties == null) {
            return null;
        }
        Collection<Property> textures = properties.get("textures");
        if (textures != null && textures.size() > 0) {
            Property textureProperty = textures.iterator().next();
            String texture = textureProperty.getValue();
            try {
                String decoded = Base64Coder.decodeString(texture);
                url = getTextureURL(decoded);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Could not parse textures in profile", ex);
            }
        }
        return url;
    }

    @Override
    public Object getProfile(Player player) {
        return ((CraftPlayer)player).getProfile();
    }

    @Override
    public JsonElement getProfileJson(Object profile) {
        if (!(profile instanceof GameProfile)) return null;
        GameProfile gameProfile = (GameProfile)profile;
        JsonElement profileJson = getGson().toJsonTree(gameProfile);
        if (profileJson.isJsonObject()) {
            JsonObject profileObject = (JsonObject) profileJson;
            PropertyMap properties = gameProfile.getProperties();
            JsonArray propertiesArray = new JsonArray();

            for (Map.Entry<String, Property> entry : properties.entries()) {
                JsonObject newObject = new JsonObject();
                newObject.addProperty("name", entry.getKey());
                String value = entry.getValue().getValue();
                newObject.addProperty("value", value);
                String signature = entry.getValue().getSignature();
                newObject.addProperty("signature", signature);
                propertiesArray.add(newObject);
            }
            profileObject.add("properties", propertiesArray);
        }
        return profileJson;
    }

    @Override
    public Object getGameProfile(UUID uuid, String playerName, String profileJSON) {
        GameProfile gameProfile = null;
        try {
            gameProfile = new GameProfile(uuid, playerName);
            PropertyMap properties = gameProfile.getProperties();
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
                            Property newProperty = new Property(name, value, signature);
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
