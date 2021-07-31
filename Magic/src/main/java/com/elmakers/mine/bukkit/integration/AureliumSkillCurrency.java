package com.elmakers.mine.bukkit.integration;

import org.bukkit.configuration.ConfigurationSection;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.skills.Skills;
import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.economy.BaseMagicCurrency;

public class AureliumSkillCurrency extends BaseMagicCurrency {
    private final Skills skill;
    private final AureliumSkillsManager aureliumSkills;

    public AureliumSkillCurrency(AureliumSkillsManager aureliumSkills, Skills skill, ConfigurationSection configuration) {
        super(aureliumSkills.getController(), skill.name(), configuration);
        this.skill = skill;
        this.aureliumSkills = aureliumSkills;
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        if (!mage.isPlayer()) {
            return 0;
        }
        return AureliumAPI.getXp(mage.getPlayer(), skill);
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        if (!mage.isPlayer()) {
            return false;
        }
        return AureliumAPI.getXp(mage.getPlayer(), skill) >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        // Nope
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        if (!mage.isPlayer()) {
            return false;
        }
        AureliumAPI.addXp(mage.getPlayer(), skill, amount);
        return true;
    }
}
