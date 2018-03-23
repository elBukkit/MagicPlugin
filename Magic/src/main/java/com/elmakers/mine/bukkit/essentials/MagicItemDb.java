package com.elmakers.mine.bukkit.essentials;

import org.bukkit.inventory.ItemStack;

import com.earth2me.essentials.ItemDb;
import com.elmakers.mine.bukkit.api.spell.SpellCategory;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.wand.Wand;

import net.ess3.api.IEssentials;

public class MagicItemDb extends ItemDb {

	private final MagicController controller;

	public MagicItemDb(final MagicController controller, final Object ess) {
		super((IEssentials)ess);
		this.controller = controller;
		this.reloadConfig();
	}

	@Override
	public ItemStack get(final String id) throws Exception
	{
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

		return super.get(id);
	}
}
