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
import org.bukkit.entity.Player;
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
import com.elmakers.mine.bukkit.item.ArmorSlot;
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
    private int slotNumber;
    private boolean isArmorSlot;
    private boolean unbreakable = true;
    private boolean returnOnFinish = false;
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

    private static class WearUndoAction implements Runnable
    {
        private final MageController controller;
        private final WeakReference<Player> player;
        private final int slotNumber;
        private boolean returned = false;

        public WearUndoAction(MageController controller, Player player, int slotNumber) {
            this.controller = controller;
            this.player = new WeakReference<>(player);
            this.slotNumber = slotNumber;
        }

        private void returnItem() {
            if (returned) return;
            returned = true;
            Player player = this.player.get();
            if (player == null) return;

            Mage targetMage = controller.getRegisteredMage(player);
            ItemStack currentItem = targetMage.getInventory().getItem(slotNumber);
            if (NMSUtils.isTemporary(currentItem)) {
                ItemStack replacement = NMSUtils.getReplacement(currentItem);
                if (player.isDead()) {
                    targetMage.giveItem(replacement);
                } else {
                    targetMage.getInventory().setItem(slotNumber, replacement);
                }
            }
            targetMage.checkWand();
            if (targetMage != null && targetMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)targetMage).armorUpdated();
            }
        }

        @Override
        public void run() {
            returnItem();
        }
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        material = ConfigurationUtils.getMaterialAndData(parameters, "material");
        item = context.getController().createItem(parameters.getString("item"));
        String slotName = parameters.getString("slot");
        if (slotName != null && !slotName.isEmpty()) {
            try {
                ArmorSlot slot = ArmorSlot.valueOf(slotName.toUpperCase());
                slotNumber = slot.getArmorSlot();
            } catch (Exception ex) {
                context.getLogger().warning("Invalid slot in Wear action: " + slotName);
            }
        } else {
            // TODO :Support for legacy armor-specific slot
            // ArmorSlot inventorySlot = new Armorslot(parameters.getInt("armor_slot", 3));
        }

        useItem = parameters.getBoolean("use_item", false);
        unbreakable = parameters.getBoolean("unbreakable", true);
        returnOnFinish = parameters.getBoolean("return_on_finish", false);
    }

    @Override
    public SpellResult perform(CastContext context) {
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            if (!context.getTargetsCaster()) return SpellResult.NO_TARGET;
            entity = context.getEntity();
        }
        if (entity == null || !(entity instanceof Player)) {
            return SpellResult.NO_TARGET;
        }

        Player player = (Player)entity;
        MaterialAndData material = this.material;
        MageController controller = context.getController();
        Mage mage = controller.getMage(player);
        ItemStack wearItem = null;

        // Find or create the item to wear
        if (useItem)
        {
            Wand activeWand = mage.getActiveWand();
            // Check for trying to wear an item from the offhand slot
            // Not handling this for now.
            if (activeWand != context.getWand()) {
                return SpellResult.NO_TARGET;
            }

            if (activeWand != null) {
                activeWand.deactivate();
            }

            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand == null || itemInHand.getType() == Material.AIR)
            {
                return SpellResult.FAIL;
            }
            wearItem = itemInHand;
        } else {
            String materialName = null;

            // Create an item as a copy of an existing template
            if (item != null) {
                wearItem = InventoryUtils.getCopy(item);
                materialName = context.getController().describeItem(wearItem);
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

                wearItem = material.getItemStack(1);
                materialName = material.getName();
            }

            if (DefaultMaterials.isAir(wearItem.getType())) {
                return SpellResult.NO_TARGET;
            }

            // Set custom name and other information on created item
            ItemMeta meta = wearItem.getItemMeta();

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
            wearItem.setItemMeta(meta);
            wearItem = InventoryUtils.makeReal(wearItem);
            NMSUtils.makeTemporary(wearItem, context.getMessage("removed").replace("$hat", materialName).replace("$item", materialName));
            if (enchantments != null) {
                wearItem.addUnsafeEnchantments(enchantments);
            }
            if (unbreakable) {
                CompatibilityUtils.makeUnbreakable(wearItem);
            }
        }

        // Find the target slot and see if there's an existing item in there
        if (isArmorSlot) {

        } else {

        }



            ItemStack[] armor = player.getInventory().getArmorContents();
            ItemStack itemStack = armor[slotNumber];
            if (itemStack != null && itemStack.getType() != Material.AIR)
            {
                if (NMSUtils.isTemporary(itemStack))
                {
                    ItemStack replacement = NMSUtils.getReplacement(itemStack);
                    if (replacement != null) {
                        itemStack = replacement;
                    }
                }
                NMSUtils.setReplacement(wearItem, itemStack);
            }

        // TODO: Fix this
        if (useItem) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            ItemStack currentItem = armor[slotNumber];
            armor[slotNumber] = itemInHand;

            player.getInventory().setArmorContents(armor);
            if (!InventoryUtils.isTemporary(currentItem)) {
                player.getInventory().setItemInMainHand(currentItem);
            } else {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }

            if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
                ((com.elmakers.mine.bukkit.magic.Mage)mage).armorUpdated();
            }
        }

        armor[slotNumber] = wearItem;
        player.getInventory().setArmorContents(armor);

        // Sanity check to make sure the block was allowed to be created
        armor = player.getInventory().getArmorContents();
        ItemStack helmetItem = armor[slotNumber];
        if (!NMSUtils.isTemporary(helmetItem)) {
            armor[slotNumber] = itemStack;
            player.getInventory().setArmorContents(armor);
            return SpellResult.NO_TARGET;
        }

        undoAction = new WearUndoAction(controller, player, slotNumber, isArmorSlot);
        context.registerForUndo(undoAction);

        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            ((com.elmakers.mine.bukkit.magic.Mage)mage).armorUpdated();
        }
        return SpellResult.CAST;
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
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("material");
        parameters.add("item");
        parameters.add("use_item");
        parameters.add("armor_slot");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
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
