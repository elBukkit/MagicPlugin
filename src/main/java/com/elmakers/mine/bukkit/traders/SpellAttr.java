package com.elmakers.mine.bukkit.traders;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidValueException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Attribute(name="Magic Spell", key="magic_spell", priority = 5)
public class SpellAttr extends ItemAttr
{
	private String spellKey;

    public SpellAttr(String key) {
        super(key);
    }

	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException 
	{
        MagicAPI api = MagicPlugin.getAPI();
        String spellKey = api.getSpell(itemStack);
        if (spellKey == null)
		{
			throw new AttributeValueNotFoundException();
		}
        this.spellKey = spellKey;

        // Make sure these don't stack!
        this.item.addFlag(".nostack");
		if (TradersController.DEBUG) Bukkit.getLogger().info("[SPELL] onFactorize: " + spellKey);
	}

	@Override
	public void onLoad(String itemData) throws AttributeInvalidValueException
	{
		if (itemData == null || itemData.isEmpty()) {
			throw new AttributeInvalidValueException(this.info, "No data");
		}
		this.spellKey = itemData;
		if (TradersController.DEBUG) Bukkit.getLogger().info("[SPELL] onLoad: " + itemData);
	}

	@Override
	public String onSave() 
	{
		if (spellKey == null) spellKey = "";
		if (TradersController.DEBUG) Bukkit.getLogger().info("[SPELL] onSave: " + spellKey);
		return spellKey;
	}
	
	@Override
	public void onAssign(ItemStack itemStack) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		
		if (spellKey != null && !spellKey.isEmpty())
		{
			InventoryUtils.setMeta(itemStack, "spell", spellKey);

            if (TradersController.DEBUG) Bukkit.getLogger().info("[SPELL] onAssign for: " + spellKey);
		}
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();

        if (spellKey != null && !spellKey.isEmpty())
        {
            MagicAPI api = MagicPlugin.getAPI();
            ItemMeta meta = itemStack.getItemMeta();
			itemStack = api.createSpellItem(spellKey);
            if (!endItem && meta != null) {
                ItemMeta newMeta = itemStack.getItemMeta();
                newMeta.setLore(meta.getLore());
                itemStack.setItemMeta(newMeta);
            }

            if (TradersController.DEBUG) Bukkit.getLogger().info("[SPELL] onReturnAssign for: " + spellKey);
		}
		
		return itemStack;
	}

	/**
	 * Called when a week equality is needed. Allows sometimes a value to be in range of another value, used for priority requests
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsWeak(ItemAttr other)
	{
		if (other instanceof SpellAttr)
		{
			return spellKey.equalsIgnoreCase(((SpellAttr)other).spellKey);
		}
		return false;
	}

	/**
	 * Called when a strong equality is needed. Values are compared strict.
	 * @return
	 *    true when equal, false instead 
	 */
	public boolean equalsStrong(ItemAttr other)
	{
		if (other instanceof SpellAttr)
		{
			return spellKey.equalsIgnoreCase(((SpellAttr)other).spellKey);
		}
		return false;
	}
}
