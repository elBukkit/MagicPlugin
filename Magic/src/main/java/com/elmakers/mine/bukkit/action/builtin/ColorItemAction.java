package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.MaterialAndData;
import com.elmakers.mine.bukkit.item.InventorySlot;
import com.elmakers.mine.bukkit.utility.ColorHD;
import com.elmakers.mine.bukkit.utility.CompatibilityLib;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class ColorItemAction extends BaseSpellAction {
    private List<InventorySlot> slots;
    private ColorHD color;
    private boolean useWand;

    private static class UndoColorItemTask implements Runnable {
        private final MageController controller;
        private final Wand wand;
        private final WeakReference<Entity> entityRef;
        private final Map<InventorySlot, Color> slotColors = new HashMap<>();
        private Color wandColor;

        public UndoColorItemTask(MageController controller, Entity entity, Wand wand) {
            this.controller = controller;
            this.entityRef = new WeakReference<>(entity);
            this.wand = wand;
        }

        public void addSlot(InventorySlot slot, Color color) {
            if (color == null || slot == null) return;
            slotColors.put(slot, color);
        }

        public void setWandColor(Color color) {
            this.wandColor = color;
        }

        public boolean isEmpty() {
            return wandColor == null && slotColors.isEmpty();
        }

        @Override
        public void run() {
            if (wandColor != null && wand != null) {
                color(controller, wand.getItem(), wandColor);
            }

            Entity entity = entityRef.get();
            if (entity == null) {
                return;
            }
            if (entity instanceof Item) {
                Color color = slotColors.get(InventorySlot.FREE);
                if (color != null) {
                    Item item = (Item)entity;
                    ItemStack itemStack = item.getItemStack();
                    color(controller, itemStack, color);
                    item.setItemStack(itemStack);
                }
            }

            if (entity instanceof LivingEntity && !slotColors.isEmpty()) {
                LivingEntity li = (LivingEntity)entity;
                EntityEquipment equipment = li.getEquipment();
                if (equipment != null) {
                    for (Map.Entry<InventorySlot, Color> entry : slotColors.entrySet()) {
                        InventorySlot slot = entry.getKey();
                        ItemStack slotItem = slot.getItem(equipment);
                        color(controller, slotItem, entry.getValue());
                        slot.setItem(equipment, slotItem);
                    }
                }
            }
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        List<String> slots = ConfigurationUtils.getStringList(parameters, "slots");
        String slot = parameters.getString("slot");
        if (slot != null && !slot.isEmpty()) {
            if (slots == null) {
                slots = new ArrayList<>();
            }
            slots.add(slot);
        }
        if (slots != null) {
            this.slots = new ArrayList<>();
            for (String slotKey : slots) {
                InventorySlot inventorySlot = InventorySlot.parse(slotKey);
                if (inventorySlot != null) {
                    this.slots.add(inventorySlot);
                } else {
                    context.getLogger().warning("Invalid slot in ColorItem action: " + slotKey);
                }
            }
        }

        ConfigurationSection colorSection = parameters.getConfigurationSection("color");
        if (colorSection != null) {
            color = new ColorHD(colorSection);
        } else {
            color = new ColorHD(parameters.getString("color"));
        }
        useWand = parameters.getBoolean("color_wand");
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        MageController controller = context.getController();
        Wand wand = context.getWand();
        Color color = this.color.getColor();
        UndoColorItemTask undoTask = new UndoColorItemTask(controller, entity, wand);
        if (useWand && wand != null) {
            Color originalColor = color(controller, wand.getItem(), color);
            if (originalColor != null) {
                undoTask.setWandColor(originalColor);
            }
        }

        if (entity instanceof Item) {
            Item item = (Item)entity;
            ItemStack itemStack = item.getItemStack();
            Color originalColor = color(controller, itemStack, color);
            if (originalColor != null) {
                undoTask.addSlot(InventorySlot.FREE, originalColor);
                item.setItemStack(itemStack);
            }
        }

        if (entity instanceof LivingEntity && slots != null && !slots.isEmpty()) {
            LivingEntity li = (LivingEntity)entity;
            EntityEquipment equipment = li.getEquipment();
            if (equipment != null) {
                for (InventorySlot slot : slots) {
                    ItemStack slotItem = slot.getItem(equipment);
                    Color originalColor = color(controller, slotItem, color);
                    if (originalColor != null) {
                        slot.setItem(equipment, slotItem);
                        undoTask.addSlot(slot, originalColor);
                    }
                }
            }
        }

        if (!undoTask.isEmpty()) {
            context.registerForUndo(undoTask);
            return SpellResult.CAST;
        }
        return SpellResult.NO_TARGET;
    }

    protected static Color color(MageController controller, ItemStack itemStack, Color color) {
        Color originalColor = null;
        if (CompatibilityLib.getItemUtils().isEmpty(itemStack)) {
            return originalColor;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta leatherMeta = (LeatherArmorMeta)meta;
            originalColor = leatherMeta.getColor();
            leatherMeta.setColor(color);
        } else if (meta instanceof FireworkMeta) {
            FireworkMeta fireworkMeta = (FireworkMeta)meta;
            originalColor = Color.WHITE;
            if (!fireworkMeta.hasEffects()) {
                FireworkEffect fireworkEffect = FireworkEffect.builder().withColor(color).build();
                fireworkMeta.addEffect(fireworkEffect);
            } else {
                List<FireworkEffect> existingEffects = fireworkMeta.getEffects();
                List<Color> colors = fireworkMeta.getEffects().get(0).getColors();
                if (!colors.isEmpty()) {
                    originalColor = colors.get(0);
                }
                fireworkMeta.clearEffects();
                for (FireworkEffect existingEffect : existingEffects) {
                    FireworkEffect.Type existingType = existingEffect.getType();
                    FireworkEffect fireworkEffect = FireworkEffect.builder()
                        .withColor(color)
                        .flicker(existingEffect.hasFlicker())
                        .trail(existingEffect.hasTrail())
                        .with(existingType == null ? FireworkEffect.Type.BALL : existingType)
                        .build();
                    fireworkMeta.addEffect(fireworkEffect);
                }
            }
        } else if (meta instanceof FireworkEffectMeta) {
            FireworkEffectMeta effectMeta = (FireworkEffectMeta)meta;
            FireworkEffect existingEffect = effectMeta.getEffect();
            originalColor = Color.WHITE;
            if (existingEffect == null) {
                FireworkEffect fireworkEffect = FireworkEffect.builder().withColor(color).build();
                effectMeta.setEffect(fireworkEffect);
            } else {
                FireworkEffect.Type existingType = existingEffect.getType();
                List<Color> colors = existingEffect.getColors();
                if (!colors.isEmpty()) {
                    originalColor = colors.get(0);
                }
                FireworkEffect fireworkEffect = FireworkEffect.builder()
                        .withColor(color)
                        .flicker(existingEffect.hasFlicker())
                        .trail(existingEffect.hasTrail())
                        .with(existingType == null ? FireworkEffect.Type.BALL : existingType)
                        .build();
                effectMeta.setEffect(fireworkEffect);
            }
        } else if (meta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta)meta;
            originalColor = CompatibilityLib.getCompatibilityUtils().getColor(potionMeta);
            CompatibilityLib.getCompatibilityUtils().setColor(potionMeta, color);
        } else {
            return null;
        }
        Wand wand = controller.getIfWand(itemStack);
        itemStack.setItemMeta(meta);
        if (wand != null) {
            wand.setIcon(new MaterialAndData(itemStack));
        }
        return originalColor;
    }

    @Override
    public boolean requiresTargetEntity() {
        return true;
    }

    @Override
    public boolean isUndoable() {
        return true;
    }
}
