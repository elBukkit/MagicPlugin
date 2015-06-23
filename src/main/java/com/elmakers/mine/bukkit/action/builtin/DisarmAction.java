package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.elmakers.mine.bukkit.wand.Wand;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class DisarmAction extends BaseSpellAction
{
	private class DisarmUndoAction implements Runnable
	{
		private final Mage mage;
		private final ItemStack targetItem;
		private final ItemStack swapItem;
		private final int originalSlot;
		private final int targetSlot;

		public DisarmUndoAction(Mage mage, ItemStack targetItem, int originalSlot, ItemStack swapItem, int targetSlot) {
			this.mage = mage;
			this.targetItem = targetItem;
			this.swapItem = swapItem;
			this.originalSlot = originalSlot;
			this.targetSlot = targetSlot;
		}

		@Override
		public void run() {
			Player player = mage.getPlayer();
			if (player == null) return;
			PlayerInventory inventory = player.getInventory();
			if (inventory.getHeldItemSlot() != originalSlot) return;
			ItemStack currentTargetItem = inventory.getItem(targetSlot);
			ItemStack currentOriginalItem = inventory.getItem(originalSlot);

			if (currentTargetItem == null && targetItem != null) return;
			if (currentTargetItem != null && targetItem == null) return;
			if (currentOriginalItem == null && swapItem != null) return;
			if (currentOriginalItem != null && swapItem == null) return;
			if (!currentOriginalItem.equals(swapItem)) return;
			if (!currentTargetItem.equals(targetItem)) return;
			inventory.setItemInHand(targetItem);
			inventory.setItem(targetSlot, swapItem);
			if (Wand.isWand(targetItem)) {
				if (mage != null) {
					mage.activateWand();
				}
			}
		}
	}

    private boolean keepInInventory;
	private int minSlot;
	private int maxSlot;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
		keepInInventory = parameters.getBoolean("keep_in_inventory", false);
		minSlot = parameters.getInt("min_slot", Wand.HOTBAR_SIZE);
		maxSlot = parameters.getInt("max_slot", Wand.INVENTORY_SIZE + Wand.HOTBAR_SIZE- 1);
    }

	@Override
	public SpellResult perform(CastContext context)
	{
		Entity target = context.getTargetEntity();
		if (target == null || !(target instanceof LivingEntity)) {
			return SpellResult.NO_TARGET;
		}
		LivingEntity entity = (LivingEntity)target;

		EntityEquipment equipment = entity.getEquipment();
		ItemStack stack = equipment.getItemInHand();

		if (stack == null || stack.getType() == Material.AIR)
		{
			return SpellResult.NO_TARGET;
		}

		// Special case for wands
		MageController controller = context.getController();
		Mage targetMage = controller.getMage(entity);
		if (Wand.isWand(stack) && controller.isMage(entity)) {
			Mage mage = context.getMage();

			// Check for protected players (admins, generally...)
			// This gets overridden by superpower...
			if (!mage.isSuperPowered() && targetMage.isSuperProtected()) {
				return SpellResult.NO_TARGET;
			}

			if (targetMage != null && targetMage.getActiveWand() != null) {
				targetMage.getActiveWand().deactivate();
			}
		}

		Integer targetSlot = null;
		Integer originalSlot = null;
		PlayerInventory targetInventory = null;
		ItemStack swapItem = null;
		Random random = context.getRandom();
		if (entity instanceof Player && keepInInventory) {
			Player targetPlayer = (Player)entity;
			targetInventory = targetPlayer.getInventory();
			originalSlot = targetInventory.getHeldItemSlot();
			List<Integer> validSlots = new ArrayList<Integer>();
			ItemStack[] contents = targetInventory.getContents();

			for (int i = minSlot; i <= maxSlot; i++) {
				if (contents[i] == null || contents[i].getType() == Material.AIR) {
					validSlots.add(i);
				}
			}

			// Randomly choose a slot if no empty one was found
			if (validSlots.size() == 0) {
				int swapSlot = random.nextInt(maxSlot - minSlot) + minSlot;
				swapItem = targetInventory.getItem(swapSlot);
				validSlots.add(swapSlot);
			}

			int chosen = random.nextInt(validSlots.size());
			targetSlot = validSlots.get(chosen);
		}

		equipment.setItemInHand(swapItem);
		if (targetSlot != null && targetInventory != null) {
			targetInventory.setItem(targetSlot, stack);
			if (originalSlot != null && targetMage != null) {
				DisarmUndoAction disarmUndo = new DisarmUndoAction(targetMage, stack, originalSlot, swapItem, targetSlot);
				context.registerForUndo(disarmUndo);
			}
		} else {
			Location location = entity.getLocation();
			location.setY(location.getY() + 1);
			Item item = entity.getWorld().dropItemNaturally(location, stack);
			Vector velocity = item.getVelocity();
			velocity.setY(velocity.getY() * 5);
			item.setVelocity(velocity);
		}

		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return true;
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }

	@Override
	public void getParameterNames(Spell spell, Collection<String> parameters) {
		super.getParameterNames(spell, parameters);
		parameters.add("keep_in_inventory");
	}

	@Override
	public void getParameterOptions(Spell spell, String parameterKey, Collection<String> examples) {
		if (parameterKey.equals("keep_in_inventory")) {
			examples.addAll(Arrays.asList((BaseSpell.EXAMPLE_BOOLEANS)));
		} else {
			super.getParameterOptions(spell, parameterKey, examples);
		}
	}
}
