package com.elmakers.mine.bukkit.integration.valhalla;

import org.bukkit.configuration.file.YamlConfiguration;

import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.skills.skills.Skill;

public class MagicSkill extends Skill {
    private final String id;
    private final int priority;

    public MagicSkill(String skillId, int priority) {
        super(skillId.toUpperCase());
        id = skillId;
        this.priority = priority;
    }

    @Override
    public void loadConfiguration() {
        /*
        ValhallaMMO.getInstance().save("skills/" + id + "_progression.yml");
        ValhallaMMO.getInstance().save("skills/" + id + ".yml");
        YamlConfiguration skillConfig = ConfigManager.getConfig("skills/" + id + ".yml").get();
        YamlConfiguration progressionConfig = ConfigManager.getConfig("skills/" + id + "_progression.yml").get();
         */
        YamlConfiguration skillConfig = new YamlConfiguration();
        YamlConfiguration progressionConfig = new YamlConfiguration();
        loadCommonConfig(skillConfig, progressionConfig);
    }

    @Override
    public boolean isLevelableSkill() {
        return true;
    }

    @Override
    public Class<? extends Profile> getProfileType() {
        return MagicProfile.class;
    }

    @Override
    public int getSkillTreeMenuOrderPriority() {
        return priority;
    }
}
