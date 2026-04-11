package com.elmakers.mine.bukkit.utility.platform.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;

import com.elmakers.mine.bukkit.utility.ReflectionUtils;
import com.elmakers.mine.bukkit.utility.platform.DeprecatedUtils;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;
import com.elmakers.mine.bukkit.utility.platform.Platform;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

public class ItemUtilsBase implements ItemUtils {
    protected final Platform platform;

    protected ItemUtilsBase(final Platform platform) {
        this.platform = platform;
    }

    @Override
    public void addGlow(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setEnchantmentGlintOverride(true);
        stack.setItemMeta(meta);
    }

    @Override
    public void removeGlow(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setEnchantmentGlintOverride(null);
        stack.setItemMeta(meta);
    }

    @Override
    public void makeTemporary(ItemStack itemStack, String message) {
        platform.getNBTUtils().setString(itemStack, "temporary", message);
    }

    @Override
    public boolean isTemporary(ItemStack itemStack) {
        return platform.getNBTUtils().containsTag(itemStack, "temporary");
    }

    @Override
    public void makeUnplaceable(ItemStack itemStack) {
        platform.getNBTUtils().setString(itemStack, "unplaceable", "true");
    }

    @Override
    public void removeUnplaceable(ItemStack itemStack) {
        platform.getNBTUtils().removeMeta(itemStack, "unplaceable");
    }

    @Override
    public boolean isUnplaceable(ItemStack itemStack) {
        return platform.getNBTUtils().containsTag(itemStack, "unplaceable");
    }

    @Override
    public String getTemporaryMessage(ItemStack itemStack) {
        return platform.getNBTUtils().getString(itemStack, "temporary");
    }

