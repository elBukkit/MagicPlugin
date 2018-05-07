package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import com.elmakers.mine.bukkit.api.action.CastContext;

public class ShopAction extends SelectorAction {

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        super.prepare(context, parameters);
        loadItems(context, parameters, "items");
        loadItems(context, parameters, "spells");
    }

    protected void loadItems(CastContext context, ConfigurationSection parameters, String key) {
        if (parameters.contains(key)) {
            List<ConfigurationSection> itemConfigs = new ArrayList<>();
            List<? extends Object> objects = parameters.getList(key);
            for (Object object : objects) {
                if (object instanceof ConfigurationSection) {
                    itemConfigs.add((ConfigurationSection)object);
                } else if (object instanceof String) {
                    ConfigurationSection itemConfig = new MemoryConfiguration();
                    itemConfig.set("item", object);
                    itemConfigs.add(itemConfig);
                } else {
                    context.getLogger().warning("Invalid item in shop config: " + object);
                }
            }

            loadOptions(itemConfigs);
        }
    }
}
