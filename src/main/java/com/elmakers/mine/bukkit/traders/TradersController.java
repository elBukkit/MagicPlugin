package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidClassException;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;
import net.dandielo.citizens.traders_v3.utils.items.ItemFlag;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.magic.MagicController;

/**
 * TODO: Remove all this, it's broken with the Metadata-based Wands.
 *
 * This should be replaced with a general "custom data" tag, IMO.
 * Preferably this would be an internal dtlTraders attribute.
 */
public class TradersController {
	
	private static MagicController controller;
	
	public void initialize(MagicController controller, Plugin tradersPlugin) throws AttributeInvalidClassException {

		ItemAttr.registerAttr(WandActiveBrushAttr.class);
		ItemAttr.registerAttr(WandActiveSpellAttr.class);
		ItemAttr.registerAttr(WandBrushListAttr.class);
		ItemAttr.registerAttr(WandCooldownReductionAttr.class);
		ItemAttr.registerAttr(WandCostReductionAttr.class);
		ItemAttr.registerAttr(WandDescriptionAttr.class);
		ItemAttr.registerAttr(WandEffectColorAttr.class);
		ItemAttr.registerAttr(WandEffectParticleAttr.class);
		ItemAttr.registerAttr(WandEffectParticleCountAttr.class);
		ItemAttr.registerAttr(WandEffectParticleDataAttr.class);
		ItemAttr.registerAttr(WandEffectParticleIntervalAttr.class);
		ItemAttr.registerAttr(WandEffectSoundAttr.class);
		ItemAttr.registerAttr(WandEffectSoundIntervalAttr.class);
		ItemAttr.registerAttr(WandHasteAttr.class);
		ItemAttr.registerAttr(WandHealthRegenerationAttr.class);
		ItemAttr.registerAttr(WandHungerRegenerationAttr.class);
		ItemAttr.registerAttr(WandIconAttr.class);
		ItemAttr.registerAttr(WandManaAttr.class);
		ItemAttr.registerAttr(WandManaMaxAttr.class);
		ItemAttr.registerAttr(WandManaRegenerationAttr.class);
		ItemAttr.registerAttr(WandModeAttr.class);
		ItemAttr.registerAttr(WandNameAttr.class);
        ItemAttr.registerAttr(WandOverridesAttr.class);
        ItemAttr.registerAttr(WandOwnerAttr.class);
        ItemAttr.registerAttr(WandOwnerIdAttr.class);
        ItemAttr.registerAttr(WandPathAttr.class);
		ItemAttr.registerAttr(WandPowerAttr.class);
		ItemAttr.registerAttr(WandProtectionAttr.class);
		ItemAttr.registerAttr(WandProtectionPhysicalAttr.class);
		ItemAttr.registerAttr(WandProtectionProjectilesAttr.class);
		ItemAttr.registerAttr(WandProtectionFallingAttr.class);
		ItemAttr.registerAttr(WandProtectionFireAttr.class);
		ItemAttr.registerAttr(WandQuietAttr.class);
		ItemAttr.registerAttr(WandSpellListAttr.class);
		ItemAttr.registerAttr(WandTemplateAttr.class);
		ItemAttr.registerAttr(WandUsesAttr.class);
        ItemAttr.registerAttr(WandUpgradeIconAttr.class);

		ItemAttr.registerAttr(BrushKeyAttr.class);
		ItemAttr.registerAttr(SpellKeyAttr.class);

		ItemFlag.registerFlag(WandBoundFlag.class);
		ItemFlag.registerFlag(WandEffectBubblesFlag.class);
		ItemFlag.registerFlag(WandFillFlag.class);
        ItemFlag.registerFlag(WandForceFlag.class);
        ItemFlag.registerFlag(WandIndestructibleFlag.class);
        ItemFlag.registerFlag(WandKeepFlag.class);
        ItemFlag.registerFlag(WandLockedFlag.class);
        ItemFlag.registerFlag(WandOrganizeFlag.class);
        ItemFlag.registerFlag(WandRandomizeFlag.class);
        ItemFlag.registerFlag(WandRenameFlag.class);
        ItemFlag.registerFlag(WandUpgradeFlag.class);
        ItemFlag.registerFlag(WandUndroppableFlag.class);

		ItemFlag.registerFlag(GlowFlag.class);

		TradersController.controller = controller;
	}
	
	public static MagicController getController() {
		return controller;
	}
}
