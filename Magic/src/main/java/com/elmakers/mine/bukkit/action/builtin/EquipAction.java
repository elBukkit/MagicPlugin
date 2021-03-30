package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.block.MaterialAndData;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.block.DefaultMaterials;
import com.elmakers.mine.bukkit.item.InventorySlot;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class EquipAction extends BaseSpellAction
{
    private MaterialAndData material;
    private ItemStack item;
    private boolean useItem;
    private Map<Enchantment, Integer> enchantments;
    private InventorySlot slot;
    private boolean unbreakable = true;
    private boolean returnOnFinish = false;
    private boolean makeTemporary = true;
    private boolean replaceItem = false;
    private WearUndoAction undoAction;

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        if (parameters.contains("enchantments"))
        {
            enchantments = new HashMap<>();
            ConfigurationSection enchantConfig = ConfigurationUtils.getConfigurationSection(parameters, "enchantments");
            Collection<String> enchantKeys = enchantConfig.getKeys(false);
            for (String enchantKey : enchantKeys)
            {
                try {
                    Enchantment enchantment = Enchantment.getByName(enchantKey.toUpperCase());
                    enchantments.put(enchantment, enchantConfig.getInt(enchantKey));
                } catch (Exception ex) {
                    spell.getController().getLogger().warning("Invalid enchantment: " + enchantKey);
                }
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        if (slot == null) {
            return SpellResult.FAIL;
        }
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            if (!context.getTargetsCaster()) return SpellResult.NO_TARGET;
            entity = context.getEntity();
        }
        if (entity == null) {
            return SpellResult.NO_TARGET;
        }
        if (!(entity instanceof LivingEntity)) {
            return SpellResult.LIVING_ENTITY_REQUIRED;
        }

        EntityEquipment equipment = ((LivingEntity)entity).getEquipment();
        MaterialAndData material = this.material;
        MageController controller = context.getController();
        Mage mage = controller.getMage(entity);
        ItemStack equipItem = null;

        // Find or create the item to wear
        if (useItem) {
            Wand activeWand = mage.getActiveWand();
            // Check for trying to wear an item from the offhand slot
            // Not handling this for now.
            if (activeWand != context.getWand()) {
                return SpellResult.NO_TARGET;
            }

            if (activeWand != null) {
                activeWand.deactivate();
            }

            ItemStack itemInHand = equipment.getItemInMainHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                return SpellResult.FAIL;
            }
            equipItem = itemInHand;
        } else {
            String materialName = null;

            // Create an item as a copy of an existing template
            if (item != null) {
                equipItem = InventoryUtils.getCopy(item);
                materialName = context.getController().describeItem(equipItem);
            } else {
                // Otherwise create a new item from a material name, falling back to the brush
                if (material == null && (context.getSpell().usesBrush() || context.getSpell().hasBrushOverride())) {
                    material = context.getBrush();
                }
                // And then falling back to the target block
                if (material == null) {
                    Block targetBlock = context.getTargetBlock();
                    if (targetBlock != null)
                    {
                        material = new com.elmakers.mine.bukkit.block.MaterialAndData(targetBlock);
                        material.setMaterial(DefaultMaterials.blockToItem(material.getMaterial()));
                    }
                }

                // If we didn't end up with a valid materia, exit
                if (material == null || DefaultMaterials.isAir(material.getMaterial())) {
                    return SpellResult.NO_TARGET;
                }

                equipItem = material.getItemStack(1);
                materialName = material.getName(context.getController().getMessages());
            }

            if (DefaultMaterials.isAir(equipItem.getType())) {
                return SpellResult.NO_TARGET;
            }

            // Set custom name and other information on created item
            ItemMeta meta = equipItem.getItemMeta();

            // Legacy support
            String displayName = context.getMessage("hat_name", "");
            displayName = context.getMessage("wear_name", displayName);
            if (materialName == null || materialName.isEmpty())
            {
                materialName = "?";
            }
            if (displayName != null && !displayName.isEmpty())
            {
                meta.setDisplayName(displayName.replace("$hat", materialName).replace("$item", materialName));
            }
            List<String> lore = new ArrayList<>();
            String loreLine = context.getMessage("hat_lore");
            loreLine = context.getMessage("wear_lore", loreLine);
            lore.add(loreLine);
            meta.setLore(lore);
            equipItem.setItemMeta(meta);
            equipItem = InventoryUtils.makeReal(equipItem);
            if (makeTemporary) {
                NMSUtils.makeTemporary(equipItem, context.getMessage("removed").replace("$hat", materialName).replace("$item", materialName));
            }
            if (enchantments != null) {
                equipItem.addUnsafeEnchantments(enchantments);
            }
            if (unbreakable) {
                CompatibilityUtils.makeUnbreakable(equipItem);
            }
        }

        // Find the target slot and see if there's an existing item in there
        ItemStack existingItem = null;
        int slotNumber = -1;

        // If replacing the main hand item we're going to deactivate the wand
        if (slot == InventorySlot.MAIN_HAND) {
            Wand activeWand = mage.getActiveWand();
            if (activeWand != null) {
                activeWand.deactivate();
            }
        }

        // Get the slot number and the item that is there now
        slotNumber = slot.getSlot(mage);
        if (slotNumber == -1) {
            context.getLogger().warning("Invalid slot for Wear action: " + slot);
            return SpellResult.FAIL;
        }
        existingItem = mage.getItem(slotNumber);

        // Decide what to do with the item in the slot being replaced
        if (!CompatibilityUtils.isEmpty(existingItem) && !replaceItem) {
            if (NMSUtils.isTemporary(existingItem)) {
                ItemStack replacement = NMSUtils.getReplacement(existingItem);
                if (replacement != null) {
                    existingItem = replacement;
                }
            }

            // If we were equipping the wand, then just replace the main hand item.
            // Otherwise, store the item inside the new item, to be returned on click or when
            // the spell finished
            if (useItem) {
                equipment.setItemInMainHand(existingItem);
            } else {
                NMSUtils.setReplacement(equipItem, existingItem);
            }
        } else if (useItem) {
            // If we didn't swap the wand out with the item in the target slot, we need to clear the main hand
            // slot since we're going to equip that item and don't want to dupe it
            equipment.setItemInMainHand(new ItemStack(Material.AIR));
        }

        // Put the new item in the target slot
        mage.setItem(slotNumber, equipItem);

        undoAction = new WearUndoAction(mage, slotNumber);
        context.registerForUndo(undoAction);

        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            com.elmakers.mine.bukkit.magic.Mage implMage = ((com.elmakers.mine.bukkit.magic.Mage)mage);
            implMage.armorUpdated();
            implMage.checkWandNextTick();
        }
        return SpellResult.CAST;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        material = ConfigurationUtils.getMaterialAndData(parameters, "material");
        item = context.getController().createItem(parameters.getString("item"));
        String slotName = parameters.getString("slot");
        if (slotName != null && !slotName.isEmpty()) {
            try {
                slot = InventorySlot.valueOf(slotName.toUpperCase());
            } catch (Exception ex) {
                context.getLogger().warning("Invalid slot in Wear action: " + slotName);
            }
        } else {
            slot = InventorySlot.getArmorSlot(parameters.getInt("armor_slot", 3));
        }

        useItem = parameters.getBoolean("use_item", false);
        unbreakable = parameters.getBoolean("unbreakable", true);
        returnOnFinish = parameters.getBoolean("return_on_finish", false);
        makeTemporary = parameters.getBoolean("temporary", true);
        replaceItem = parameters.getBoolean("replace_item", false);
    }

    private static class WearUndoAction implements Runnable {
        private final WeakReference<Mage> mage;
        private final int slotNumber;
        private boolean returned = false;

        public WearUndoAction(Mage mage, int slotNumber) {
            this.mage = new WeakReference<>(mage);
            this.slotNumber = slotNumber;
        }

        private void returnItem() {
            if (returned) return;
            returned = true;
            Mage mage = this.mage.get();
            if (mage == null) return;

            ItemStack currentItem = mage.getItem(slotNumber);
            if (NMSUtils.isTemporary(currentItem)) {
                ItemStack replacement = NMSUtils.getReplacement(currentItem);
                mage.setItem(slotNumber, replacement);
            }
            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                com.elmakers.mine.bukkit.magic.Mage implMage = ((com.elmakers.mine.bukkit.magic.Mage)mage);
                implMage.armorUpdated();
                implMage.checkWandNextTick();
            }
        }

        @Override
        public void run() {
            returnItem();
        }
    }

    @Override
    public void reset(CastContext context) {
        super.reset(context);
        // Prevent these getting lost
        if (returnOnFinish && undoAction != null) {
            undoAction.returnItem();
        }
        undoAction = null;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        if (returnOnFinish && undoAction != null) {
            undoAction.returnItem();
        }
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("material");
        parameters.add("item");
        parameters.add("use_item");
        parameters.add("armor_slot");
        parameters.add("slot");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("material")) {
            examples.addAll(MagicPlugin.getAPI().getBrushes());
        } else if (parameterKey.equals("item")) {
            for (Material material : Material.values()) {
                examples.add(material.name().toLowerCase());
            }
            Collection<String> allItems = spell.getController().getItemKeys();
            for (String itemKey : allItems) {
                examples.add(itemKey);
            }
        } else if (parameterKey.equals("armor_slot")) {
            examples.add("0");
            examples.add("1");
            examples.add("2");
            examples.add("3");
        } else if (parameterKey.equals("slot")) {
            for (InventorySlot slot : InventorySlot.values()) {
                examples.add(slot.name().toLowerCase());
            }
        } else if (parameterKey.equals("use_item")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }
}
