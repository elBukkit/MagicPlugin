package com.elmakers.mine.bukkit.action.builtin;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;

public class InventoryAction extends BaseSpellAction
{
    private InventoryType inventoryType;
    private String title;
    private boolean disposal = false;

    @Override
    public void processParameters(CastContext context, ConfigurationSection parameters)
    {
        super.processParameters(context, parameters);
        String inventoryTypeString = parameters.getString("type", "ender_chest").toUpperCase();
        if (inventoryTypeString.equals("ENDER")) {
            inventoryTypeString = "ENDER_CHEST";
        } else if (inventoryTypeString.equals("INVENTORY")) {
            inventoryTypeString = "CRAFTING";
        } else if (inventoryTypeString.equals("DISPOSAL")) {
            disposal = true;
            inventoryTypeString = "CHEST";
        }

        // For backwards-compatibility with disposal spell
        if (inventoryTypeString.equals("CHEST") && parameters.getString("target", "").equals("self")) {
            disposal = true;
        }

        title = parameters.getString("title");
        try {
            inventoryType = InventoryType.valueOf(inventoryTypeString);
        } catch (Exception ex) {
            context.getLogger().warning("Invalid inventory type in " + context.getSpell().getKey() + ": " + inventoryTypeString);
        }
    }

    @Override
    public SpellResult perform(CastContext context)
    {
        MageController controller = context.getController();
        Entity targetEntity = context.getTargetEntity();
        Mage showMage = context.getMage();
        Player showPlayer = showMage.getPlayer();
        if (showPlayer == null) return SpellResult.PLAYER_REQUIRED;

        // Make sure to close the player's wand
        Wand activeWand = showMage.getActiveWand();
        if (activeWand != null) {
            activeWand.closeInventory();
        }

        switch (inventoryType) {
            case CRAFTING:
                if (targetEntity == null || !(targetEntity instanceof Player)) {
                    return SpellResult.NO_TARGET;
                }
                Mage mage = controller.getMage(targetEntity);
                Inventory craftingInventory = mage.getInventory();
                showPlayer.openInventory(craftingInventory);
                break;
            case ENDER_CHEST:
                if (targetEntity == null || !(targetEntity instanceof HumanEntity)) {
                    return SpellResult.NO_TARGET;
                }
                HumanEntity humanTarget = (HumanEntity)targetEntity;
                Inventory enderInventory = humanTarget.getEnderChest();
                showPlayer.openInventory(enderInventory);
                break;
            case WORKBENCH:
                showPlayer.openWorkbench(null, true);
                break;
            case CHEST:
                if (disposal) {
                    showGenericInventory(showPlayer);
                    break;
                }
                Block targetBlock = context.getTargetBlock();
                if (targetBlock == null) return SpellResult.NO_TARGET;
                BlockState state = targetBlock.getState();
                if (!(state instanceof InventoryHolder)) return SpellResult.NO_TARGET;
                InventoryHolder holder = (InventoryHolder)state;
                showPlayer.openInventory(holder.getInventory());
                break;
            default:
                // Probably wont' work very well, but sure why not.
                showGenericInventory(showPlayer);
                break;
        }

        return SpellResult.CAST;
    }

    private void showGenericInventory(Player showPlayer) {
        Inventory inventory = title != null && !title.isEmpty()
                ? Bukkit.createInventory(showPlayer, inventoryType, title)
                : Bukkit.createInventory(showPlayer, inventoryType);
        showPlayer.openInventory(inventory);
    }

    @Override
    public boolean isUndoable()
    {
        return false;
    }

    @Override
    public boolean requiresTargetEntity()
    {
        return inventoryType == InventoryType.ENDER_CHEST || inventoryType == InventoryType.CRAFTING;
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters) {
        super.getParameterNames(spell, parameters);
        parameters.add("type");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
        if (parameterKey.equals("type")) {
            for (InventoryType invType : InventoryType.values()) {
                examples.add(invType.name().toLowerCase());
            }
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }
}
