package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;

public class StashWandAction extends BaseSpellAction
{
    private ItemStack stashedItem;
    private Mage targetMage;
    private int slotNumber;
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
        if (targetMage == null || stashedItem == null) return;
        Player player = targetMage.getPlayer();
        if (player == null) return;

        ItemStack existing = player.getInventory().getItem(slotNumber);
        if (existing == null || existing.getType() == Material.AIR) {
            player.getInventory().setItem(slotNumber, stashedItem);
        } else {
            targetMage.giveItem(stashedItem);
        }
        targetMage.checkWand();
        stashedItem = null;
        targetMage = null;
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

        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand == null || itemInHand.getType() == Material.AIR)
        {
            return SpellResult.FAIL;
        }

        Wand activeWand = mage.getActiveWand();

        // Check for trying to stash an item in the offhand slot
        // Not handling this for now.
        if (activeWand != context.getWand()) {
            return SpellResult.NO_TARGET;
        }

        if (activeWand != null) {
            activeWand.deactivate();
        }

        slotNumber = player.getInventory().getHeldItemSlot();
        stashedItem = player.getInventory().getItemInMainHand();

        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));

        targetMage = mage;
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
