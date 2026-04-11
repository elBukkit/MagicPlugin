package com.elmakers.mine.bukkit.integration.valhalla;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.economy.BaseMagicCurrency;

import me.athlaeos.valhallammo.event.PlayerSkillExperienceGainEvent;
import me.athlaeos.valhallammo.skills.skills.Skill;

public class ValhallaSkillCurrency extends BaseMagicCurrency {
    private final Skill skill;

    public ValhallaSkillCurrency(ValhallaManager valhallaManager, Skill skill, String skillId, ConfigurationSection configuration) {
        super(valhallaManager.getController(), skillId, configuration);
        this.skill = skill;
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        // ?
        return 0.0;
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        return false;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {

    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        skill.addEXP(mage.getPlayer(), amount, false, PlayerSkillExperienceGainEvent.ExperienceGainReason.PLUGIN);
        return true;
    }
}
