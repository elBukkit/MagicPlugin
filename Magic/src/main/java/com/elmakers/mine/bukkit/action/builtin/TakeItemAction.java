package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class TakeItemAction extends BaseSpellAction
{
	private String displayName;

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
		displayName = parameters.getString("display_name", null);
		if (displayName != null) {
			displayName = ChatColor.translateAlternateColorCodes('&', displayName);
		}
    }

	@Override
	public SpellResult perform(CastContext context)
	{
		Entity target = context.getTargetEntity();
		if (target == null) {
			return SpellResult.NO_TARGET;
		}

		ItemStack item = null;
		if (target instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity)target;
			if (displayName == null) {
				EntityEquipment equipment = livingEntity.getEquipment();
				item = equipment.getItemInMainHand();;
				equipment.setItemInMainHand(null);
			} else {
				if (!(target instanceof Player)) {
					return SpellResult.PLAYER_REQUIRED;
				}
				Player targetPlayer = (Player)target;
				PlayerInventory playerInventory = targetPlayer.getInventory();
				for (int i = 0; i < playerInventory.getSize(); i++) {
					ItemStack inventoryItem = playerInventory.getItem(i);
					if (InventoryUtils.isEmpty(inventoryItem)) continue;

					ItemMeta meta = inventoryItem.getItemMeta();
					if (meta == null || !meta.hasDisplayName()) continue;
					if (meta.getDisplayName().equals(displayName)) {
						item = inventoryItem;
						playerInventory.setItem(i, null);
						break;
					}
				}
			}
		} else if (target instanceof Item) {
			Item itemEntity = (Item)target;
			item = itemEntity.getItemStack();
			if (displayName != null) {
				ItemMeta itemMeta = item.getItemMeta();
				if (itemMeta == null || !itemMeta.hasDisplayName() || !itemMeta.getDisplayName().equals(displayName)) {
					item = null;
				}
			}
			if (item != null) {
				itemEntity.remove();
			}
		}

		if (InventoryUtils.isEmpty(item)) {
			return SpellResult.NO_TARGET;
		}

		context.getMage().giveItem(item);

		return SpellResult.CAST;
	}

	@Override
	public boolean isUndoable()
	{
		return false;
	}

    @Override
    public boolean requiresTargetEntity()
    {
        return true;
    }
}
