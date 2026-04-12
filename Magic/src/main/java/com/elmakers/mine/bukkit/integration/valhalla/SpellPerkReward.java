package com.elmakers.mine.bukkit.integration.valhalla;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;

import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;

public class SpellPerkReward extends PerkReward {
    private final MageController controller;
    private final String classKey;
    private String spellKey;

    public SpellPerkReward(MageController controller, String classKey, String name) {
        super(name);
        this.controller = controller;
        this.classKey = classKey;
    }

    @Override
    public void apply(Player player) {
        Mage mage = controller.getMage(player);
        MageClass mageClass = mage.getClass(classKey);
        if (mageClass != null) {
            mageClass.addSpell(spellKey);
        }
    }

    @Override
    public void remove(Player player) {
        Mage mage = controller.getMage(player);
        MageClass mageClass = mage.getClass(classKey);
        if (mageClass != null) {
            mageClass.removeSpell(spellKey);
        }
    }

    @Override
    public void parseArgument(Object argument) {
        spellKey = parseString(argument);
    }

    @Override
    public String rewardPlaceholder() {
        return "";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return null;
    }
}
