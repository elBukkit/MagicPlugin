package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
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
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.CompatibilityUtils;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import com.elmakers.mine.bukkit.utility.NMSUtils;

public class WearAction extends BaseSpellAction
{
    private MaterialAndData material;
    private ItemStack item;
    private boolean useItem;
    private Map<Enchantment, Integer> enchantments;
    private int slotNumber;
    private boolean unbreakable = true;
    private boolean returnOnFinish = false;
    private Mage targetMage = null;

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

    private class WearUndoAction implements Runnable
    {
        public WearUndoAction() {
        }

        @Override
        public void run() {
            returnItem();
        }
    }

    private void returnItem() {
        if (targetMage == null) return;
        Player player = targetMage.getPlayer();
        if (player == null) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack currentItem = armor[slotNumber];
        if (NMSUtils.isTemporary(currentItem)) {
            ItemStack replacement = NMSUtils.getReplacement(currentItem);
            armor[slotNumber] = replacement;
            player.getInventory().setArmorContents(armor);
        }
        if (targetMage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            ((com.elmakers.mine.bukkit.magic.Mage)targetMage).armorUpdated();
        }
        targetMage = null;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        material = ConfigurationUtils.getMaterialAndData(parameters, "material");
        item = context.getController().createItem(parameters.getString("item"));
        useItem = parameters.getBoolean("use_item", false);
        slotNumber = parameters.getInt("armor_slot", 3);
        slotNumber = Math.max(Math.min(slotNumber, 3), 0);
        unbreakable = parameters.getBoolean("unbreakable", true);
        returnOnFinish = parameters.getBoolean("return_on_finish", false);
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        Entity entity = context.getTargetEntity();
        if (entity == null) {
            if (!context.getTargetsCaster()) return SpellResult.NO_TARGET;
            entity = context.getEntity();
        }
        if (entity == null || !(entity instanceof Player))
        {
            return SpellResult.NO_TARGET;
        }

        Player player = (Player)entity;
        MaterialAndData material = this.material;
        MageController controller = context.getController();
        Mage mage = controller.getMage(player);
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
            return SpellResult.CAST;
        }

        ItemStack wearItem = null;
        String materialName = null;
        if (item == null)
        {
            if (material == null && (context.getSpell().usesBrush() || context.getSpell().hasBrushOverride())) {
                material = context.getBrush();
            }

            if (material == null || material.getMaterial() == Material.AIR) {
                return SpellResult.NO_TARGET;
            }

            wearItem = material.getItemStack(1);
            materialName = material.getName();
        }
        else
        {
            wearItem = InventoryUtils.getCopy(item);
            materialName = context.getController().describeItem(wearItem);
        }

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

        targetMage = mage;
        context.registerForUndo(new WearUndoAction());

        if (mage instanceof com.elmakers.mine.bukkit.magic.Mage) {
            ((com.elmakers.mine.bukkit.magic.Mage)mage).armorUpdated();
        }
        return SpellResult.CAST;
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        if (returnOnFinish) {
            returnItem();
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
