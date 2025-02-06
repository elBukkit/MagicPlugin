package com.elmakers.mine.bukkit.utility.platform.modern2;

import java.net.URL;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.InventoryUtilsBase;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public abstract class Modern2InventoryUtils extends InventoryUtilsBase {
    public Modern2InventoryUtils(Platform platform) {
        super(platform);
    }

    @Override
    public ItemStack setSkullURL(ItemStack itemStack, URL url, UUID id, String name) {
        try {
            if (platform.getItemUtils().isEmpty(itemStack)) {
                return itemStack;
            }

            GameProfile gameProfile = new GameProfile(id, name);
            PropertyMap properties = gameProfile.getProperties();
            if (properties == null) {
                return itemStack;
            }
            itemStack = platform.getItemUtils().makeReal(itemStack);
            if (platform.getItemUtils().isEmpty(itemStack)) {
                return itemStack;
            }

            String textureJSON = "{textures:{SKIN:{url:\"" + url + "\"}}}";
            String encoded = Base64Coder.encodeString(textureJSON);

            Property newProperty = new Property("textures", encoded);
            properties.put("textures", newProperty);

            ItemMeta skullMeta = itemStack.getItemMeta();
            setSkullProfile(skullMeta, gameProfile);

            itemStack.setItemMeta(skullMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return itemStack;
    }

    @Override
    public boolean isSkull(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta instanceof SkullMeta;
    }

    @Override
    public Object getSkullProfile(ItemMeta itemMeta) {
        if (itemMeta == null || !(itemMeta instanceof SkullMeta)) return null;
        return ReflectionUtils.getPrivate(platform.getLogger(), itemMeta, itemMeta.getClass(), "profile");
    }

    @Override
    public Object getSkullProfile(Skull state) {
        return ReflectionUtils.getPrivate(platform.getLogger(), state, state.getClass(), "profile");
    }

    @Override
    public boolean setSkullProfile(Skull state, Object data) {
        return ReflectionUtils.setPrivate(platform.getLogger(), state, state.getClass(), "profile", data);
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getMapId(ItemStack mapItem) {
        ItemMeta meta = mapItem.getItemMeta();
        if (meta instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta)meta;
            // Why is this deprecated if there is no alternative?
            return mapMeta.getMapId();
        }
        return 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setMapId(ItemStack mapItem, int id) {
        ItemMeta meta = mapItem.getItemMeta();
        if (meta instanceof MapMeta) {
            MapMeta mapMeta = (MapMeta)meta;
            // Why is this deprecated if there is no alternative?
            mapMeta.setMapId(id);
            mapItem.setItemMeta(mapMeta);
        }
    }

    @Override
    public ItemStack createMap(Material material, int mapId) {
        ItemStack mapItem = new ItemStack(material, 1);
        setMapId(mapItem, mapId);
        return mapItem;
    }
}
