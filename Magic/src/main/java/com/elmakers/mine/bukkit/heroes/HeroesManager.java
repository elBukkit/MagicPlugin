package com.elmakers.mine.bukkit.heroes;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.spell.MageSpell;
import com.elmakers.mine.bukkit.api.spell.SpellTemplate;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.magic.ManaController;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import com.herocraftonline.heroes.characters.party.HeroParty;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;

public class HeroesManager implements ManaController, AttributeProvider, TeamProvider {
    private Heroes heroes;
    private CharacterManager characters;
    private SkillManager skills;
    private static final Set<String> emptySkills = new HashSet<>();
    private static final List<String> emptySkillList = new ArrayList<>();
    private final Logger log;

    private Method getHeroAttributeMethod;
    private static final Map<String, Enum<?>> attributes = new HashMap<>();

    public HeroesManager(Plugin plugin, Plugin heroesPlugin) {
        log = plugin.getLogger();
        if (!(heroesPlugin instanceof Heroes))
        {
            log.warning("Heroes found, but is not instance of Heroes plugin!");
            return;
        }
        heroes = (Heroes)heroesPlugin;

        try {
            @SuppressWarnings("unchecked")
            Class<Enum<?>> classAttributeType = (Class<Enum<?>>)Class.forName("com.herocraftonline.heroes.attributes.AttributeType");
            Enum<?>[] values = classAttributeType.getEnumConstants();
            for (Enum<?> value : values) {
                attributes.put(value.name().toLowerCase(), value);
            }
            getHeroAttributeMethod = Hero.class.getMethod("getAttributeValue", classAttributeType);
            log.info("Registered Heroes attributes for use in spell parameters: " + getAllAttributes());
        } catch (Exception ex) {
            attributes.clear();
            getHeroAttributeMethod = null;
            log.info("Could not register Heroes attributes, you may need to update Heroes");
        }

        // Delay integration because Heroes doesn't create its subsystems right away
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                finishIntegration();
            }
        }, 2);
    }

    public void finishIntegration() {
        characters = heroes.getCharacterManager();
        skills = heroes.getSkillManager();
        if (characters != null && skills != null)
        {
            log.info("Heroes found, skills available for wand and hotbar use.");
            log.info("Give Magic.commands.mskills permission for /mskills command");
            log.info("Use \"/wand heroes\" for a wand that uses Heroes skills");
        }
        else
        {
            log.warning("Heroes found, but failed to integrate!");
            if (characters == null) {
                log.warning(" CharacterManager is null");
            }
            if (skills == null) {
                log.warning(" SkillManager is null");
            }
        }
    }

    public boolean canUseSkill(Player player, String skillName) {
        Hero hero = getHero(player);
        if (hero == null) return false;
        return hero.canUseSkill(skillName);
    }

    public List<String> getSkillList(Player player, boolean showUnuseable, boolean showPassive)
    {
        if (skills == null) return emptySkillList;
        Hero hero = getHero(player);
        if (hero == null) return emptySkillList;

        HeroClass heroClass = hero.getHeroClass();
        HeroClass secondClass = hero.getSecondClass();
        Set<String> primarySkills = new HashSet<>();
        Set<String> secondarySkills = new HashSet<>();
        addSkills(hero, heroClass, primarySkills, showUnuseable, showPassive);
        addSkills(hero, secondClass, secondarySkills, showUnuseable, showPassive);
        secondarySkills.removeAll(primarySkills);

        Multimap<Integer, Skill> primaryMap = mapSkillsByLevel(hero, primarySkills);
        Multimap<Integer, Skill> secondaryMap = mapSkillsByLevel(hero, secondarySkills);
        List<String> skillNames = new ArrayList<>();
        for (Skill skill : primaryMap.values())
        {
            skillNames.add(skill.getName());
        }
        for (Skill skill : secondaryMap.values())
        {
            skillNames.add(skill.getName());
        }
        return skillNames;
    }

    private Multimap<Integer, Skill> mapSkillsByLevel(Hero hero, Collection<String> skillNames) {

        Multimap<Integer, Skill> skillMap = TreeMultimap.create(Ordering.natural(), new Comparator<Skill>() {
            @Override
            public int compare(Skill skill1, Skill skill2) {
                return skill1.getName().compareTo(skill2.getName());
            }
        });
        for (String skillName : skillNames)
        {
            Skill skill = skills.getSkill(skillName);
            if (skill == null) continue;
            int level = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.LEVEL, 1, true);
            skillMap.put(level, skill);
        }
        return skillMap;
    }

    public int getSkillLevel(Player player, String skillName) {
        Skill skill = skills.getSkill(skillName);
        if (skill == null) return 0;
        Hero hero = getHero(player);
        if (hero == null) return 0;
        return SkillConfigManager.getUseSetting(hero, skill, SkillSetting.LEVEL, 1, true);
    }

    private void addSkills(Hero hero, HeroClass heroClass, Collection<String> skillSet, boolean showUnuseable, boolean showPassive)
    {
        if (heroClass != null)
        {
            Set<String> classSkills = heroClass.getSkillNames();
            for (String classSkill : classSkills)
            {
                Skill skill = skills.getSkill(classSkill);
                if (!showUnuseable && !hero.canUseSkill(skill)) continue;
                if (!showPassive && !(skill instanceof ActiveSkill)) continue;
                // getRaw's boolean default value is ignored! :(
                if (SkillConfigManager.getRaw(skill, "wand", "true").equalsIgnoreCase("true"))
                {
                    skillSet.add(classSkill);
                }
            }
        }
    }

    public Set<String> getSkills(Player player) {
        return getSkills(player, false, false);
    }

    public Set<String> getSkills(Player player, boolean showUnuseable, boolean showPassive) {
        if (skills == null) return emptySkills;
        Hero hero = getHero(player);
        if (hero == null) return emptySkills;
        Set<String> skillSet = new HashSet<>();

        HeroClass heroClass = hero.getHeroClass();
        HeroClass secondClass = hero.getSecondClass();
        addSkills(hero, heroClass, skillSet, showUnuseable, showPassive);
        addSkills(hero, secondClass, skillSet, showUnuseable, showPassive);
        return skillSet;
    }

    public void clearCooldown(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return;

        hero.clearCooldowns();
    }

    public void setCooldown(Player player, long ms) {
        Hero hero = getHero(player);
        if (hero == null) return;

        long cooldown = System.currentTimeMillis() + ms;
        Set<String> skills = getSkills(player, false, false);
        for (String skill : skills) {
            Long currentCooldown = hero.getCooldown(skill);
            if (currentCooldown == null || currentCooldown < cooldown) {
                hero.setCooldown(skill, cooldown);
            }
        }
    }

    public void reduceCooldown(Player player, long ms) {
        Hero hero = getHero(player);
        if (hero == null) return;

        Set<String> skills = getSkills(player, false, false);
        for (String skill : skills) {
            Long currentCooldown = hero.getCooldown(skill);
            if (currentCooldown != null) {
                hero.setCooldown(skill, Math.max(0, currentCooldown - ms));
            }
        }
    }

    @Nullable
    public SpellTemplate createSkillSpell(MagicController controller, String skillName) {
        if (skills == null) return null;
        Skill skill = skills.getSkill(skillName);
        if (skill == null) return null;

        MageSpell newSpell = new HeroesSkillSpell();
        newSpell.initialize(controller);
        ConfigurationSection config = new MemoryConfiguration();
        String iconURL = SkillConfigManager.getRaw(skill, "icon-url", SkillConfigManager.getRaw(skill, "icon_url", null));
        if (iconURL == null || iconURL.isEmpty()) {
            String icon = SkillConfigManager.getRaw(skill, "icon", null);
            if (icon == null || icon.isEmpty()) {
                config.set("icon", controller.getDefaultSkillIcon());
            } else if (icon.startsWith("http://")) {
                config.set("icon_url", icon);
            } else {
                config.set("icon", icon);
            }
        } else {
            config.set("icon_url", iconURL);
        }

        String iconDisabledURL = SkillConfigManager.getRaw(skill, "icon-disabled-url", SkillConfigManager.getRaw(skill, "icon_disabled_url", null));
        if (iconDisabledURL == null || iconDisabledURL.isEmpty()) {
            String icon = SkillConfigManager.getRaw(skill, "icon-disabled", SkillConfigManager.getRaw(skill, "icon_disabled", null));
            if (icon != null && !icon.isEmpty()) {
                if (icon.startsWith("http://")) {
                    config.set("icon_disabled_url", icon);
                } else {
                    config.set("icon_disabled", icon);
                }
            }
        } else {
            config.set("icon_disabled_url", iconDisabledURL);
        }

        String nameTemplate = controller.getMessages().get("skills.item_name", "$skill");
        String skillDisplayName = SkillConfigManager.getRaw(skill, "name", skill.getName());
        config.set("name", nameTemplate.replace("$skill", skillDisplayName));
        config.set("category", "skills");
        String descriptionTemplate = controller.getMessages().get("skills.item_description", "$description");
        descriptionTemplate = descriptionTemplate.replace("$description", SkillConfigManager.getRaw(skill, "description", ""));
        config.set("description", descriptionTemplate);
        newSpell.loadTemplate("heroes*" + skillName, config);
        return newSpell;
    }

    public String getClassName(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return "";
        HeroClass heroClass = hero.getHeroClass();
        if (heroClass == null) return "";
        return heroClass.getName();
    }

    public String getSecondaryClassName(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return "";
        HeroClass heroClass = hero.getSecondClass();
        if (heroClass == null) return "";
        return heroClass.getName();
    }

    @Nullable
    protected Skill getSkill(String key) {
        if (skills == null) return null;
        return skills.getSkill(key);
    }

    @Nullable
    protected Hero getHero(Player player) {
        if (characters == null) return null;
        return characters.getHero(player);
    }

    @Override
    public int getMaxMana(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return 0;
        return hero.getMaxMana();
    }

    @Override
    public int getManaRegen(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return 0;
        return hero.getManaRegen();
    }

    @Override
    public float getMana(Player player) {
        Hero hero = getHero(player);
        if (hero == null) return 0;
        return hero.getMana();
    }

    @Override
    public void removeMana(Player player, float amount) {
        Hero hero = getHero(player);
        if (hero == null) return;
        hero.setMana(Math.max(0, hero.getMana() - (int)amount));
    }

    @Override
    public void setMana(Player player, float amount) {
        Hero hero = getHero(player);
        if (hero == null) return;
        hero.setMana((int)amount);
    }

    public boolean isInParty(Player source, Player check, boolean pvpCheck) {
        Hero sourceHero = getHero(source);
        Hero checkHero = getHero(check);
        if (sourceHero == null || checkHero == null) return false;

        HeroParty party = sourceHero.getParty();
        if (party == null || (pvpCheck && !party.isNoPvp())) return false;

        return party.getMembers().contains(checkHero);
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (attacker instanceof Player && entity instanceof Player) {
            return isInParty((Player)attacker, (Player)entity, false);
        }
        return false;
    }

    @Override
    public Set<String> getAllAttributes() {
        return attributes.keySet();
    }

    @Nullable
    @Override
    public Double getAttributeValue(String attribute, Player player) {
        Double result = null;
        Hero hero = getHero(player);
        if (hero == null) {
            return null;
        }
        try {
            if (getHeroAttributeMethod != null) {
                Enum<?> attributeType = attributes.get(attribute);
                if (attributeType != null) {
                    result = (double)(int)getHeroAttributeMethod.invoke(hero, attributeType);
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed read Hero attribute", ex);
        }
        return result;
    }
}
