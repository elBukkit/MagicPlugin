package com.elmakers.mine.bukkit.integration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.entity.TeamProvider;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageContext;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;
import com.elmakers.mine.bukkit.magic.ManaController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.manager.AttributeManager;

public class SkillAPIManager implements ManaController, AttributeProvider, RequirementsProcessor, TeamProvider {
    private final Plugin skillAPIPlugin;
    private final MageController controller;
    private Set<String> attributes = new HashSet<>();
    private final Set<String> usesMana = new HashSet<>();
    private boolean usesAllies = true;

    private AttributeManager attributeManager;

    public SkillAPIManager(MageController controller, Plugin skillAPIPlugin) {
        this.controller = controller;
        this.skillAPIPlugin = skillAPIPlugin;
    }

    public boolean initialize() {
        if (skillAPIPlugin == null) return false;
        if (!(skillAPIPlugin instanceof SkillAPI)) return false;

        attributeManager = SkillAPI.getAttributeManager();

        if (attributeManager == null) {
            attributes = null;
            controller.getLogger().info("SkillAPI found but but attributes are disabled");
        } else {
            attributes = attributeManager.getKeys();
            controller.getLogger().info("SkillAPI Attributes: " + attributes);
        }

        return true;
    }

    public void load(ConfigurationSection configuration) {
        boolean useMana = configuration.getBoolean("use_skillapi_mana", true);
        usesMana.clear();
        if (useMana) {
            usesMana.addAll(ConfigurationUtils.getStringList(configuration, "heroes_mana_classes"));
        }

        usesAllies = configuration.getBoolean("use_skillapi_allies", true);
        if (usesAllies) {
            controller.getLogger().info("SKillAPI allies will be respected in friendly fire checks");
        }
        if (!usesMana.isEmpty()) {
            controller.getLogger().info("SkillAPI mana will be used for classes: " + StringUtils.join(usesMana, ",") + ")");
        }
    }

    public boolean usesAllies() {
        return usesAllies;
    }

    public boolean usesMana(String mageClass) {
        return usesMana.contains(mageClass);
    }

    @Override
    public Set<String> getAllAttributes() {
        return attributes;
    }

    @Nullable
    @Override
    public Double getAttributeValue(String attribute, Player player) {
        if (attributes == null || !attributes.contains(attribute)) return null;
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return (double)playerData.getAttribute(attribute);
    }

    @Override
    public int getMaxMana(Player player) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return (int)playerData.getMaxMana();
    }

    @Override
    public int getManaRegen(Player player) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        Collection<PlayerClass> classes = playerData.getClasses();
        double amount = 0;
        for (PlayerClass c : classes)
        {
            if (c.getData().hasManaRegen())
            {
                amount += c.getData().getManaRegen();
            }
        }

        return (int)amount;
    }

    @Override
    public float getMana(Player player) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return (float)playerData.getMana();
    }

    @Override
    public void removeMana(Player player, float amount) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        playerData.useMana(amount);
    }

    @Override
    public void setMana(Player player, float amount) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        playerData.setMana(amount);
    }

    public boolean hasSkill(Player player, String name) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return playerData.hasSkill(name);
    }

    public boolean hasClass(Player player, String name) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return playerData.hasClass(name);
    }

    @Override
    public boolean checkRequirement(@Nonnull MageContext context, @Nonnull Requirement requirement) {
        Mage mage = context.getMage();
        if (!mage.isPlayer()) return false;
        ConfigurationSection configuration = requirement.getConfiguration();
        if (configuration.contains("skill")) {
            String skillKey = configuration.getString("skill");
            return hasSkill(mage.getPlayer(), skillKey);
        }
        if (configuration.contains("class")) {
            String classKey = configuration.getString("class");
            return hasClass(mage.getPlayer(), classKey);
        }
        return true;
    }

    protected String getMessage(MageContext context, String key) {
        return context.getMessage(key, context.getController().getMessages().get("skillapi." + key));
    }

    @Override
    public @Nullable String getRequirementDescription(@Nonnull MageContext context, @Nonnull Requirement requirement) {
        ConfigurationSection configuration = requirement.getConfiguration();
        if (configuration.contains("skill")) {
            return getMessage(context, "required_skill").replace("$skill", configuration.getString("skill"));
        }
        if (configuration.contains("class")) {
            return getMessage(context, "required_class").replace("$class", configuration.getString("class"));
        }
        return null;
    }

    @Override
    public boolean isFriendly(Entity attacker, Entity entity) {
        if (!(attacker instanceof LivingEntity) || !(entity instanceof LivingEntity)) return false;
        return SkillAPI.getSettings().isAlly((LivingEntity)attacker, (LivingEntity)entity);
    }
}
