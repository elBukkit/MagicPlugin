package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.Spell;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import de.slikey.effectlib.util.ConfigUtils;

@Deprecated
public class ItemShopAction extends com.elmakers.mine.bukkit.action.BaseShopAction
{
    private List<ShopItem> items = new ArrayList<>();

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
                    items.add(createShopItem(spell.getController(), itemKey, itemSection.getDouble(itemKey, -1)));
                }
            } else {
                List<?> itemList = parameters.getList("items");
                for (Object itemEntry : itemList)
                {
                    if (itemEntry instanceof String) {
                        String itemKey = (String)itemEntry;
                        items.add(createShopItem(spell.getController(), itemKey, -1));
                    } else if (itemEntry instanceof ConfigurationSection || itemEntry instanceof Map) {
                        ConfigurationSection itemConfig = (itemEntry instanceof ConfigurationSection)
                                ? (ConfigurationSection)itemEntry
                                : ConfigUtils.toConfigurationSection((Map<?,?>)itemEntry);
                        ShopItem shopItem = null;
                        if (itemConfig != null) {
                            double cost = itemConfig.getDouble("cost");
                            if (itemConfig.isString("item")) {
                                shopItem = createShopItem(spell.getController(), itemConfig);
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
                                            List<String> translatedLore = new ArrayList<>();
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

    @Nullable
    protected ShopItem createShopItem(MageController controller, String itemKey, double worth) {
        ItemStack item = parseItemKey(controller, itemKey);
        return item == null ? null : new ShopItem(controller, item, worth);
    }

    @Nullable
    protected ShopItem createShopItem(MageController controller, ConfigurationSection configuration) {
        ItemStack item = parseItemKey(controller, configuration.getString("item"));
        return item == null ? null : new ShopItem(controller, item, configuration);
    }

    @Nullable
    protected ItemStack parseItemKey(MageController controller, String itemKey) {
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

        if (showActiveIcons && controller.getAPI().isWand(item))
        {
            Wand wand = controller.getWand(item);
            wand.getIcon().applyToItem(item);
        }
        item.setAmount(amount);
        return item;
    }

    @Override
    protected List<ShopItem> getItems(CastContext context) {
        return items;
    }
}
