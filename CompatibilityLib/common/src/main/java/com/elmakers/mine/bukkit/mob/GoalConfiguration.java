package com.elmakers.mine.bukkit.mob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.utility.ConfigUtils;

public class GoalConfiguration {
    private final GoalType goalType;
    private final ConfigurationSection configuration;

    private GoalConfiguration(GoalType goalType, ConfigurationSection configuration) {
        this.goalType = goalType;
        this.configuration = configuration;
    }

    public static GoalConfiguration fromConfiguration(ConfigurationSection parent, Object rawGoal, Logger logger, String logContext) {
        String goalKey;
        ConfigurationSection config;
        if (rawGoal instanceof String) {
            goalKey = (String)rawGoal;
            config = ConfigUtils.newSection(parent);
        } else {
            if (rawGoal instanceof Map) {
                rawGoal = ConfigUtils.toConfigurationSection(parent, (Map<?,?>)rawGoal);
            }
            if (rawGoal instanceof ConfigurationSection) {
                config = (ConfigurationSection)rawGoal;
                goalKey = config.getString("goal");
            } else {
                goalKey = null;
                config = null;
            }
        }
        if (goalKey == null || goalKey.isEmpty()) {
            logger.warning("Goal missing goal type in  " + logContext);
            return null;
        }
        GoalType goalType;
        try {
            goalType = GoalType.valueOf(goalKey.toUpperCase());
        } catch (Exception ex) {
            logger.warning("Invalid goal type in mob " + logContext);
            return null;
        }
        return new GoalConfiguration(goalType, config);
    }

    @Nullable
    public static Collection<GoalConfiguration> fromList(ConfigurationSection parent, String key, Logger logger, String logContext) {
        if (!parent.contains(key)) return null;
        List<GoalConfiguration> list = new ArrayList<>();
        if (parent.isList(key)) {
            List<?> goals = parent.getList("goals");
            for (Object rawGoal : goals) {
                GoalConfiguration goal = fromConfiguration(parent, rawGoal, logger, logContext);
                if (goal == null) continue;
                list.add(goal);
            }
        } else {
            GoalConfiguration single = fromConfiguration(parent, parent.get(key), logger, logContext);
            list.add(single);
        }
        return list;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }
}
