package com.elmakers.mine.bukkit.traders;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import net.dandielo.core.items.dItem;
import net.dandielo.core.items.serialize.Attribute;
import net.dandielo.core.items.serialize.ItemAttribute;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

@Attribute(name="Magic Spell", key="magic_spell", priority = 5)
public class SpellAttr extends ItemAttribute
{
	private String spellKey;

    public SpellAttr(dItem item, String key) {
        super(item, key);
    }
	
	@Override
	public void onAssign(ItemStack itemStack, boolean unused)
	{
		if (itemStack != null && spellKey != null && !spellKey.isEmpty())
		{
			InventoryUtils.setMeta(itemStack, "spell", spellKey);
            if (TradersController.DEBUG) Bukkit.getLogger().info("[SPELL] onAssign for: " + spellKey);
		}
	}
	
	@Override
	public boolean onRefactor(ItemStack itemStack)
	{
		spellKey = null;
		if (itemStack != null)
		{
			MagicAPI api = MagicPlugin.getAPI();
			spellKey = api.getSpell(itemStack);
		}
		return spellKey != null;
	}

	/**
	 * Called when a week equality is needed. Allows sometimes a value to be in range of another value, used for priority requests
	 * @return
	 *    true when equal, false instead
	 */
	public boolean similar(ItemAttribute other)
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
	public boolean equals(ItemAttribute other)
	{
		if (other instanceof SpellAttr)
		{
			return spellKey.equalsIgnoreCase(((SpellAttr)other).spellKey);
		}
		return false;
	}

	@Override
	public String serialize() {
		return spellKey;
	}

	@Override
	public boolean deserialize(String s) {
		spellKey = s;
		return spellKey != null;
	}
}
