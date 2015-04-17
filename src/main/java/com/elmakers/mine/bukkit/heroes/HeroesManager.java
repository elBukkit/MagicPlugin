package com.elmakers.mine.bukkit.heroes;

import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class HeroesManager {
    private Heroes heroes;
    private CharacterManager characters;
    private SkillManager skills;
    private final static Set<String> emptySkills = new HashSet<String>();

    public HeroesManager(Plugin plugin, Plugin heroesPlugin) {
        if (!(heroesPlugin instanceof Heroes))
        {
            plugin.getLogger().warning("Heroes found, but is not instance of Heroes plugin!");
            return;
        }
        heroes = (Heroes)heroesPlugin;
        characters = heroes.getCharacterManager();
        skills = heroes.getSkillManager();
        if (heroes != null && characters != null && skills != null)
        {
            plugin.getLogger().warning("Heroes found, skills available for wand use");
        }
        else
        {
            plugin.getLogger().warning("Heroes found, but failed to integrate!");
        }
    }

    public Set<String> getSkills(Player player) {
        if (skills == null) return emptySkills;
        Hero hero = getHero(player);
        if (hero == null) return emptySkills;
        HeroClass heroClass = hero.getHeroClass();
        if (heroClass == null) return emptySkills;
        Set<String> skillSet = new HashSet<String>();
        if (heroClass != null)
        {
            Set<String> classSkills = heroClass.getSkillNames();
            for (String classSkill : classSkills)
            {
                Skill skill = skills.getSkill(classSkill);
                // getRaw's boolean default value is ignored! :(
                if (hero.canUseSkill(skill) && SkillConfigManager.getRaw(skill, "wand", "true").equalsIgnoreCase("true"))
                {;
                    skillSet.add(classSkill);
                }
            }
        }
        HeroClass secondClass = hero.getSecondClass();
        if (secondClass != null)
        {
            Set<String> classSkills = secondClass.getSkillNames();
            for (String classSkill : classSkills)
            {
                Skill skill = skills.getSkill(classSkill);
                if (hero.canUseSkill(skill) && SkillConfigManager.getRaw(skill, "wand", "true").equalsIgnoreCase("true"))
                {
                    skillSet.add(classSkill);
                }
            }
        }
        return skillSet;
    }

    public SpellTemplate createSkillSpell(MagicController controller, String skillName) {
        if (skills == null) return null;
        Skill skill = skills.getSkill(skillName);
        if (skill == null) return null;

        MageSpell newSpell = new HeroesSkillSpell();
        newSpell.initialize(controller);
        ConfigurationSection config = new MemoryConfiguration();
        config.set("icon", SkillConfigManager.getRaw(skill, "icon", controller.getDefaultSkillIcon()));
        config.set("icon_url", SkillConfigManager.getRaw(skill, "icon-url", null));
        config.set("name", skill.getName());
        config.set("description", SkillConfigManager.getRaw(skill, "description", null));
        newSpell.loadTemplate("heroes*" + skillName, config);
        return newSpell;
    }

    public Skill getSkill(String key) {
        if (skills == null) return null;
        return skills.getSkill(key);
    }

    public Hero getHero(Player player) {
        if (characters == null) return null;
        return characters.getHero(player);
    }

    public int getMaxMana(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return 0;
        return hero.getMaxMana();
    }

    public int getManaRegen(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return 0;
        return hero.getManaRegen();
    }

    public int getMana(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return 0;
        return hero.getMana();
    }
}
