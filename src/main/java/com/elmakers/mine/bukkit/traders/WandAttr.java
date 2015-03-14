package com.elmakers.mine.bukkit.traders;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.wand.Wand;
import net.dandielo.citizens.traders_v3.core.exceptions.InvalidItemException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidValueException;
import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeValueNotFoundException;
import net.dandielo.citizens.traders_v3.utils.items.Attribute;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.logging.Level;

@Attribute(name="Magic Wand", key="magic_wand", priority = 5)
public class WandAttr extends ItemAttr
{
	private String wandData;

    public WandAttr(String key) {
        super(key);
    }

	@Override
	public void onFactorize(ItemStack itemStack) throws AttributeValueNotFoundException 
	{
        MagicAPI api = MagicPlugin.getAPI();
        if (!api.isWand(itemStack))
		{
			throw new AttributeValueNotFoundException();
		}
        Wand wand = new Wand(TradersController.getController(), itemStack);
        YamlConfiguration saveData = new YamlConfiguration();
        wand.saveProperties(saveData);
        wandData = saveData.saveToString();

        // Make sure these don't stack!
        this.item.addFlag(".nostack");
		if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] onFactorize: " + wandData);
	}

	@Override
	public void onLoad(String itemData) throws AttributeInvalidValueException
	{
		if (itemData == null || itemData.isEmpty()) {
			throw new AttributeInvalidValueException(this.info, "No data");
		}
		this.wandData = itemData
                .replace("{sp}", " ")
                .replace("{co}", ":")
                .replace("{cr}", "\n");;
		if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] onLoad: " + itemData);
	}

	@Override
	public String onSave() 
	{
		if (wandData == null) wandData = "";
        String escaped = wandData
                .replace(" ", "{sp}")
                .replace(":", "{co}")
                .replace("\n", "{cr}");
		if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] onSave: " + escaped);
        return escaped;
	}
	
	@Override
	public void onAssign(ItemStack itemStack) throws InvalidItemException
	{
		onReturnAssign(itemStack, false);
	}
	
	@Override
	public ItemStack onReturnAssign(ItemStack itemStack, boolean endItem) throws InvalidItemException
	{
		if (itemStack == null) throw new InvalidItemException();

        if (wandData != null && !wandData.isEmpty())
        {
            Wand wand = Wand.createWand(TradersController.getController(), itemStack);
            YamlConfiguration saveData = new YamlConfiguration();
            try {
                ItemMeta meta = itemStack.getItemMeta();
                saveData.loadFromString(wandData);
                if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] loading keys: " + StringUtils.join(saveData.getKeys(false), ","));
                wand.loadProperties(saveData);
                wand.save();
                itemStack = wand.getItem();
                if (!endItem && meta != null) {
                    ItemMeta newMeta = itemStack.getItemMeta();
                    newMeta.setLore(meta.getLore());
                    itemStack.setItemMeta(newMeta);
                }
            } catch (InvalidConfigurationException ex) {
                TradersController.getController().getLogger().log(Level.WARNING, "Error deserializing wand data", ex);
            }
            if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] onReturnAssign for: " + wandData);
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
		if (other instanceof WandAttr)
		{
			return wandData.equals(((WandAttr)other).wandData);
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
		if (other instanceof WandAttr)
		{
			return wandData.equals(((WandAttr) other).wandData);
        }
        return false;
	}
}
