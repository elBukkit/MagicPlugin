package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.requirements.Requirement;
import com.elmakers.mine.bukkit.api.requirements.RequirementsProcessor;
import com.elmakers.mine.bukkit.magic.ManaController;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.manager.AttributeManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SkillAPIManager implements ManaController, AttributeProvider, RequirementsProcessor {
    private final Plugin skillAPIPlugin;
    private final MageController controller;
    private Set<String> attributes = new HashSet<>();

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
        playerData.setMana(Math.max(0, playerData.getMana() - amount));
    }

    @Override
    public void setMana(Player player, float amount) {
        PlayerData playerData = SkillAPI.getPlayerData(player);
        playerData.setMana(amount);
    }

    public boolean hasSkill(Player player, String name){
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return playerData.hasSkill(name);
    }

    public boolean hasClass(Player player, String name){
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return playerData.hasClass(name);
    }

    @Override
    public boolean checkRequirement(@Nonnull CastContext castContext, @Nonnull Requirement requirement) {
        Mage mage = castContext.getMage();
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
    
    protected String getMessage(CastContext context, String key) {
        return context.getMessage(key, context.getController().getMessages().get("skillapi." + key));
    }

    @Override
    public @Nullable String getRequirementDescription(@Nonnull CastContext context, @Nonnull Requirement requirement) {
        ConfigurationSection configuration = requirement.getConfiguration();
        if (configuration.contains("skill")) {
            return getMessage(context, "required_skill").replace("$skill", configuration.getString("skill"));   
        }
        if (configuration.contains("class")) {
            return getMessage(context, "required_class").replace("$class", configuration.getString("class"));
        }
        return null;
    }
}
