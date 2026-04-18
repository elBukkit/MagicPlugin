package com.elmakers.mine.bukkit.integration.valhalla;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;

public class SpellPerkReward extends PerkReward {
    private final MageController controller;
    private final String classKey;
    private List<SpellTemplate> spells = new ArrayList<>();

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
            controller.getPlugin().getServer().getScheduler().runTask(controller.getPlugin(), () -> {
                for (SpellTemplate spell : spells) {
                    mageClass.addSpell(spell.getKey());
                }
            });
        }
    }

    @Override
    public void remove(Player player) {
        Mage mage = controller.getMage(player);
        MageClass mageClass = mage.getClass(classKey);
        if (mageClass != null) {
            controller.getPlugin().getServer().getScheduler().runTask(controller.getPlugin(), () -> {
                for (SpellTemplate spell : spells) {
                    mageClass.removeSpell(spell.getKey());
                }
            });
        }
    }

    @Override
    public void parseArgument(Object argument) {
        List<String> keyList = ConfigurationUtils.getStringList(argument);
        for (String spellKey : keyList) {
            SpellTemplate spell = controller.getSpellTemplate(spellKey);
            if (spell != null) {
                spells.add(spell);
            }
        }
    }

    @Override
    public String rewardPlaceholder() {
        if (spells.size() == 1) {
            return "Learn " + spells.get(0).getName();
        }
        return "Learn " + spells.size() + " spells";
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.STRING_LIST;
    }

    @Override
    public SpellPerkReward clone() throws CloneNotSupportedException {
        SpellPerkReward reward = (SpellPerkReward)super.clone();
        reward.spells = new ArrayList<>(reward.spells);
        return reward;
    }
}
