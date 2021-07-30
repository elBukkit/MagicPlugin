package com.elmakers.mine.bukkit.integration;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.configuration.Option;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.skills.Skills;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.stats.Stats;
import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.ManaController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;

public class AureliumSkillsManager implements ManaController, AttributeProvider {
    private final MageController controller;
    private final Set<String> usesMana = new HashSet<>();
    private boolean enabled;
    private boolean useAttributes;
    private double manaScale;
    private double manaCostReduction;

    public AureliumSkillsManager(MageController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection configuration) {
        enabled = configuration.getBoolean("enabled", true);
        useAttributes = enabled && configuration.getBoolean("use_attributes", true);
        manaScale = configuration.getDouble("mana_scale");
        manaCostReduction = configuration.getDouble("mana_cost_reduction");
        if (manaScale <= 0) {
            controller.getLogger().info("Invalid mana scale in aurelium_sklls configuration: " + manaScale);
            manaScale = 1;
        }

        boolean useMana = enabled && configuration.getBoolean("use_mana", true);
        usesMana.clear();
        if (useMana) {
            usesMana.addAll(ConfigurationUtils.getStringList(configuration, "mana_classes"));
        } else {
            manaCostReduction = 0;
        }
        useMana = !usesMana.isEmpty();

        String statusString;
        if (!useMana && !useAttributes) {
            statusString = " but integration is disabled in configs";
        } else {
            statusString = ", will integrate for ";
            if (useMana) {
                statusString += "mana for classes (" + StringUtils.join(usesMana, ",") + ")";
                if (useAttributes) {
                    statusString += " and skill/stat attributes";
                }
            } else {
                statusString += "attributes";
            }
        }
        controller.getLogger().info("AureliumSkills found " + statusString);
    }

    public double getManaCostReduction() {
        return manaCostReduction;
    }

    public boolean useMana(String mageClass) {
        return usesMana.contains(mageClass);
    }

    public boolean useAttributes() {
        return useAttributes;
    }

    @Override
    public double getMaxMana(Player player) {
        return (int)(manaScale * AureliumAPI.getMaxMana(player));
    }

    @Override
    public double getManaRegen(Player player) {
        double regen = OptionL.getDouble(Option.REGENERATION_BASE_MANA_REGEN) + AureliumAPI.getStatLevel(player, Stats.REGENERATION) * OptionL.getDouble(Option.REGENERATION_MANA_MODIFIER);
        return (manaScale * regen);
    }

    @Override
    public double getMana(Player player) {
        return (float)(manaScale * AureliumAPI.getMana(player));
    }

    @Override
    public void removeMana(Player player, double amount) {
        AureliumAPI.setMana(player, AureliumAPI.getMana(player) - (amount / manaScale));
    }

    @Override
    public void setMana(Player player, double amount) {
        AureliumAPI.setMana(player, amount / manaScale);
    }

    @Override
    public Set<String> getAllAttributes() {
        Set<String> stats = new HashSet<>();
        for (Stats stat : Stats.values()) {
            stats.add(stat.name());
        }
        for (Skills skill : Skills.values()) {
            stats.add(skill.name());
        }
        return stats;
    }

    @Nullable
    @Override
    public Double getAttributeValue(String attribute, Player player) {
        try {
            Stat stat = Stats.valueOf(attribute);
            return AureliumAPI.getStatLevel(player, stat);
        } catch (Exception ignore) {
        }
        try {
            Skills skill = Skills.valueOf(attribute);
            return (double)AureliumAPI.getSkillLevel(player, skill);
        } catch (Exception ignore) {
        }
        return null;
    }
}
