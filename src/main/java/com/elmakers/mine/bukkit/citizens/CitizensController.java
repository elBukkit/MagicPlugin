package com.elmakers.mine.bukkit.citizens;

import net.citizensnpcs.Citizens;
import org.bukkit.plugin.Plugin;

public class CitizensController {
	private Citizens citizensPlugin;

	public CitizensController(Plugin plugin) {
        citizensPlugin = (Citizens)plugin;

        net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(MagicCitizensTrait.class).withName("magic"));
        net.citizensnpcs.api.CitizensAPI.getTraitFactory().registerTrait(net.citizensnpcs.api.trait.TraitInfo.create(CommandCitizensTrait.class).withName("command"));
    }

    public Citizens getCitizensPlugin() {
        return citizensPlugin;
    }
}
