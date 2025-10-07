package com.elmakers.mine.bukkit.utility.platform.v1_21_10;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_21_R6.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.NBTUtilsBase;

import net.minecraft.core.component.DataComponents;
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
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;

public class NBTUtils extends NBTUtilsBase {
    public NBTUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Object getTag(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        Object tagObject = platform.getItemUtils().getTag(stack);
        if (tagObject == null || !(tagObject instanceof CompoundTag)) return null;
        return ((CompoundTag)tagObject).get(tag);
    }

    @Override
    public Object getTag(Object nbtBase, String tag) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;
        return ((CompoundTag)nbtBase).get(tag);
    }

    @Override
    public Set<String> getAllKeys(Object nbtBase) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;
        return ((CompoundTag)nbtBase).keySet();
    }

    @Override
    public boolean contains(Object nbtBase, String tag) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return false;
        return ((CompoundTag)nbtBase).contains(tag);
    }

    @Override
    public Object createTag(Object nbtBase, String tag) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;

        CompoundTag compoundTag = (CompoundTag)nbtBase;
        Optional<CompoundTag> childTag = compoundTag.getCompound(tag);
        if (childTag.isPresent()) {
            return childTag.get();
        }
        CompoundTag newTag = new CompoundTag();
        compoundTag.put(tag, newTag);
        return newTag;
    }

    @Override
    public Object createTag(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        Object outputObject = getTag(stack, tag);
        if (outputObject == null || !(outputObject instanceof CompoundTag)) {
            Object craft = platform.getItemUtils().getHandle(stack);
            if (craft == null) return null;

            CompoundTag tagObject = (CompoundTag)platform.getItemUtils().getTag(craft);
            if (tagObject == null) {
                tagObject = new CompoundTag();
                // This makes a copy
                CustomData customData = CustomData.of(tagObject);
                tagObject = (CompoundTag) ReflectionUtils.getPrivate(platform.getLogger(), customData, CustomData.class, "tag");
                ((net.minecraft.world.item.ItemStack)craft).set(DataComponents.CUSTOM_DATA, customData);
            }
            outputObject = new CompoundTag();
            tagObject.put(tag, (CompoundTag)outputObject);
        }
        return outputObject;
    }

    @Override
    public byte[] getByteArray(Object tag, String key) {
        if (tag == null || !(tag instanceof CompoundTag)) return null;
        Optional<byte[]> optional = ((CompoundTag)tag).getByteArray(key);
        return optional.orElse(null);
    }

    @Override
    public int[] getIntArray(Object tag, String key) {
        if (tag == null || !(tag instanceof CompoundTag)) return null;
        Optional<int[]> optional = ((CompoundTag)tag).getIntArray(key);
        return optional.orElse(null);
    }

    @Override
    public String getString(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        Optional<String> optional = ((CompoundTag)node).getString(tag);
        return optional.orElse(null);
    }

    @Override
    public String getString(ItemStack stack, String tag) {
        if (platform.getItemUtils().isEmpty(stack)) return null;
        String meta = null;
        Object tagObject = platform.getItemUtils().getTag(stack);
        if (tagObject == null || !(tagObject instanceof CompoundTag)) return null;
        Optional<String> optional = ((CompoundTag)tagObject).getString(tag);
        return optional.orElse(null);
    }

    @Override
    public Byte getOptionalByte(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        Optional<Byte> optional = ((CompoundTag)node).getByte(tag);
        return optional.orElse(null);
    }

    @Override
    public Integer getOptionalInt(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        Optional<Integer> optional = ((CompoundTag)node).getInt(tag);
        return optional.orElse(null);
    }

    @Override
    public Short getOptionalShort(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        Optional<Short> optional = ((CompoundTag)node).getShort(tag);
        return optional.orElse(null);
    }

    @Override
    public Double getOptionalDouble(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        Optional<Double> optional = ((CompoundTag)node).getDouble(tag);
        return optional.orElse(null);
    }

    @Override
    public Boolean getOptionalBoolean(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return null;
        Optional<Boolean> optional = ((CompoundTag)node).getBoolean(tag);
        return optional.orElse(null);
    }

    @Override
    public void setLong(Object node, String tag, long value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putLong(tag, value);
    }

    @Override
    public void setBoolean(Object node, String tag, boolean value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putBoolean(tag, value);
    }

    @Override
    public void setDouble(Object node, String tag, double value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putDouble(tag, value);
    }

    @Override
    public void setInt(Object node, String tag, int value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putInt(tag, value);
    }

    @Override
    public void setMetaShort(Object node, String tag, short value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putShort(tag, value);
    }

    @Override
    public void removeMeta(Object node, String tag) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).remove(tag);
    }

    @Override
    public void setTag(Object node, String tag, Object child) {
        if (node == null || !(node instanceof CompoundTag)) return;
        if (child == null) {
            ((CompoundTag)node).remove(tag);
        } else if (child instanceof Tag) {
            ((CompoundTag)node).put(tag, (Tag)child);
        }
    }

    @Override
    public boolean setTag(ItemStack stack, String tag, Object child) {
        if (platform.getItemUtils().isEmpty(stack)) return false;
        Object craft = platform.getItemUtils().getHandle(stack);
        if (craft == null) return false;
        Object node = platform.getItemUtils().getOrCreateTag(craft);
        if (node == null || !(node instanceof CompoundTag)) return false;
        if (child == null) {
            ((CompoundTag)node).remove(tag);
        } else {
            ((CompoundTag)node).put(tag, (Tag)child);
        }
        return true;
    }

    @Override
    public void setString(Object node, String tag, String value) {
        if (node == null || !(node instanceof CompoundTag)) return;
        ((CompoundTag)node).putString(tag, value);
    }

    @Override
    public void setString(ItemStack stack, String tag, String value) {
        if (platform.getItemUtils().isEmpty(stack)) return;
        Object craft = platform.getItemUtils().getHandle(stack);
        if (craft == null) return;
        Object tagObject = platform.getItemUtils().getOrCreateTag(craft);
        if (tagObject == null || !(tagObject instanceof CompoundTag)) return;
        ((CompoundTag)tagObject).putString(tag, value);
    }

    @Override
    public void setIntArray(Object tag, String key, int[] value) {
        if (tag == null || !(tag instanceof CompoundTag)) return;
        ((CompoundTag)tag).put(key, new IntArrayTag(value));
    }

    @Override
    public void setByteArray(Object tag, String key, byte[] value) {
        if (tag == null || !(tag instanceof CompoundTag)) return;
        ((CompoundTag)tag).put(key, new ByteArrayTag(value));
    }

    @Override
    public void setEmptyList(Object tag, String key) {
        if (tag == null || !(tag instanceof CompoundTag)) return;
        ((CompoundTag)tag).put(key, new ListTag());
    }

    @Override
    public void addToList(Object listObject, Object node) {
        if (listObject == null || !(listObject instanceof ListTag) || !(node instanceof Tag)) return;
        ListTag list = (ListTag)listObject;
        list.add((Tag)node);
    }

    @Override
    public Object readTagFromStream(InputStream input) {
        CompoundTag tag = null;
        try {
            tag = NbtIo.readCompressed(input, NbtAccounter.unlimitedHeap());
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error reading from NBT input stream", ex);
        }
        return tag;
    }

    @Override
    public boolean writeTagToStream(Object tag, OutputStream output) {
        if (tag == null || !(tag instanceof CompoundTag)) return false;
        try {
            NbtIo.writeCompressed((CompoundTag)tag, output);
        } catch (Exception ex) {
            platform.getLogger().log(Level.WARNING, "Error writing NBT output stream", ex);
            return false;
        }
        return true;
    }

    @Override
    public Collection<Object> getTagList(Object tag, String key) {
        Collection<Object> list = new ArrayList<>();
        if (tag == null || !(tag instanceof CompoundTag)) {
            return list;
        }

        Optional<ListTag> optional = ((CompoundTag)tag).getList(key);

        if (optional.isPresent()) {
            ListTag listTag = optional.get();
            int size = listTag.size();
            for (int i = 0; i < size; i++) {
                Tag entry = listTag.get(i);
                list.add(entry);
            }
        }
        return list;
    }

    @Override
    public Object newCompoundTag() {
        return new CompoundTag();
    }

    @Override
    public boolean setSpawnEggEntityData(ItemStack spawnEgg, Entity entity, Object entityData) {
        if (platform.getItemUtils().isEmpty(spawnEgg)) return false;
        if (entityData == null || !(entityData instanceof CompoundTag)) return false;

        Object handle = platform.getItemUtils().getHandle(spawnEgg);
        if (handle == null) return false;
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)handle;
        CraftEntity craft = (CraftEntity) entity;
        net.minecraft.world.entity.Entity nmsEntity = craft.getHandle();
        TypedEntityData<EntityType<?>> typedEntityData = TypedEntityData.of(nmsEntity.getType(), (CompoundTag)entityData);
        itemStack.set(DataComponents.ENTITY_DATA, typedEntityData);
        return true;
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
    public Set<String> getTagKeys(Object tag) {
        if (tag == null || !(tag instanceof CompoundTag)) {
            return null;
        }
        return ((CompoundTag)tag).keySet();
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
        Object value;
        if (tag instanceof DoubleTag) {
            value = ((DoubleTag)tag).asDouble().orElse(null);
        } else if (tag instanceof IntTag) {
            value = ((IntTag)tag).asInt().orElse(null);
        } else if (tag instanceof LongTag) {
            value = ((LongTag)tag).asLong().orElse(null);
        } else if (tag instanceof FloatTag) {
            value = ((FloatTag)tag).asFloat().orElse(null);
        } else if (tag instanceof ShortTag) {
            value = ((ShortTag)tag).asShort().orElse(null);
        } else if (tag instanceof ByteTag) {
            // This is kind of nasty. Really need a type-juggling container class for config properties.
            value = ((ByteTag)tag).asByte().orElse(null);
            if (value != null) {
                if (value.equals((byte)0)) {
                    value = false;
                } else if (value.equals((byte)1)) {
                    value = true;
                }
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
            value = ((StringTag)tag).asString().orElse(null);
        } else if (tag instanceof IntArrayTag) {
            value = ((IntArrayTag)tag).asIntArray().orElse(null);
        } else if (tag instanceof ByteArrayTag) {
            value = ((ByteArrayTag)tag).asByteArray().orElse(null);
        } else if (tag instanceof LongArrayTag) {
            value = ((LongArrayTag)tag).asLongArray().orElse(null);
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
        } else {
            value = null; // ???
        }

        return value;
    }
}
