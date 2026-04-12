package com.elmakers.mine.bukkit.integration.valhalla;

import org.bukkit.entity.Player;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageClass;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;

import me.athlaeos.valhallammo.skills.perk_rewards.PerkReward;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardArgumentType;

public class SpellPerkReward extends PerkReward {
    private final MageController controller;
    private final String classKey;
    private SpellTemplate spell;

    public SpellPerkReward(MageController controller, String classKey, String name) {
        super(name);
        this.controller = controller;
        this.classKey = classKey;
    }

    @Override
    public void apply(Player player) {
        Mage mage = controller.getMage(player);
        MageClass mageClass = mage.getClass(classKey);
        if (mageClass != null && spell != null) {
            controller.getPlugin().getServer().getScheduler().runTask(controller.getPlugin(), () -> {
                mageClass.addSpell(spell.getKey());
            });
        }
    }

    @Override
    public void remove(Player player) {
        Mage mage = controller.getMage(player);
        MageClass mageClass = mage.getClass(classKey);
        if (mageClass != null && spell != null) {
            mageClass.removeSpell(spell.getKey());
        }
    }

    @Override
    public void parseArgument(Object argument) {
        String spellKey = parseString(argument);
        spell = controller.getSpellTemplate(spellKey);
    }

    @Override
    public String rewardPlaceholder() {
        if (spell == null) {
            return "";
        }
        return "Learn " + spell.getName();
    }

    @Override
    public PerkRewardArgumentType getRequiredType() {
        return PerkRewardArgumentType.STRING;
    }
}
