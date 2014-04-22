package com.elmakers.mine.bukkit.traders;

import net.dandielo.citizens.traders_v3.core.exceptions.attributes.AttributeInvalidClassException;
import net.dandielo.citizens.traders_v3.utils.items.ItemAttr;
import net.dandielo.citizens.traders_v3.utils.items.ItemFlag;

import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.plugins.magic.MagicController;

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
		ItemAttr.registerAttr(WandEffectSoundPitchAttr.class);
		ItemAttr.registerAttr(WandEffectSoundVolumeAttr.class);
		ItemAttr.registerAttr(WandHasteAttr.class);
		ItemAttr.registerAttr(WandHealthRegenerationAttr.class);
		ItemAttr.registerAttr(WandHungerRegenerationAttr.class);
		ItemAttr.registerAttr(WandIconAttr.class);
		ItemAttr.registerAttr(WandManaAttr.class);
		ItemAttr.registerAttr(WandManaMaxAttr.class);
		ItemAttr.registerAttr(WandManaRegenerationAttr.class);
		ItemAttr.registerAttr(WandModeAttr.class);
		ItemAttr.registerAttr(WandNameAttr.class);
		ItemAttr.registerAttr(WandOwnerAttr.class);
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

		ItemAttr.registerAttr(BrushKeyAttr.class);
		ItemAttr.registerAttr(SpellKeyAttr.class);

		ItemFlag.registerFlag(WandBoundFlag.class);
		ItemFlag.registerFlag(WandKeepFlag.class);
		ItemFlag.registerFlag(WandEffectBubblesFlag.class);
		ItemFlag.registerFlag(WandOrganizeFlag.class);
		ItemFlag.registerFlag(WandFillFlag.class);
		ItemFlag.registerFlag(WandLockedFlag.class);
		
		ItemFlag.registerFlag(WandFlag.class);
		ItemFlag.registerFlag(GlowFlag.class);
		ItemFlag.registerFlag(WandUpgradeFlag.class);

		TradersController.controller = controller;
	}
	
	public static MagicController getController() {
		return controller;
	}
}
