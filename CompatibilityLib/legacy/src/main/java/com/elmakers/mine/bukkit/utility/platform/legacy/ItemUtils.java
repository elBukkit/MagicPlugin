package com.elmakers.mine.bukkit.utility.platform.legacy;

import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.utility.CompatibilityConstants;
import com.elmakers.mine.bukkit.utility.platform.Platform;
import com.elmakers.mine.bukkit.utility.platform.base.ItemUtilsBase;

public class ItemUtils extends ItemUtilsBase {
    public ItemUtils(Platform platform) {
        super(platform);
    }

    @Override
    public Object getHandle(org.bukkit.inventory.ItemStack stack) {
        Object handle = null;
        try {
            handle = NMSUtils.class_CraftItemStack_getHandleField.get(stack);
        } catch (Throwable ex) {
            handle = null;
        }
        return handle;
    }

    @Override
    public Object getTag(Object mcItemStack) {
        Object tag = null;
        try {
            tag = NMSUtils.class_ItemStack_tagField.get(mcItemStack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    @Override
    public Object getTag(ItemStack itemStack) {
        Object tag = null;
        try {
            Object mcItemStack = ItemUtils.this.getHandle(itemStack);
            if (mcItemStack == null) {
                if (itemStack.hasItemMeta()) {
                    itemStack = ItemUtils.this.makeReal(itemStack);
                    mcItemStack = ItemUtils.this.getHandle(itemStack);
                }
            }
            if (mcItemStack == null) return null;
            tag = NMSUtils.class_ItemStack_tagField.get(mcItemStack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    protected Object getNMSCopy(ItemStack stack) {
        Object nms = null;
        try {
            nms = NMSUtils.class_CraftItemStack_copyMethod.invoke(null, stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return nms;
    }

    @Override
    public ItemStack getCopy(ItemStack stack) {
        if (stack == null) return null;
        if (NMSUtils.class_CraftItemStack_mirrorMethod == null) return stack;

        try {
            Object craft = ItemUtils.this.getNMSCopy(stack);
            stack = (ItemStack) NMSUtils.class_CraftItemStack_mirrorMethod.invoke(null, craft);
        } catch (Throwable ex) {
            stack = null;
        }

        return stack;
    }

    @Override
    public ItemStack makeReal(ItemStack stack) {
        if (stack == null) return null;
        Object nmsStack = ItemUtils.this.getHandle(stack);
        if (nmsStack == null) {
            stack = ItemUtils.this.getCopy(stack);
            nmsStack = ItemUtils.this.getHandle(stack);
        }
        if (nmsStack == null) {
            return null;
        }
        try {
            Object tag = NMSUtils.class_ItemStack_tagField.get(nmsStack);
            if (tag == null) {
                NMSUtils.class_ItemStack_tagField.set(nmsStack, NMSUtils.class_NBTTagCompound_constructor.newInstance());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }

        return stack;
    }

    @Override
    public void addGlow(ItemStack stack) {
        if (ItemUtils.this.isEmpty(stack)) return;

        try {
            ItemMeta meta = stack.getItemMeta();
            meta.addEnchant(Enchantment.LUCK, 1, true);
            stack.setItemMeta(meta);
       } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeGlow(ItemStack stack) {
        if (ItemUtils.this.isEmpty(stack)) return;

        try {
            ItemMeta meta = stack.getItemMeta();
            if (meta.hasEnchant(Enchantment.LUCK)) {
                meta.removeEnchant(Enchantment.LUCK);
                stack.setItemMeta(meta);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isUnbreakable(ItemStack stack) {
        if (ItemUtils.this.isEmpty(stack)) return false;
        Boolean unbreakableFlag = null;
        try {
            Object tagObject = ItemUtils.this.getTag(stack);
            if (tagObject == null) return false;
            unbreakableFlag = platform.getNBTUtils().getMetaBoolean(tagObject, "Unbreakable");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return unbreakableFlag != null && unbreakableFlag;
    }

    @Override
    public void makeUnbreakable(ItemStack stack) {
        if (ItemUtils.this.isEmpty(stack)) return;

        try {
            Object craft = ItemUtils.this.getHandle(stack);
            if (craft == null) return;
            Object tagObject = ItemUtils.this.getTag(craft);
            if (tagObject == null) return;

            Object unbreakableFlag = null;
            unbreakableFlag = NMSUtils.class_NBTTagByte_constructor.newInstance((byte) 1);
            NMSUtils.class_NBTTagCompound_setMethod.invoke(tagObject, "Unbreakable", unbreakableFlag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void removeUnbreakable(ItemStack stack) {
        platform.getNBTUtils().removeMeta(stack, "Unbreakable");
    }

    @Override
    public void hideFlags(ItemStack stack, int flags) {
        if (ItemUtils.this.isEmpty(stack)) return;

        try {
            Object craft = ItemUtils.this.getHandle(stack);
            if (craft == null) return;
            Object tagObject = ItemUtils.this.getTag(craft);
            if (tagObject == null) return;

            Object hideFlag = null;
            hideFlag = NMSUtils.class_NBTTagInt_constructor.newInstance(flags);
            NMSUtils.class_NBTTagCompound_setMethod.invoke(tagObject, "HideFlags", hideFlag);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void makeTemporary(ItemStack itemStack, String message) {
        platform.getNBTUtils().setMeta(itemStack, "temporary", message);
    }

    @Override
    public boolean isTemporary(ItemStack itemStack) {
        return platform.getNBTUtils().hasMeta(itemStack, "temporary");
    }

    @Override
    public void makeUnplaceable(ItemStack itemStack) {
        platform.getNBTUtils().setMeta(itemStack, "unplaceable", "true");
    }

    @Override
    public void removeUnplaceable(ItemStack itemStack) {
        platform.getNBTUtils().removeMeta(itemStack, "unplaceable");
    }

    @Override
    public boolean isUnplaceable(ItemStack itemStack) {
        return platform.getNBTUtils().hasMeta(itemStack, "unplaceable");
    }

    @Override
    public String getTemporaryMessage(ItemStack itemStack) {
        return platform.getNBTUtils().getMetaString(itemStack, "temporary");
    }

    @Override
    public void setReplacement(ItemStack itemStack, ItemStack replacement) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("item", replacement);
        platform.getNBTUtils().setMeta(itemStack, "replacement", configuration.saveToString());
    }

    @Override
    public ItemStack getReplacement(ItemStack itemStack) {
        String serialized = platform.getNBTUtils().getMetaString(itemStack, "replacement");
        if (serialized == null || serialized.isEmpty()) {
            return null;
        }
        YamlConfiguration configuration = new YamlConfiguration();
        ItemStack replacement = null;
        try {
            configuration.loadFromString(serialized);
            replacement = configuration.getItemStack("item");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return replacement;
    }

    @Override
    public boolean isEmpty(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return true;
        if (NMSUtils.class_ItemStack_isEmptyMethod == null) return false;
        try {
            Object handle = ItemUtils.this.getHandle(itemStack);
            if (handle == null) return false;
            return (Boolean) NMSUtils.class_ItemStack_isEmptyMethod.invoke(handle);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    protected Object getTagString(String value) {
        try {
            return NMSUtils.class_NBTTagString_consructor.newInstance(value);
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return null;
    }

    @Override
    public Object setStringList(Object nbtBase, String tag, Collection<String> values) {
        if (nbtBase == null) return null;
        Object listMeta = null;
        try {
            listMeta = NMSUtils.class_NBTTagList_constructor.newInstance();

            for (String value : values) {
                Object nbtString = ItemUtils.this.getTagString(value);
                platform.getNBTUtils().addToList(listMeta, nbtString);
            }

            NMSUtils.class_NBTTagCompound_setMethod.invoke(nbtBase, tag, listMeta);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
        return listMeta;
    }

    @Override
    public ItemStack getItem(Object itemTag) {
        if (itemTag == null) return null;
        ItemStack item = null;
        try {
            Object nmsStack = null;
            if (NMSUtils.class_ItemStack_consructor != null) {
                nmsStack = NMSUtils.class_ItemStack_consructor.newInstance(itemTag);
            } else {
                nmsStack = NMSUtils.class_ItemStack_createStackMethod.invoke(null, itemTag);
            }
            item = (ItemStack) NMSUtils.class_CraftItemStack_mirrorMethod.invoke(null, nmsStack);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return item;
    }

    @Override
    public ItemStack[] getItems(Object rootTag, String tagName) {
        try {
            Object itemList = NMSUtils.class_NBTTagCompound_getListMethod.invoke(rootTag, tagName, CompatibilityConstants.NBT_TYPE_COMPOUND);
            Integer size = (Integer) NMSUtils.class_NBTTagList_sizeMethod.invoke(itemList);
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                try {
                    Object itemData = NMSUtils.class_NBTTagList_getMethod.invoke(itemList, i);
                    if (itemData != null) {
                        items[i] = ItemUtils.this.getItem(itemData);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return items;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
