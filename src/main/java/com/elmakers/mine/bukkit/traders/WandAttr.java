package com.elmakers.mine.bukkit.traders;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;
import com.elmakers.mine.bukkit.magic.MagicPlugin;
import com.elmakers.mine.bukkit.wand.Wand;
import net.dandielo.core.items.dItem;
import net.dandielo.core.items.serialize.Attribute;
import net.dandielo.core.items.serialize.ItemAttribute;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

@Attribute(name="Magic Wand", key="magic_wand", priority = 5)
public class WandAttr extends ItemAttribute
{
	private String wandData;

    public WandAttr(dItem item, String key) {
        super(item, key);
    }

	@Override
	public boolean onRefactor(ItemStack itemStack)
	{
        MagicAPI api = MagicPlugin.getAPI();
        if (!api.isWand(itemStack))
		{
			return false;
		}
        Wand wand = new Wand(TradersController.getController(), itemStack);
        YamlConfiguration saveData = new YamlConfiguration();
        wand.saveProperties(saveData);
        wandData = saveData.saveToString();
		if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] onRefactor: " + wandData);
		return true;
	}

	@Override
	public boolean deserialize(String itemData)
	{
		if (itemData == null || itemData.isEmpty()) {
			return false;
		}
		this.wandData = itemData
                .replace("{sp}", " ")
                .replace("{co}", ":")
                .replace("{cr}", "\n");;
		if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] onLoad: " + itemData);
		return true;
	}

	@Override
	public String serialize()
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
	public void onAssign(ItemStack itemStack, boolean unused)
	{
        if (itemStack != null && wandData != null && !wandData.isEmpty())
        {
            Wand wand = Wand.createWand(TradersController.getController(), itemStack);
            YamlConfiguration saveData = new YamlConfiguration();
            try {
                saveData.loadFromString(wandData);
                if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] loading keys: " + StringUtils.join(saveData.getKeys(false), ","));
                wand.loadProperties(saveData);
                wand.save();
            } catch (InvalidConfigurationException ex) {
                TradersController.getController().getLogger().log(Level.WARNING, "Error deserializing wand data", ex);
            }
            if (TradersController.DEBUG) Bukkit.getLogger().info("[WAND] onReturnAssign for: " + wandData);
        }
	}

	/**
	 * Called when a week equality is needed. Allows sometimes a value to be in range of another value, used for priority requests
	 * @return
	 *    true when equal, false instead 
     */
    @Override
    public boolean similar(ItemAttribute other)
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
	@Override
    public boolean equals(ItemAttribute other)
	{
		if (other instanceof WandAttr)
		{
			return wandData.equals(((WandAttr) other).wandData);
        }
        return false;
	}
}
