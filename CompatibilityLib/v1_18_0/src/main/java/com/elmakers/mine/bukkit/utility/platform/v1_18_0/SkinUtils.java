package com.elmakers.mine.bukkit.utility.platform.v1_18_0;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.platform.base_v1_17_0.SkinUtilsBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
            String texture = getValue(textureProperty);
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
                String value = getValue(entry.getValue());
                newObject.addProperty("value", value);
                String signature = getSignature(entry.getValue());
                newObject.addProperty("signature", signature);
                propertiesArray.add(newObject);
            }
            profileObject.add("properties", propertiesArray);
        }
        return profileJson;
    }
}
