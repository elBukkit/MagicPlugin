package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.magic.ManaController;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.manager.AttributeManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SkillAPIManager implements ManaController, AttributeProvider {
    private final Plugin skillAPIPlugin;
    private final Plugin owningPlugin;
    private Set<String> attributes = new HashSet<>();

    private AttributeManager attributeManager;

    public SkillAPIManager(Plugin owningPlugin, Plugin skillAPIPlugin) {
        this.owningPlugin = owningPlugin;
        this.skillAPIPlugin = skillAPIPlugin;
    }

    public boolean initialize() {
        if (skillAPIPlugin == null) return false;
        if (!(skillAPIPlugin instanceof SkillAPI)) return false;

        attributeManager = SkillAPI.getAttributeManager();

        if (attributeManager == null) {
            attributes = null;
            owningPlugin.getLogger().warning("SkillAPI but but attributes are disabled");
        } else {
            attributes = attributeManager.getKeys();
            owningPlugin.getLogger().info("SkillAPI Attributes: " + attributes);
        }

        return true;
    }

    @Override
    public Set<String> getAllAttributes() {
        return attributes;
    }

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
}
