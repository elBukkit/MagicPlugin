package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseShopAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import de.slikey.effectlib.util.ConfigUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ItemShopAction extends BaseShopAction
{
    private List<ShopItem> items = new ArrayList<ShopItem>();

    @Override
    public void initialize(Spell spell, ConfigurationSection parameters)
    {
        super.initialize(spell, parameters);
        items.clear();
        if (parameters.contains("items"))
        {
            if (parameters.isConfigurationSection("items")) {
                ConfigurationSection itemSection = ConfigurationUtils.getConfigurationSection(parameters, "items");
                Collection<String> itemKeys = itemSection.getKeys(false);
                for (String itemKey : itemKeys) {
                    items.add(parseItemKey(spell.getController(), itemKey, itemSection.getDouble(itemKey, -1)));
                }
            } else {
                List<?> itemList = parameters.getList("items");
                for (Object itemEntry : itemList)
                {
                    if (itemEntry instanceof String) {
                        String itemKey = (String)itemEntry;
                        items.add(parseItemKey(spell.getController(), itemKey, -1));
                    } else if (itemEntry instanceof ConfigurationSection || itemEntry instanceof Map) {
                        ConfigurationSection itemConfig = (itemEntry instanceof ConfigurationSection) ?
                                (ConfigurationSection)itemEntry : ConfigUtils.toConfigurationSection((Map)itemEntry); 
                        ShopItem shopItem = null;
                        if (itemConfig != null) {
                            double cost = itemConfig.getDouble("cost");
                            if (itemConfig.isString("item")) {
                                shopItem = parseItemKey(spell.getController(), itemConfig.getString("item"), cost);
                                if (shopItem != null) {
                                    String name = itemConfig.getString("name");
                                    List<String> lore = ConfigurationUtils.getStringList(itemConfig, "lore");

                                    if (name != null || lore != null) {
                                        ItemStack item = shopItem.getItem();
                                        ItemMeta meta = item.getItemMeta();
                                        if (name != null) {
                                            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                                        }
                                        if (lore != null) {
                                            List<String> translatedLore = new ArrayList<String>();
                                            for (String line : lore) {
                                                if (line != null) {
                                                    translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                                                }
                                            }
                                            meta.setLore(translatedLore);
                                        }
                                        item.setItemMeta(meta);
                                    }
                                }
                            } else {
                                ItemStack itemStack = itemConfig.getItemStack("item");
                                if (itemStack != null) {
                                    shopItem = new ShopItem(itemStack, cost);
                                }
                            }
                        }
                        items.add(shopItem);
                    }
                }
            }
        }
    }

    protected ShopItem parseItemKey(MageController controller, String itemKey, double worth) {
        if (itemKey == null || itemKey.isEmpty() || itemKey.equalsIgnoreCase("none"))
        {
            return null;
        }

        String[] pieces = StringUtils.split(itemKey, '@');
        int amount = 1;
        if (pieces.length > 1) {
            itemKey = pieces[0];
            try {
                amount = Integer.parseInt(pieces[1]);
            } catch (Exception ex) {
                context.getLogger().warning("Invalid item amount in shop: " + pieces[1] + " for item " + itemKey + " shop " + context.getSpell().getKey());
            }
        }
        ItemStack item = controller.createItem(itemKey, castsSpells);
        if (item == null) return null;
        item.setAmount(amount);
        if (worth < 0) {
            Double defaultWorth = controller.getWorth(item);
            
            org.bukkit.Bukkit.getLogger().info("Checking worth: " + defaultWorth + " of " + item);
            
            worth = defaultWorth == null ? 0 : defaultWorth;
        }
        
        return new ShopItem(item, worth);
    }
    
    @Override
    public SpellResult perform(CastContext context) {
        SpellResult contextResult = checkContext(context);
        if (!contextResult.isSuccess()) {
            return contextResult;
        }
        return showItems(context, items);
	}
}