    @Override
    public void setReplacement(ItemStack itemStack, ItemStack replacement) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("item", replacement);
        platform.getNBTUtils().setString(itemStack, "replacement", configuration.saveToString());
    }

    @Override
    public ItemStack getReplacement(ItemStack itemStack) {
        String serialized = platform.getNBTUtils().getString(itemStack, "replacement");
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
    public boolean isSameItem(ItemStack first, ItemStack second) {
        if (first.getType() != second.getType()) return false;
        DeprecatedUtils deprecatedUtils = platform.getDeprecatedUtils();
        if (deprecatedUtils.getItemDamage(first) != deprecatedUtils.getItemDamage(second)) return false;
        return hasSameTags(first, second);
    }

    @Override
    public boolean hasSameTags(ItemStack first, ItemStack second) {
        Object firstTag = getTag(first);
        Object secondTag = getTag(second);
        return Objects.equals(firstTag, secondTag);
    }

    @Override
    public int getCustomModelData(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return 0;
        if (!itemMeta.hasCustomModelData()) return 0;
        CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();
        // Spigot will just throw an error if there is only string data in here, which is not great behavior
        List<Float> floats = component.getFloats();
        if (floats.isEmpty()) return 0;
        return itemMeta.getCustomModelData();
    }

    @Override
    public CompoundTag getOrCreateTag(ItemStack itemStack) {
        CompoundTag tag = null;
        try {
            Object mcItemStack = getHandle(itemStack);
            if (mcItemStack == null) {
                if (itemStack.hasItemMeta()) {
                    itemStack = makeReal(itemStack);
                    mcItemStack = getHandle(itemStack);
                }
            }
            if (mcItemStack == null) return null;
            net.minecraft.world.item.ItemStack stack = (net.minecraft.world.item.ItemStack)mcItemStack;
            tag = getOrCreateTag(stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    @Override
    public CompoundTag getOrCreateTag(Object mcItemStack) {
        if (mcItemStack == null || !(mcItemStack instanceof net.minecraft.world.item.ItemStack)) return null;
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)mcItemStack;
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = null;
        if (customData == null) {
            tag = new CompoundTag();
            // This makes a copy
            customData = CustomData.of(tag);
            tag = getCompoundTagFromCustomData(customData);
            ((net.minecraft.world.item.ItemStack)mcItemStack).set(DataComponents.CUSTOM_DATA, customData);
        } else {
            tag = getCompoundTagFromCustomData(customData);
        }
        return tag;
    }

    protected net.minecraft.world.item.ItemStack getNMSCopy(ItemStack stack) {
        net.minecraft.world.item.ItemStack nms = null;
        try {
            nms = CraftItemStack.asNMSCopy(stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return nms;
    }

    @Override
    public ItemStack getCopy(ItemStack stack) {
        if (stack == null) return null;
        net.minecraft.world.item.ItemStack craft = getNMSCopy(stack);
        return CraftItemStack.asCraftMirror(craft);
    }

    @Override
    public boolean isEmpty(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return true;
        Object handle = getHandle(itemStack);
        if (handle == null || !(handle instanceof net.minecraft.world.item.ItemStack)) return false;
        net.minecraft.world.item.ItemStack mcItem = (net.minecraft.world.item.ItemStack)handle;
        return mcItem.isEmpty();
    }

    protected StringTag getTagString(String value) {
        return StringTag.valueOf(value);
    }

    @Override
    public Object setStringList(Object nbtBase, String tag, Collection<String> values) {
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return null;
        CompoundTag compoundTag = (CompoundTag)nbtBase;
        ListTag listTag = new ListTag();

        for (String value : values) {
            StringTag nbtString = getTagString(value);
            listTag.add(nbtString);
        }

        compoundTag.put(tag, listTag);
        return listTag;
    }

    @Override
    public List<String> getStringList(Object nbtBase, String key) {
        List<String> list = new ArrayList<>();
        if (nbtBase == null || !(nbtBase instanceof CompoundTag)) return list;
        CompoundTag compoundTag = (CompoundTag)nbtBase;
        Optional<ListTag> listTagOptional = compoundTag.getList(key);

        if (listTagOptional.isPresent()) {
            ListTag listTag = listTagOptional.get();
            int size = listTag.size();
            for (int i = 0; i < size; i++) {
                Tag entry = listTag.get(i);
                Optional<String> optionalString = entry.asString();
                if (!optionalString.isPresent()) continue;
                list.add(optionalString.get());
            }
        }
        return list;
    }

    @Override
    public ItemStack getItem(Object itemTag) {
        // This was only called by getItems, which no longer uses this method
        throw new RuntimeException("The getItem method is no longer supported");

    }

    @Override
    public ItemStack[] getItems(Object rootTag, String tagName) {
        if (rootTag == null || !(rootTag instanceof CompoundTag)) return null;
        CompoundTag compoundTag = (CompoundTag)rootTag;

        try {
            CraftWorld world = (CraftWorld) Bukkit.getWorlds().get(0);
            ProblemReporter discard = ProblemReporter.DISCARDING;
            ValueInput valueInput = TagValueInput.create(discard, world.getHandle().registryAccess(), compoundTag);
            ValueInput.TypedInputList<ItemStackWithSlot> list = valueInput.listOrEmpty(tagName, ItemStackWithSlot.CODEC);
            List<ItemStack> itemList = new ArrayList<>();
            for (ItemStackWithSlot stackWithSlot : list) {
                ItemStack item = CraftItemStack.asCraftMirror(stackWithSlot.stack());
                itemList.add(item);
            }
            return itemList.toArray(new ItemStack[itemList.size()]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public String getItemModel(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta.hasItemModel()) {
            return itemMeta.getItemModel().toString();
        }
        return null;
    }

    @Override
    public void setCustomModelData(ItemStack itemStack, int customModelData) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.setCustomModelData(customModelData);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public Object getHandle(ItemStack stack) {
        if (stack == null || !(stack instanceof CraftItemStack)) {
            return null;
        }
        return ReflectionUtils.getHandle(platform.getLogger(), stack, CraftItemStack.class);
    }

    private CompoundTag getCompoundTagFromCustomData(CustomData customData) {
        return (CompoundTag)platform.getNBTUtils().getCompoundTagFromCustomData(customData);
    }

    @Override
    public CompoundTag getTag(Object mcItemStack) {
        if (mcItemStack == null || !(mcItemStack instanceof net.minecraft.world.item.ItemStack)) return null;
        net.minecraft.world.item.ItemStack itemStack = (net.minecraft.world.item.ItemStack)mcItemStack;
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        return getCompoundTagFromCustomData(customData);
    }

    @Override
    public CompoundTag getTag(ItemStack itemStack) {
        CompoundTag tag = null;
        try {
            Object mcItemStack = getHandle(itemStack);
            if (mcItemStack == null) {
                if (itemStack.hasItemMeta()) {
                    itemStack = makeReal(itemStack);
                    mcItemStack = getHandle(itemStack);
                }
            }
            if (mcItemStack == null) return null;
            net.minecraft.world.item.ItemStack stack = (net.minecraft.world.item.ItemStack)mcItemStack;
            tag = getTag(stack);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return tag;
    }

    @Override
    public ItemStack makeReal(ItemStack stack) {
        if (stack == null) return null;
        Object nmsStack = getHandle(stack);
        if (nmsStack == null) {
            stack = getCopy(stack);
            nmsStack = getHandle(stack);
        }
        if (nmsStack == null) {
            return null;
        }

        return stack;
    }

    @Override
    public boolean isUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta.isUnbreakable();
    }

    @Override
    public void makeUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(true);
        stack.setItemMeta(meta);
    }

    @Override
    public void removeUnbreakable(ItemStack stack) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(false);
        stack.setItemMeta(meta);
    }

    @Override
    public void hideFlags(ItemStack stack, int flags) {
        if (isEmpty(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        ItemFlag[] flagArray = ItemFlag.values();
        for (int ordinal = 0; ordinal < flagArray.length; ordinal++) {
            ItemFlag flag = flagArray[ordinal];
            if ((flags & 1) == 1) {
                meta.addItemFlags(flag);
            } else {
                meta.removeItemFlags(flag);
            }
            flags >>= 1;
        }
        stack.setItemMeta(meta);
    }

    public Object getEquippable(ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) return null;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasEquippable()) return null;
        return itemMeta.getEquippable();
    }

    public void setEquippable(ItemStack itemStack, Object equippable) {
        if (equippable == null || !(equippable instanceof EquippableComponent)) return;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        itemMeta.setEquippable((EquippableComponent)equippable);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public void setItemModel(ItemStack itemStack, String itemModel) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setItemModel(NamespacedKey.fromString(itemModel));
            itemStack.setItemMeta(meta);
        }
    }
}
