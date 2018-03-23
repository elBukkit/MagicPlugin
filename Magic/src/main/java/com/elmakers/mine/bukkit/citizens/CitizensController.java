package com.elmakers.mine.bukkit.citizens;

import org.bukkit.plugin.Plugin;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;

public class CitizensController {
	private Citizens citizensPlugin;

	public CitizensController(Plugin plugin) {
        citizensPlugin = (Citizens)plugin;

        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(MagicCitizensTrait.class).withName("magic"));
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(CommandCitizensTrait.class).withName("command"));
    }

    public Citizens getCitizensPlugin() {
        return citizensPlugin;
    }
}
