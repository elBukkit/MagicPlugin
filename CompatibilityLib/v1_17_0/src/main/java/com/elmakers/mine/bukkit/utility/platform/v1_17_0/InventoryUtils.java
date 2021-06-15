package com.elmakers.mine.bukkit.utility.platform.v1_17_0;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.elmakers.mine.bukkit.utility.Base64Coder;
import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.InventoryUtilsBase;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class InventoryUtils extends InventoryUtilsBase {
    public InventoryUtils(Platform platform) {
        super(platform);
    }

    @Override
    public boolean addTagsToNBT(Map<String, Object> tags, Object node) {
        if (node == null) {
            platform.getLogger().warning("Trying to save tags to a null node");
            return false;
        }
        if (!(node instanceof CompoundTag)) {
            platform.getLogger().warning("Trying to save tags to a non-CompoundTag");
            return false;
        }

        CompoundTag compoundTag = (CompoundTag)node;
        for (Map.Entry<String, Object> tag : tags.entrySet()) {
            Object value = tag.getValue();
            try {
                Tag wrappedTag = wrapInTag(value);
                if (wrappedTag == null) continue;
                compoundTag.put(tag.getKey(), wrappedTag);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error saving item data tag " + tag.getKey(), ex);
            }
        }

        return true;
    }

    @Override
    public boolean saveTagsToNBT(Map<String, Object> tags, Object node, Set<String> tagNames) {
        if (node == null) {
            platform.getLogger().warning("Trying to save tags to a null node");
            return false;
        }
        if (!(node instanceof CompoundTag)) {
            platform.getLogger().warning("Trying to save tags to a non-CompoundTag");
            return false;
        }

        CompoundTag compoundTag = (CompoundTag)node;
        if (tagNames == null) {
            tagNames = tags.keySet();
        }

        // Remove tags that were not included
        Set<String> currentTags = getTagKeys(node);
        if (currentTags != null && !tagNames.containsAll(currentTags)) {
            // Need to copy this, getKeys returns a live list and bad things can happen.
            currentTags = new HashSet<>(currentTags);
        } else {
            currentTags = null;
        }

        for (String tagName : tagNames) {
            if (currentTags != null) currentTags.remove(tagName);
            Object value = tags.get(tagName);
            try {
                Tag wrappedTag = wrapInTag(value);
                if (wrappedTag == null) continue;
                compoundTag.put(tagName, wrappedTag);
            } catch (Exception ex) {
                platform.getLogger().log(Level.WARNING, "Error saving item data tag " + tagName, ex);
            }
        }

        // Finish removing any remaining properties
        if (currentTags != null) {
            for (String currentTag : currentTags) {
                platform.getNBTUtils().removeMeta(node, currentTag);
            }
        }

        return true;
    }

    @Override
    public Tag wrapInTag(Object value) {
        if (value == null) return null;
        Tag wrappedValue = null;
        if (value instanceof Boolean) {
            wrappedValue = ByteTag.valueOf((byte)((boolean)value ? 1 : 0));
        } else if (value instanceof Double) {
            wrappedValue = DoubleTag.valueOf((Double)value);
        } else if (value instanceof Float) {
            wrappedValue = FloatTag.valueOf((Float) value);
        } else if (value instanceof Integer) {
            wrappedValue = IntTag.valueOf((Integer)value);
        } else if (value instanceof Long) {
            wrappedValue = LongTag.valueOf((Long) value);
        } else if (value instanceof ConfigurationSection) {
            wrappedValue = new CompoundTag();
            saveTagsToNBT((ConfigurationSection)value, wrappedValue, null);
        } else if (value instanceof Map) {
            wrappedValue = new CompoundTag();
            @SuppressWarnings("unchecked")
            Map<String, Object> valueMap = (Map<String, Object>)value;
            addTagsToNBT(valueMap, wrappedValue);
        } else if (value instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> list = (Collection<Object>)value;
            ListTag listMeta = new ListTag();
            if (list.size() > 1 && list instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> checkList = (List<Object>)value;
                Object first = checkList.get(0);
                Object second = checkList.get(1);
                if (first instanceof String && !(second instanceof String)) {
                    list = new ArrayList<>();
                    for (int i = 1; i < checkList.size(); i++) {
                        if (first.equals("I")) {
                            list.add(convertToInteger(checkList.get(i)));
                        } else if (first.equals("L")) {
                            list.add(convertToLong(checkList.get(i)));
                        } else if (first.equals("B")) {
                            list.add(convertToByte(checkList.get(i)));
                        } else {
                            list.add(checkList.get(i));
                        }
                    }
                    if (first.equals("B")) {
                        wrappedValue = new ByteArrayTag(makeByteArray((List<Object>)list));
                    } else if (first.equals("I")) {
                        wrappedValue = new IntArrayTag(makeIntArray((List<Object>)list));
                    } else if (first.equals("L")) {
                        wrappedValue = new LongArrayTag(makeLongArray((List<Object>)list));
                    }
                }
            }
            if (wrappedValue == null) {
                for (Object item : list) {
                    if (item != null) {
                        platform.getNBTUtils().addToList(listMeta, wrapInTag(item));
                    }
                }
                wrappedValue = listMeta;
            }
        } else {
            wrappedValue = StringTag.valueOf(value.toString());
        }

        return wrappedValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getTagKeys(Object tag) {
        if (tag == null || !(tag instanceof CompoundTag)) {
            return null;
        }
        return ((CompoundTag)tag).getAllKeys();
    }

    @Override
    public Object getMetaObject(Object tag, String key) {
        if (tag == null || !(tag instanceof CompoundTag)) {
            return null;
        }

        try {
            Tag metaBase = ((CompoundTag)tag).get(key);
            return getTagValue(metaBase);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public Object getTagValue(Object tag) throws IllegalAccessException, InvocationTargetException {
        if (tag == null) return null;
        Object value = null;
        if (tag instanceof DoubleTag) {
            value = ((DoubleTag)tag).getAsDouble();
        } else if (tag instanceof IntTag) {
            value = ((IntTag)tag).getAsInt();
        } else if (tag instanceof LongTag) {
            value = ((LongTag)tag).getAsLong();
        } else if (tag instanceof FloatTag) {
            value = ((FloatTag)tag).getAsFloat();
        } else if (tag instanceof ShortTag) {
            value = ((ShortTag)tag).getAsShort();
        } else if (tag instanceof ByteTag) {
            // This is kind of nasty. Really need a type-juggling container class for config properties.
            value = ((ByteTag)tag).getAsByte();
            if (value != null && value.equals((byte)0)) {
                value = false;
            } else if (value != null && value.equals((byte)1)) {
                value = true;
            }
        } else if (tag instanceof ListTag) {
            List<Object> converted = new ArrayList<>();
            for (Tag baseTag : (ListTag)tag) {
                Object convertedBase = getTagValue(baseTag);
                if (convertedBase != null) {
                    converted.add(convertedBase);
                }
            }
            value = converted;
        } else if (tag instanceof StringTag) {
            value = ((StringTag)tag).getAsString();
        } else if (tag instanceof CompoundTag) {
            Map<String, Object> compoundMap = new HashMap<>();
            Set<String> keys = getTagKeys(tag);
            for (String key : keys) {
                Tag baseTag = ((CompoundTag)tag).get(key);
                Object convertedBase = getTagValue(baseTag);
                if (convertedBase != null) {
                    compoundMap.put(key, convertedBase);
                }
            }
            value = compoundMap;
        }

        return value;
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
    public boolean setSkullProfile(ItemMeta itemMeta, Object data) {
        if (itemMeta == null || !(itemMeta instanceof SkullMeta)) return false;
        Class<?>[] parameters = {GameProfile.class};
        Object[] values = {data};
        return ReflectionUtils.callPrivate(platform.getLogger(), itemMeta, itemMeta.getClass(), "setProfile", parameters, values);
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
    public void openSign(Player player, Location signBlock) {
        try {
            Object tileEntity = platform.getCompatibilityUtils().getTileEntity(signBlock);
            ServerPlayer playerHandle = ((CraftPlayer)player).getHandle();
            if (tileEntity != null && playerHandle != null && tileEntity instanceof SignBlockEntity) {
                playerHandle.openTextEdit((SignBlockEntity) tileEntity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
