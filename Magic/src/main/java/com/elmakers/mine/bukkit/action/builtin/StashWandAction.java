package com.elmakers.mine.bukkit.action.builtin;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.utility.InventoryUtils;

public class StashWandAction extends BaseSpellAction
{
    private ItemStack stashedItem;
    private WeakReference<Mage> targetMage;
    private int slotNumber;
    private boolean isOffhand = false;
    private boolean returnOnFinish = true;

    private class StashWandUndoAction implements Runnable
    {
        public StashWandUndoAction() {
        }

        @Override
        public void run() {
            returnItem();
        }
    }

    private void returnItem() {
        Mage targetMage = this.targetMage == null ? null : this.targetMage.get();
        if (targetMage == null || stashedItem == null) return;
        Player player = targetMage.getPlayer();
        if (player == null) return;

        boolean gave = false;
        Wand activeWand = targetMage.getActiveWand();
        if (activeWand != null && activeWand.isInventoryOpen()) {
            gave = targetMage.addToStoredInventory(stashedItem);
        } else {
            if (isOffhand) {
                ItemStack existing = player.getInventory().getItemInOffHand();
                if (InventoryUtils.isEmpty(existing)) {
                    player.getInventory().setItemInOffHand(stashedItem);
                    gave = true;
                }
            } else {
                ItemStack existing = player.getInventory().getItem(slotNumber);
                if (InventoryUtils.isEmpty(existing)) {
                    player.getInventory().setItem(slotNumber, stashedItem);
                    gave = true;
                }
            }
        }
        if (!gave) {
            targetMage.giveItem(stashedItem);
        }
        targetMage.checkWand();
        stashedItem = null;
        this.targetMage = null;
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
        MageController controller = context.getController();
        Mage mage = controller.getMage(player);

        Wand activeWand = mage.getActiveWand();
        Wand offhandWand = mage.getOffhandWand();

        // Check for trying to stash an item in the offhand slot
        ItemStack activeItem = null;
        if (offhandWand == context.getWand()) {
            isOffhand = true;
            activeWand = offhandWand;
            activeItem = player.getInventory().getItemInOffHand();
        } else if (activeWand != context.getWand()) {
            return SpellResult.NO_TARGET;
        } else {
            isOffhand = false;
            activeItem = player.getInventory().getItemInMainHand();
        }

        if (InventoryUtils.isEmpty(activeItem))
        {
            return SpellResult.FAIL;
        }

        if (activeWand != null) {
            activeWand.deactivate();
        }

        slotNumber = player.getInventory().getHeldItemSlot();

        if (isOffhand) {
            stashedItem = player.getInventory().getItemInOffHand();
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        } else {
            stashedItem = player.getInventory().getItemInMainHand();
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        targetMage = new WeakReference<>(mage);
        context.registerForUndo(new StashWandUndoAction());
        return SpellResult.CAST;
    }

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        returnOnFinish = parameters.getBoolean("return_on_finish", false);
    }

    @Override
    public void getParameterNames(Spell spell, Collection<String> parameters)
    {
        super.getParameterNames(spell, parameters);
        parameters.add("return_on_finish");
    }

    @Override
    public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples)
    {
        if (parameterKey.equals("return_on_finish")) {
            examples.addAll(Arrays.asList(BaseSpell.EXAMPLE_BOOLEANS));
        } else {
            super.getParameterOptions(spell, parameterKey, examples);
        }
    }

    @Override
    public void finish(CastContext context) {
        super.finish(context);
        if (returnOnFinish) {
            returnItem();
        }
    }

    @Override
    public boolean isUndoable()
    {
        return true;
    }
}
