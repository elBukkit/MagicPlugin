package com.elmakers.mine.bukkit.progression;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;

import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.item.Cost;
import com.elmakers.mine.bukkit.utility.random.RandomUtils;

public class ProgressionLevel implements com.elmakers.mine.bukkit.api.magic.ProgressionLevel {
    private final int level;
    private final Map<String, Cost> costs = new HashMap<>();

    public ProgressionLevel(MageController controller, int level, ConfigurationSection configuration) {
        this.level = level;
        List<Cost> costs = Cost.parseCosts(configuration.getConfigurationSection("costs"), controller);
        if (costs != null) {
            for (Cost cost : costs) {
                this.costs.put(cost.getType(), cost);
            }
        }
        String costKey = configuration.getString("cost");
        if (costKey != null && !costKey.isEmpty()) {
            Cost cost = Cost.parseCost(controller, costKey, "xp");
            if (cost != null) {
                Cost duplicate = this.costs.get(cost.getType());
                if (duplicate != null) {
                    duplicate.setAmount(duplicate.getAmount() + cost.getAmount());
                } else {
                    this.costs.put(cost.getType(), cost);
                }
            }
        }
    }

    public ProgressionLevel(int level, ProgressionLevel first, ProgressionLevel last) {
        this.level = level;
        this.costs.putAll(first.costs);
        for (Map.Entry<String, Cost> entry : last.costs.entrySet()) {
            Cost cost = this.costs.get(entry.getKey());
            if (cost == null) {
                this.costs.put(entry.getKey(), entry.getValue());
            } else {
                float distance = (float)(level - first.getLevel()) / (last.getLevel() - first.getLevel());
                cost.setAmount(RandomUtils.lerp(cost.getAmount(), entry.getValue().getAmount(), distance));
            }
        }
    }

    @Nullable
    @Override
    public Collection<Cost> getCosts() {
        return costs.values();
    }

    @Override
    public int getLevel() {
        return level;
    }
}
