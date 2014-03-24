package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidClassException;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;

public class TradersController {
	
	private static MagicController controller;
	
	public void initialize(MagicController controller, Plugin tradersPlugin) throws AttributeInvalidClassException {
		ItemAttr.registerAttr(WandItem.class);
		ItemAttr.registerAttr(BrushItem.class);
		ItemAttr.registerAttr(SpellItem.class);
		TradersController.controller = controller;
	}
	
	public static MagicController getController() {
		return controller;
	}
}
