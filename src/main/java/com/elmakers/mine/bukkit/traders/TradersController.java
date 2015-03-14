package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidClassException;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;

public class TradersController {
    protected static final boolean DEBUG = false;
    private static MagicController controller;
	
	public void initialize(MagicController controller, Plugin tradersPlugin) throws AttributeInvalidClassException {

        ItemAttr.registerAttr(BrushAttr.class);
        ItemAttr.registerAttr(SpellAttr.class);
        ItemAttr.registerAttr(WandAttr.class);

		TradersController.controller = controller;
	}
	
	public static MagicController getController() {
		return controller;
	}
}
