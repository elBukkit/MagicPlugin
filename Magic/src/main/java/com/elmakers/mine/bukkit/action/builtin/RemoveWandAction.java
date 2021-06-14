package com.elmakers.mine.bukkit.action.builtin;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.platform.ItemUtils;

public class RemoveWandAction extends BaseSpellAction
{
    @Override
    public SpellResult perform(CastContext context)
    {
        Mage mage = context.getMage();
        Player player = mage.getPlayer();
        if (player == null) {
            return SpellResult.PLAYER_REQUIRED;
        }

        Wand activeWand = mage.getActiveWand();
        Wand offhandWand = mage.getOffhandWand();

        // Check for trying to remove an item in the offhand slot
        ItemStack activeItem = null;
        boolean isOffhand = false;
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

        if (ItemUtils.isEmpty(activeItem))
        {
            return SpellResult.FAIL;
        }

        if (activeWand != null) {
            activeWand.deactivate();
        }

        if (isOffhand) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }
        mage.checkWand();
        return SpellResult.CAST;
    }
}
