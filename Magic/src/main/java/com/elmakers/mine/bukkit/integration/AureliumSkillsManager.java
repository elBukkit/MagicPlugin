package com.elmakers.mine.bukkit.integration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.event.EarnEvent;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.ManaController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.user.SkillsUser;

public class AureliumSkillsManager implements ManaController, AttributeProvider, Listener {
    private final MageController controller;
    private final Set<String> usesMana = new HashSet<>();
    private final Map<Skills, Double> xpEarnRates = new HashMap<>();
    private boolean enabled;
    private boolean useAttributes;
    private double manaScale;
    private double manaCostReduction;
    private boolean registerCurrencies;

    public AureliumSkillsManager(MageController controller) {
        this.controller = controller;
        Plugin plugin = controller.getPlugin();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void load(ConfigurationSection configuration) {
        enabled = configuration.getBoolean("enabled", true);
        useAttributes = enabled && configuration.getBoolean("use_attributes", true);
        manaScale = configuration.getDouble("mana_scale", 1.0);
        manaCostReduction = configuration.getDouble("mana_cost_reduction", 0.0);
        registerCurrencies = enabled && configuration.getBoolean("use_xp_currencies", true);
        if (manaScale <= 0) {
            controller.getLogger().info("Invalid mana scale in aurelium_skills configuration: " + manaScale);
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

        xpEarnRates.clear();
        ConfigurationSection earnRates = configuration.getConfigurationSection("xp_earn_from_sp");
        if (earnRates != null) {
            for (String key : earnRates.getKeys(false)) {
                try {
                    Skills skill = Skills.valueOf(key);
                    xpEarnRates.put(skill, earnRates.getDouble(key));
                } catch (Exception ex) {
                    controller.getLogger().warning("Invalid AuraSkills XP type in xp_earn_from_sp config: " + key);
                }
            }
        }

        String statusString;
        if (!useMana && !useAttributes && !registerCurrencies) {
            statusString = " but integration is disabled in configs";
        } else {
            statusString = ", will integrate for ";
            List<String> integrations = new ArrayList<>();
            if (useMana) {
                integrations.add("mana for classes (" + StringUtils.join(usesMana, ",") + ")");
            }
            if (useAttributes) {
                integrations.add("skill/stat attributes");
            }
            if (registerCurrencies) {
                integrations.add("xp currencies");
            }
            statusString += StringUtils.join(integrations, ",");
        }
        controller.getLogger().info("AuraSkills found" + statusString);
        if (!useMana) {
            controller.getLogger().info("  If you want Magic spells to use AuraSkills mana, use &7/mconfig configure config aurelium_skills.use_mana true");
        }
    }

    public void register(ConfigurationSection currencyConfiguration) {
        if (registerCurrencies) {
            List<String> names = new ArrayList<>();
            for (Skills skill : Skills.values()) {
                ConfigurationSection configuration = currencyConfiguration.getConfigurationSection(skill.name());
                if (configuration == null) {
                    configuration = ConfigurationUtils.newConfigurationSection();
                }
                controller.register(new AureliumSkillCurrency(this, skill, configuration));
                names.add(skill.name());
            }
            controller.getLogger().info("Registered AuraSkills XP as currencies: " + StringUtils.join(names, ","));
        }
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

    private SkillsUser getUser(Player player) {
        return AuraSkillsApi.get().getUser(player.getUniqueId());
    }

    @Override
    public double getMaxMana(Player player) {
        SkillsUser user = getUser(player);
        if (user == null) return 0;
        return manaScale * user.getMaxMana();
    }

    @Override
    public double getManaRegen(Player player) {
        double regen = AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId()).getStatLevel(Stats.REGENERATION);
        return (manaScale * regen);
    }

    @Override
    public double getMana(Player player) {
        SkillsUser user = getUser(player);
        if (user == null) return 0;
        return manaScale * user.getMana();
    }

    @Override
    public void removeMana(Player player, double amount) {
        SkillsUser user = getUser(player);
        if (user == null) return;
        user.setMana(user.getMana() - (amount / manaScale));
    }

    @Override
    public void setMana(Player player, double amount) {
        SkillsUser user = getUser(player);
        if (user == null) return;
        user.setMana(amount / manaScale);
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
        SkillsUser user = getUser(player);
        if (user == null) return null;
        try {
            Stat stat = Stats.valueOf(attribute);
            return (double) user.getStatLevel(stat);
        } catch (Exception ignore) {
        }
        try {
            Skills skill = Skills.valueOf(attribute);
            return (double) user.getSkillLevel(skill);
        } catch (Exception ignore) {
        }
        return null;
    }

    public MageController getController() {
        return controller;
    }

    @EventHandler
    public void onEarn(EarnEvent event) {
        if (event.getEarnCause() != EarnEvent.EarnCause.SPELL_CAST) return;
        Mage mage = event.getMage();
        if (!mage.isPlayer()) return;
        SkillsUser user = getUser(mage.getPlayer());
        if (user == null) return;
        for (Map.Entry<Skills, Double> xpEarnRate : xpEarnRates.entrySet()) {
            user.addSkillXp(xpEarnRate.getKey(), xpEarnRate.getValue() * event.getEarnAmount());
        }
    }
}
