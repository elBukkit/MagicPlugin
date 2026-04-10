package com.elmakers.mine.bukkit.integration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.economy.BaseMagicCurrency;

import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;

public class ValhallaProfileCurrency extends BaseMagicCurrency {
    private final Profile profile;

    public ValhallaProfileCurrency(ValhallaManager valhallaManager, Profile profile, String skillId, ConfigurationSection configuration) {
        super(valhallaManager.getController(), skillId, configuration);
        this.profile = profile;
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        Player player = mage.getPlayer();
        if (player == null) {
            return 0.0;
        }
        Profile playerProfile = ProfileCache.getOrCache(player, profile.getClass());
        if (playerProfile == null) {
            return 0.0;
        }
        return playerProfile.getTotalEXP();
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        return getBalance(mage, caster) > amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {

    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        return false;
    }
}
