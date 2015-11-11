package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseShopAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemShopAction extends BaseShopAction
{
    private Map<String, Double> items = new HashMap<String, Double>();

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        items.clear();
        if (parameters.contains("items"))
        {
            ConfigurationSection itemSection = parameters.getConfigurationSection("items");
            Collection<String> itemKeys = itemSection.getKeys(false);
            for (String itemKey : itemKeys) {
                items.put(itemKey, itemSection.getDouble(itemKey));
            }
        }
    }

    @Override
    public SpellResult perform(CastContext context) {
        SpellResult contextResult = checkContext(context);
        if (!contextResult.isSuccess()) {
            return contextResult;
        }
        List<ShopItem> shopItems = new ArrayList<ShopItem>();
        MageController controller = context.getController();
        for (Map.Entry<String, Double> itemValue : items.entrySet()) {
            String itemKey = itemValue.getKey();
            double worth = items.get(itemKey);
            // This is kinda ugly.. :|
            // This is here to undo the scaling of whatever type of currency is selected
            // So, for an SP shop- SP is converted to virtual economy units
            // and then converted back into SP by BaseShopAction.
            if (costScale > 0) {
                worth /= costScale;
            }

            ItemStack item = controller.createItem(itemKey);
            if (item == null) continue;
            shopItems.add(new ShopItem(item, worth));
        }
        return showItems(context, shopItems);
	}
}
