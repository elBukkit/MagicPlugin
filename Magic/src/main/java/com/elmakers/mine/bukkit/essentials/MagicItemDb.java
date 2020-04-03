package com.elmakers.mine.bukkit.essentials;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.api.IItemDb;
import com.earth2me.essentials.items.AbstractItemDb;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.wand.Wand;

import net.ess3.api.IEssentials;

public class MagicItemDb implements net.ess3.api.IItemDb.ItemResolver {

    private final MagicController controller;
    private final List<String> names = new ArrayList<>();

    public static boolean register(final MagicController controller, final Plugin essentialsPlugin) throws Exception {
        IEssentials essentials = (IEssentials)essentialsPlugin;
        IItemDb itemDb = essentials.getItemDb();
        if (itemDb instanceof AbstractItemDb) {
            ((AbstractItemDb)itemDb).registerResolver(controller.getPlugin(), "Magic", new MagicItemDb(controller));
        } else {
            return false;
        }
        return true;
    }

    private MagicItemDb(final MagicController controller) {
        this.controller = controller;
        Collection<SpellTemplate> spellList = controller.getSpellTemplates(false);
        for (SpellTemplate spell : spellList) {
            names.add("spell:" + spell.getKey());
        }
        Collection<String> allWands = controller.getWandTemplateKeys();
        for (String wandKey : allWands) {
            names.add("wand:" + wandKey);
        }
        Collection<String> allItems = controller.getItemKeys();
        for (String itemKey : allItems) {
            names.add("magic:" + itemKey);
        }
        Collection<String> currencies = controller.getCurrencyKeys();
        for (String currency : currencies) {
            names.add("magic:" + currency);
        }
    }

    @Override
    public Collection<String> getNames() {
        return names;
    }

    @Nullable
    @Override
    public ItemStack apply(final String id) {
        if (id.startsWith("m:")) {
            String itemId = id.replace("m:", "");
            return controller.createItem(itemId);
        } if (id.startsWith("magic:")) {
            String itemId = id.replace("magic:", "");
            return controller.createItem(itemId);
        } else if (id.equals("wand")) {
            Wand wand = Wand.createWand(controller, "");
            if (wand != null) {
                return wand.getItem();
            }
        } else if (id.startsWith("wand:")) {
            String wandId = id.replace("wand:", "");
            Wand wand = Wand.createWand(controller, wandId.trim());
            if (wand != null) {
                return wand.getItem();
            }

        } else if (id.startsWith("w:")) {
            String wandId = id.replace("w:", "");
            Wand wand = Wand.createWand(controller, wandId.trim());
            if (wand != null) {
                return wand.getItem();
            }

        } else if (id.startsWith("book:")) {
            String bookCategory = id.replace("book:", "");
            SpellCategory category = null;
            if (bookCategory.length() > 0 && !bookCategory.equalsIgnoreCase("all")) {
                category = controller.getCategory(bookCategory);
            }
            ItemStack bookItem = controller.getSpellBook(category, 1);
            if (bookItem != null) {
                return bookItem;
            }
        } else if (id.startsWith("spell:")) {
            String spellKey = id.replace("spell:", "");
            ItemStack itemStack = Wand.createSpellItem(spellKey, controller, null, true);
            if (itemStack != null) {
                return itemStack;
            }
        } else if (id.startsWith("s:")) {
            String spellKey = id.replace("s:", "");
            ItemStack itemStack = Wand.createSpellItem(spellKey, controller, null, true);
            if (itemStack != null) {
                return itemStack;
            }
        } else if (id.startsWith("brush:")) {
            String brushKey = id.replace("brush:", "");
            ItemStack itemStack = Wand.createBrushItem(brushKey, controller, null, true);
            if (itemStack != null) {
                return itemStack;
            }
        } else if (id.startsWith("upgrade:")) {
            String wandId = id.replace("upgrade:", "");
            Wand wand = Wand.createWand(controller, wandId.trim());
            if (wand != null) {
                wand.makeUpgrade();
                return wand.getItem();
            }
        } else if (id.startsWith("item:")) {
            String wandId = id.replace("item:", "");
            return controller.createItem(wandId);
        }

        return null;
    }
}
