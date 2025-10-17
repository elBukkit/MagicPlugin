package com.elmakers.mine.bukkit.integration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.CasterProperties;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.economy.BaseMagicCurrency;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;

public class AureliumSkillCurrency extends BaseMagicCurrency {
    private final Skills skill;

    public AureliumSkillCurrency(AureliumSkillsManager auraSkillsManager, Skills skill, ConfigurationSection configuration) {
        super(auraSkillsManager.getController(), skill.name(), configuration);
        this.skill = skill;
    }

    private SkillsUser getUser(Mage mage) {
        if (!mage.isPlayer()) {
            return null;
        }
        Player player = mage.getPlayer();
        return AuraSkillsApi.get().getUser(player.getUniqueId());
    }

    @Override
    public double getBalance(Mage mage, CasterProperties caster) {
        SkillsUser user = getUser(mage);
        if (user == null) {
            return 0;
        }
        return user.getSkillXp(skill);
    }

    @Override
    public boolean has(Mage mage, CasterProperties caster, double amount) {
        SkillsUser user = getUser(mage);
        if (user == null) {
            return false;
        }
        return user.getSkillXp(skill) >= amount;
    }

    @Override
    public void deduct(Mage mage, CasterProperties caster, double amount) {
        SkillsUser user = getUser(mage);
        if (user == null) {
            return;
        }
        double currentXp = user.getSkillXp(skill);
        double newXp = currentXp - amount;
        if (newXp < 0) {
            newXp = 0;
        }
        user.setSkillXp(skill, newXp);
    }

    @Override
    public boolean give(Mage mage, CasterProperties caster, double amount) {
        SkillsUser user = getUser(mage);
        if (user == null) {
            return false;
        }
        user.addSkillXp(skill, amount);
        return true;
    }
}
