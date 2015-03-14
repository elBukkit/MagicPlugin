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

@Attribute(name="Magic Material Brush", key="magic_brush", priority = 5)
public class BrushAttr extends ItemAttr
{
	private String brushKey;

    public BrushAttr(String key) {
        super(key);
    }

	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException 
	{
        MagicAPI api = MagicPlugin.getAPI();
        String brushKey = api.getBrush(itemStack);
        if (brushKey == null)
		{
			throw new AttributeValueNotFoundException();
		}
        this.brushKey = brushKey;

        // Make sure these don't stack!
        this.item.addFlag(".nostack");
		if (TradersController.DEBUG) Bukkit.getLogger().info("[BRUSH] onFactorize: " + brushKey);
	}

	@Override
	public void onLoad(String itemData) throws AttributeInvalidValueException
	{
		if (itemData == null || itemData.isEmpty()) {
			throw new AttributeInvalidValueException(this.info, "No data");
		}
		this.brushKey = itemData.replace("|", ":");;
		if (TradersController.DEBUG) Bukkit.getLogger().info("[BRUSH] onLoad: " + itemData);
	}

	@Override
	public String onSave() 
	{
		if (brushKey == null) brushKey = "";
		if (TradersController.DEBUG) Bukkit.getLogger().info("[BRUSH] onSave: " + brushKey);
		return brushKey.replace(":", "|");
	}
	
	@Override
	public void onAssign(ItemStack itemStack) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();
		
		if (brushKey != null && !brushKey.isEmpty())
		{
			InventoryUtils.setMeta(itemStack, "brush", brushKey);

            if (TradersController.DEBUG) Bukkit.getLogger().info("[BRUSH] onAssign for: " + brushKey);
		}
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();

        if (brushKey != null && !brushKey.isEmpty())
        {
            MagicAPI api = MagicPlugin.getAPI();
            ItemMeta meta = itemStack.getItemMeta();
            itemStack = api.createBrushItem(brushKey);
            if (!endItem && meta != null) {
                ItemMeta newMeta = itemStack.getItemMeta();
                newMeta.setLore(meta.getLore());
                itemStack.setItemMeta(newMeta);
            }

            if (TradersController.DEBUG) Bukkit.getLogger().info("[BRUSH] onReturnAssign for: " + brushKey);
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
	public boolean equalsStrong(ItemAttr other)
	{
		if (other instanceof BrushAttr)
		{
			return brushKey.equalsIgnoreCase(((BrushAttr)other).brushKey);
		}
		return false;
	}
}
