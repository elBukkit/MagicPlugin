package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.Messages;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.elmakers.mine.bukkit.spell.CastingCost;
import com.elmakers.mine.bukkit.wand.Wand;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.spell.BaseSpell;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class HeroesSkillSpell extends BaseSpell {
    private String skillKey;
    private Skill skill;
    private HeroesManager heroes;
    private MagicController magic;
    private CastingCost manaCost = new CastingCost("mana", 1);

    @Override
    public void loadTemplate(String key, ConfigurationSection template) {
        super.loadTemplate(key, template);
        skillKey = key.substring(7);
        showUndoable = false;

        if (!(controller instanceof MagicController)) return;
        magic = (MagicController)controller;
        heroes = magic.getHeroes();
        if (heroes == null) return;
        skill = heroes.getSkill(skillKey);
    }

    @Override
    public SpellResult onCast(ConfigurationSection parameters) {
        CommandSender sender = mage.getCommandSender();
        controller.getPlugin().getServer().dispatchCommand(sender, "skill " + skillKey);
        return SpellResult.CAST;
    }

    @Override
    public void addSpellLore(Messages messages, Mage mage, com.elmakers.mine.bukkit.api.wand.Wand apiWand, List<String> lore) {
        if (mage == null || !mage.isPlayer()) return;
        Hero hero = heroes.getHero(mage.getPlayer());
        if (hero == null) return;

        int level = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.LEVEL, 1, true);

        String levelDescription = controller.getMessages().get("spell.level_description").replace("$level", Integer.toString(level));
        if (levelDescription != null && levelDescription.length() > 0) {
            lore.add(ChatColor.GOLD + levelDescription);
        }
        String description = getDescription();
        if (description == null || description.isEmpty())
        {
            description = skill.getDescription(hero);
        }

        if (description != null && description.length() > 0) {
            lore.add(description);
        }

        int cooldown = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 0, true);
        if (cooldown > 0)
        {
            String cooldownDescription = getCooldownDescription(messages, cooldown);
            if (cooldownDescription != null && !cooldownDescription.isEmpty()) {
                lore.add(messages.get("cooldown.description").replace("$time", cooldownDescription));
            }
        }

        int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 0, true);
        if (mana > 0)
        {
            String manaDescription = messages.get("costs.heroes_mana").replace("$amount", Integer.toString(mana));
            lore.add(ChatColor.YELLOW + messages.get("wand.costs_description").replace("$description", manaDescription));
        }
    }

    @Override
    public long getRemainingCooldown() {
        if (skill == null || mage == null) return 0;
        Player player = mage.getPlayer();
        if (player == null) return 0;
        Hero hero = heroes.getHero(mage.getPlayer());
        if (hero == null) return 0;
        Long cooldown = hero.getCooldown(skillKey);
        if (cooldown == null) return 0;
        long now = System.currentTimeMillis();
        return Math.max(0, cooldown - now);
    }

    @Override
    public CastingCost getRequiredCost() {
        if (skill == null || mage == null) return null;
        Player player = mage.getPlayer();
        if (player == null) return null;
        Hero hero = heroes.getHero(mage.getPlayer());
        if (hero == null) return null;
        int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 0, true);
        if (mana == 0 || hero.getMana() > mana) return null;
        return manaCost;
    }
}
