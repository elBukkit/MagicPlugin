package com.elmakers.mine.bukkit.traders;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.utility.InventoryUtils;
import net.dandielo.core.items.dItem;
import net.dandielo.core.items.serialize.Attribute;
import net.dandielo.core.items.serialize.ItemAttribute;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

@Attribute(name="Magic Material Brush", key="magic_brush", priority = 5)
public class BrushAttr extends ItemAttribute
{
	private String brushKey;

    public BrushAttr(dItem item, String key) {
        super(item, key);
    }

	@Override
	public String serialize() {
		if (brushKey == null) brushKey = "";
		if (TradersController.DEBUG) Bukkit.getLogger().info("[BRUSH] serialize: " + brushKey);
		return brushKey.replace(":", "|");
	}

	@Override
	public boolean deserialize(String s) {
		brushKey = s;
		if (brushKey != null) {
			brushKey = brushKey.replace("|", ":");;
		}
		return brushKey != null;
	}

	@Override
	public boolean onRefactor(ItemStack itemStack)
	{
		brushKey = null;
		if (itemStack != null)
		{
			MagicAPI api = MagicPlugin.getAPI();
			this.brushKey = api.getBrush(itemStack);
		}
		return this.brushKey != null;
	}
	
	@Override
	public void onAssign(ItemStack itemStack, boolean unused)
	{
		if (brushKey != null && !brushKey.isEmpty())
		{
			InventoryUtils.setMeta(itemStack, "brush", brushKey);
			if (TradersController.DEBUG) Bukkit.getLogger().info("[BRUSH] onAssign for: " + brushKey);
		}
	}

	/**
	 * Called when a week equality is needed. Allows sometimes a value to be in range of another value, used for priority requests
	 * @return
	 *    true when equal, false instead
	 */
	@Override
    public boolean equals(ItemAttribute other)
	{
		if (other instanceof BrushAttr)
		{
			return brushKey.equalsIgnoreCase(((BrushAttr)other).brushKey);
		}
		return false;
	}

	/**
	 * Called when a strong equality is needed. Values are compared strict.
	 * @return
	 *    true when equal, false instead
	 */
	@Override
    public boolean similar(ItemAttribute other)
	{
		if (other instanceof BrushAttr)
		{
			return brushKey.equalsIgnoreCase(((BrushAttr)other).brushKey);
		}
		return false;
	}
}
