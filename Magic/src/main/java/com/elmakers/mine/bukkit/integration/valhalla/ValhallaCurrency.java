package com.elmakers.mine.bukkit.integration.valhalla;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.economy.BaseMagicCurrency;

import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.skills.skills.Skill;

public class ValhallaCurrency extends BaseMagicCurrency {
    private final Skill skill;
    private final Class<? extends Profile> profileClass;

    public ValhallaCurrency(ValhallaManager valhallaManager, Skill skill, String skillId, ConfigurationSection configuration) {
        super(valhallaManager.getController(), skillId, configuration);
        this.skill = skill;
        this.profileClass = skill.getProfileType();
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        Player player = mage.getPlayer();
        if (player == null) {
            return 0.0;
        }
        Profile playerProfile = ProfileCache.getOrCache(player, profileClass);
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
        Player player = mage.getPlayer();
        if (player == null) {
            return;
        }
        Profile playerProfile = ProfileCache.getOrCache(player, profileClass);
        if (playerProfile == null) {
            return;
        }
        playerProfile.setTotalEXP(playerProfile.getTotalEXP() - amount);
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        skill.addEXP(mage.getPlayer(), amount, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.PLUGIN);
        return true;
    }
}
