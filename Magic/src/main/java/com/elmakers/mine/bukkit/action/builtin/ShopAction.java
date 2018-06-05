package com.elmakers.mine.bukkit.action.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.magic.ProgressionPath;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.wand.Wand;

public class ShopAction extends SelectorAction {

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters) {
        boolean showPath = parameters.getBoolean("show_path_spells", false);
        boolean showExtra = parameters.getBoolean("show_extra_spells", false);
        boolean showRequired = parameters.getBoolean("show_required_spells", false);
        boolean showFree = parameters.getBoolean("show_free", false);

        // Don't load items as defaults
        Object itemDefaults = parameters.get("items");
        parameters.set("items", null);

        // Sell shop overrides
        if (parameters.getBoolean("sell")) {
            // Support scale parameter
            parameters.set("earn_scale", parameters.get("scale"));
            parameters.set("scale", null);

            // Supply selected message
            if (!parameters.contains("selected")) {
                parameters.set("selected", context.getController().getMessages().get("shops.sold"));
            }
        }

        if (!parameters.contains("cost_type") && (showPath || showExtra || showRequired || parameters.contains("spells"))) {
            parameters.set("cost_type", "sp");
        }

        super.prepare(context, parameters);

        // Restore items list. This is kind of messy, but so is this whole action.
        parameters.set("items", itemDefaults);
        loadItems(context, parameters, "items", false);
        loadItems(context, parameters, "spells", true);

        // Auto-populate spells
        Mage mage = context.getMage();
        CasterProperties caster = mage.getActiveProperties();
        ProgressionPath currentPath = caster.getPath();
        if (currentPath != null) {
            List<String> spellKeys = new ArrayList<>();
            if (showPath) {
                spellKeys.addAll(currentPath.getSpells());
            }
            if (showRequired) {
                spellKeys.addAll(currentPath.getRequiredSpells());
            }
            loadSpells(context, spellKeys, showFree, false);

            if (showExtra) {
                loadSpells(context, currentPath.getExtraSpells(), showFree, true);
            }
        }
    }

    protected void loadSpells(CastContext context, Collection<String> spellKeys, boolean showFree, boolean isExtra) {
        Mage mage = context.getMage();
        CasterProperties caster = mage.getActiveProperties();
        MageController controller = context.getController();
        List<SpellTemplate> spells = new ArrayList<>();
        for (String spellKey : spellKeys) {
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (spell == null) {
                mage.sendDebugMessage(ChatColor.GRAY + " Skipping " + spellKey + ", is invalid", 3);
                continue;
            }
            if (caster.hasSpell(spellKey) && (spell.getWorth() > 0 || showFree)) {
                mage.sendDebugMessage(ChatColor.GRAY + " Skipping " + spellKey + ", already have it", 3);
                continue;
            }
            if (spell.getWorth() <= 0 && !showFree) {
                mage.sendDebugMessage(ChatColor.GRAY + " Skipping " + spellKey + ", is free", 3);
                continue;
            }
            if (!spell.hasCastPermission(mage.getCommandSender())) {
                mage.sendDebugMessage(ChatColor.YELLOW + " Skipping " + spellKey + ", no permission", 3);
                continue;
            }

            spells.add(spell);
        }

        mage.sendDebugMessage(ChatColor.GOLD + "Spells to buy: " + spells.size(), 2);

        if (spells.size() == 0) {
            return;
        }

        Collections.sort(spells, new Comparator<SpellTemplate>() {
            @Override
            public int compare(SpellTemplate spell1, SpellTemplate spell2) {
                return Double.compare(spell1.getWorth(), spell2.getWorth());
            }
        });

        // Add padding
        List<ConfigurationSection> pathSpellConfigs = new ArrayList<>();
        if (isExtra) {
            int maxSlot = getNumSlots();
            int paddedSlot = 8 - (maxSlot + 8) % 9;
            for (int i = 0; i < paddedSlot; i++) {
                ConfigurationSection emptyConfig = new MemoryConfiguration();
                emptyConfig.set("item", "none");
                pathSpellConfigs.add(emptyConfig);
            }
        }

        String addLore = isExtra ? getMessage("extra_spell") : null;
        for (SpellTemplate spell : spells) {
            ConfigurationSection spellConfig = new MemoryConfiguration();
            spellConfig.set("item", "spell:" + spell.getKey());
            if (addLore != null) {
                spellConfig.set("description", addLore);
            }
            pathSpellConfigs.add(spellConfig);
        }
        loadOptions(pathSpellConfigs);
    }

    protected void loadItems(CastContext context, ConfigurationSection parameters, String key, boolean filterSpells) {
        if (parameters.contains(key)) {
            List<ConfigurationSection> itemConfigs = new ArrayList<>();
            if (parameters.isConfigurationSection(key)) {
                ConfigurationSection itemSection = parameters.getConfigurationSection(key);
                Set<String> itemKeys = itemSection.getKeys(false);
                for (String itemKey : itemKeys) {
                    ConfigurationSection itemConfig = new MemoryConfiguration();
                    itemConfig.set("item", itemKey);
                    itemConfig.set("cost", itemSection.get(itemKey));
                    itemConfigs.add(itemConfig);
                }
            } else {
                List<?> objects = parameters.getList(key);
                for (Object object : objects) {
                    if (object instanceof ConfigurationSection) {
                        itemConfigs.add((ConfigurationSection)object);
                    } else if (object instanceof Map) {
                         itemConfigs.add(ConfigurationUtils.toConfigurationSection((Map<?, ?>)object));
                    } else if (object instanceof String) {
                        ConfigurationSection itemConfig = new MemoryConfiguration();
                        itemConfig.set("item", object);
                        itemConfigs.add(itemConfig);
                    } else {
                        context.getLogger().warning("Invalid item in shop config: " + object);
                    }
                }
            }

            if (filterSpells) {
                CasterProperties caster = context.getMage().getActiveProperties();
                Iterator<ConfigurationSection> it = itemConfigs.iterator();
                while (it.hasNext()) {
                    ConfigurationSection config = it.next();
                    String spellName = config.getString("item");
                    if (spellName != null && caster.hasSpell(spellName)) {
                        it.remove();
                    } else {
                        config.set("item", "spell:" + spellName);
                    }
                }
            }

            if (parameters.getBoolean("sell", false)) {
                for (ConfigurationSection itemConfig : itemConfigs) {
                    String itemName = itemConfig.getString("item");
                    if (itemName == null || itemName.equalsIgnoreCase("none")) continue;

                    itemConfig.set("item", null);
                    itemConfig.set("icon", itemName);
                    ItemStack item = parseItem(itemName);
                    if (item == null) continue;

                    Object costs = itemConfig.get("cost");
                    if (costs != null) {
                        itemConfig.set("earn", costs);
                        itemConfig.set("cost", null);
                    } else {
                        Double worth = context.getController().getWorth(item);
                        if (worth != null && worth > 0) {
                            itemConfig.set("earn", worth);
                        }
                    }
                    ConfigurationSection costSection = itemConfig.createSection("costs");
                    Integer sp = Wand.getSP(item);
                    if (sp != null) {
                        costSection.set("sp", sp);
                    } else {
                        costSection.set(itemName, item.getAmount());
                    }
                }
            }
            loadOptions(itemConfigs);
        }
    }
}
