package com.elmakers.mine.bukkit.item;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;

public class InventorySlot {
    private static final Map<Integer, InventorySlot> bySlot = new HashMap<>();
    private static final Map<String, InventorySlot> byName = new HashMap<>();

    public static final InventorySlot HELMET = new InventorySlot(ArmorSlot.HELMET);
    public static final InventorySlot CHESTPLATE = new InventorySlot(ArmorSlot.CHESTPLATE);
    public static final InventorySlot LEGGINGS = new InventorySlot(ArmorSlot.LEGGINGS);
    public static final InventorySlot BOOTS = new InventorySlot(ArmorSlot.BOOTS);

    public static final InventorySlot MAIN_HAND = new InventorySlot(ArmorSlot.MAIN_HAND);
    public static final InventorySlot OFF_HAND = new InventorySlot(ArmorSlot.OFF_HAND);
    public static final InventorySlot FREE = new InventorySlot(ArmorSlot.FREE);

    // This is here for armor stands
    public static final InventorySlot RIGHT_ARM = new InventorySlot(ArmorSlot.RIGHT_ARM);

    private final int slot;
    private final ArmorSlot slotType;

    InventorySlot(ArmorSlot armorSlot) {
        this(armorSlot, armorSlot.getSlot());
    }

    InventorySlot(int slot) {
        this(ArmorSlot.INVENTORY, slot);
    }

    InventorySlot(ArmorSlot armorSlot, int slot) {
        this.slotType = armorSlot;
        this.slot = slot;
        if (slot >= 0) {
            bySlot.put(slot, this);
        }
        byName.put(armorSlot.name().toLowerCase(), this);
    }

    @Nullable
    public static InventorySlot parse(String key) {
        if (key.equalsIgnoreCase("mainhand")) {
            key = "main_hand";
        } else if (key.equalsIgnoreCase("offhand")) {
            key = "off_hand";
        } else if (key.equalsIgnoreCase("any")) {
            key = "free";
        }
        key = key.toLowerCase();
        InventorySlot slot = byName.get(key);
        if (slot == null) {
            try {
                int slotNumber = Integer.parseInt(key);
                slot = bySlot.get(slotNumber);
                if (slot == null) {
                    slot = new InventorySlot(slotNumber);
                    bySlot.put(slotNumber, slot);
                }
                byName.put(key, slot);
            } catch (Exception ignore) {
            }
        }
        return slot;
    }

    public static Integer parseSlot(String key) {
        try {
            return Integer.parseInt(key);
        } catch (Exception ignore) {
        }
        InventorySlot slot = parse(key);
        return slot == null ? null : slot.getSlot();
    }

    public static InventorySlot getArmorSlot(int slot) {
        slot = Math.max(Math.min(slot, 3), 0);
        switch (slot) {
            case 0: return BOOTS;
            case 1: return LEGGINGS;
            case 2: return CHESTPLATE;
            case 3: return HELMET;
        }
        return HELMET;
    }

    public static InventorySlot getSlot(int slot) {
        InventorySlot number = bySlot.get(slot);
        if (number == null) {
            number = new InventorySlot(slot);
            bySlot.put(slot, number);
        }
        return number;
    }

    public int getSlot() {
        return slot;
    }

    public int getSlot(Mage mage) {
        if (slot != -1 || !mage.isPlayer()) {
            return slot;
        }
        switch (this.slotType) {
            case MAIN_HAND:
                return mage.getPlayer().getInventory().getHeldItemSlot();
            case OFF_HAND:
                return 40;
            case FREE:
                Inventory inventory = mage.getInventory();
                for (int i = 0; i < inventory.getSize(); i++) {
                    if (CompatibilityLib.getItemUtils().isEmpty(inventory.getItem(i))) {
                        return i;
                    }
                }
            break;
            default:
                return -1;
        }
        return -1;
    }

    public boolean isArmorSlot() {
        return slotType.isArmorSlot();
    }

    public boolean isAnySlot() {
        return slotType == ArmorSlot.FREE;
    }

    public boolean setItem(EntityEquipment equipment, ItemStack itemStack) {
        if (equipment == null) return false;
        switch (this.slotType) {
            case HELMET:
                equipment.setHelmet(itemStack);
                break;
            case CHESTPLATE:
                equipment.setChestplate(itemStack);
                break;
            case LEGGINGS:
                equipment.setLeggings(itemStack);
                break;
            case BOOTS:
                equipment.setBoots(itemStack);
                break;
            case MAIN_HAND:
                equipment.setItemInMainHand(itemStack);
                break;
            case OFF_HAND:
                equipment.setItemInOffHand(itemStack);
                break;
            case RIGHT_ARM:
                equipment.setItemInOffHand(itemStack);
                break;
            default: return false;
        }
        return true;
    }

    @Nullable
    public ItemStack getItem(EntityEquipment equipment) {
        if (equipment == null) return null;
        switch (this.slotType) {
            case HELMET:
                return equipment.getHelmet();
            case CHESTPLATE:
                return equipment.getChestplate();
            case LEGGINGS:
                return equipment.getLeggings();
            case BOOTS:
                return equipment.getBoots();
            case MAIN_HAND:
                return equipment.getItemInMainHand();
            case OFF_HAND:
            case RIGHT_ARM:
                return equipment.getItemInOffHand();
            default: return null;
        }
    }
}
