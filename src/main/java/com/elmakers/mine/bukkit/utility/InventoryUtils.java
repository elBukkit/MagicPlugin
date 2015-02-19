package com.elmakers.mine.bukkit.utility;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class InventoryUtils extends NMSUtils
{	
    public static boolean saveTagsToNBT(ConfigurationSection tags, Object node, String[] tagNames)
    {
        if (node == null) {
            Bukkit.getLogger().warning("Trying to save tags to a null node");
            return false;
        }
        if (!class_NBTTagCompound.isAssignableFrom(node.getClass())) {
            Bukkit.getLogger().warning("Trying to save tags to a non-CompoundTag");
            return false;
        }
        for (String tagName : tagNames)
        {
            String value = tags.getString(tagName);
            // This is kinda hacky, but makes for generally cleaner data.
            if (value == null || value.length() == 0 || value.equals("0") || value.equals("0.0") || value.equals("false")) {
                removeMeta(node, tagName);
            } else {
                setMeta(node, tagName, value);
            }
        }

        return true;
    }

    public static boolean loadTagsFromNBT(ConfigurationSection tags, Object node, String[] tagNames)
    {
        if (node == null) {
            Bukkit.getLogger().warning("Trying to load tags from a null node");
            return false;
        }
        if (!class_NBTTagCompound.isAssignableFrom(node.getClass())) {
            Bukkit.getLogger().warning("Trying to load tags from a non-CompoundTag");
            return false;
        }
        for (String tagName : tagNames)
        {
            String meta = getMeta(node, tagName);
            if (meta != null && meta.length() > 0) {
                ConfigurationUtils.set(tags, tagName, meta);
            }
        }

        return true;
    }

    public static boolean inventorySetItem(Inventory inventory, int index, ItemStack item) {
        try {
            Method setItemMethod = class_CraftInventoryCustom.getMethod("setItem", Integer.TYPE, ItemStack.class);
            setItemMethod.invoke(inventory, index, item);
            return true;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static boolean setInventoryResults(Inventory inventory, ItemStack item) {
        try {
            Method getResultsMethod = inventory.getClass().getMethod("getResultInventory");
            Object inv = getResultsMethod.invoke(inventory);
            Method setItemMethod = inv.getClass().getMethod("setItem", Integer.TYPE, class_ItemStack);
            setItemMethod.invoke(inv, 0, getHandle(item));
            return true;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static ItemStack getURLSkull(String url) {
        // The "MHF_Question" is here so serialization doesn't cause an NPE
        return getURLSkull(url, "MHF_Question", UUID.randomUUID(), null);
    }

    public static ItemStack getURLSkull(URL url) {
        // The "MHF_Question" is here so serialization doesn't cause an NPE
        return getURLSkull(url, "MHF_Question", UUID.randomUUID(), null);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getURLSkull(String url, String ownerName, UUID id, String itemName) {
        try {
            return getURLSkull(new URL(url), ownerName, id, itemName);
        } catch (MalformedURLException e) {
            Bukkit.getLogger().log(Level.WARNING, "Malformed URL: " + url, e);
        }
        return new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)3);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getURLSkull(URL url, String ownerName, UUID id, String itemName) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short)0, (byte)3);
        if (itemName != null) {
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName(itemName);
            skull.setItemMeta(meta);
        }

        try {
            skull = makeReal(skull);
            Object skullOwner = createNode(skull, "SkullOwner");
            setMeta(skullOwner, "Id", id.toString());
            setMeta(skullOwner, "Name", ownerName);
            Object properties = createNode(skullOwner, "Properties");

            Object listMeta = class_NBTTagList.newInstance();
            Object textureNode = class_NBTTagCompound.newInstance();

            String textureJSON = "{textures:{SKIN:{url:\"" + url + "\"}}}";
            String encoded = Base64Coder.encodeString(textureJSON);

            setMeta(textureNode, "Value", encoded);
            class_NBTTagList_addMethod.invoke(listMeta, textureNode);
            class_NBTTagCompound_setMethod.invoke(properties, "textures", listMeta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return skull;
    }

    public static String getProfileURL(Object profile)
    {
        String url = null;
        try {
            Multimap<String, Object> properties = (Multimap<String, Object>)class_GameProfile_properties.get(profile);
            Collection<Object> textures = properties.get("textures");
            if (textures != null && textures.size() > 0)
            {
                Object textureProperty = textures.iterator().next();
                String texture = (String)class_GameProfileProperty_value.get(textureProperty);
                String decoded = Base64Coder.decodeString(texture);
                url = decoded.replace("{textures:{SKIN:{url:\"", "").replace("\"}}}", "").trim();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return url;
    }

    public static ItemStack getPlayerSkull(String playerName)
    {
        return getPlayerSkull(playerName, null);
    }

    public static ItemStack getPlayerSkull(String playerName, String itemName)
    {
        return getURLSkull("http://skins.minecraft.net/MinecraftSkins/" + playerName + ".png", playerName, UUID.randomUUID(), itemName);
    }

    public static ItemStack getPlayerSkull(String playerName, UUID uuid, String itemName)
    {
        return getURLSkull("http://skins.minecraft.net/MinecraftSkins/" + playerName + ".png", playerName, uuid, itemName);
    }

    public static ItemStack getPlayerSkull(Player player)
    {
        return getPlayerSkull(player, null);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack getPlayerSkull(Player player, String itemName)
    {
        return getPlayerSkull(player.getName(), player.getUniqueId(), itemName);
    }

    public static Object getSkullProfile(ItemMeta itemMeta)
    {
        Object profile = null;
        try {
            if (itemMeta == null || !class_CraftMetaSkull.isInstance(itemMeta)) return false;
            profile = class_CraftMetaSkull_profile.get(itemMeta);
        } catch (Exception ex) {

        }
        return profile;
    }

    public static boolean setSkullProfile(ItemMeta itemMeta, Object data)
    {
        try {
            if (itemMeta == null || !class_CraftMetaSkull.isInstance(itemMeta)) return false;
            class_CraftMetaSkull_profile.set(itemMeta, data);
            return true;
        } catch (Exception ex) {

        }
        return false;
    }
}