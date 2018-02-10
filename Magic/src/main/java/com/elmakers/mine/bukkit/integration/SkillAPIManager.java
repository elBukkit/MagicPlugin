package com.elmakers.mine.bukkit.integration;

import com.elmakers.mine.bukkit.magic.ManaController;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.manager.AttributeManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SkillAPIManager implements ManaController {
    private final Plugin skillAPIPlugin;
    private final Plugin owningPlugin;

    private AttributeManager attributeManager;

    public SkillAPIManager(Plugin owningPlugin, Plugin skillAPIPlugin) {
        this.owningPlugin = owningPlugin;
        this.skillAPIPlugin = skillAPIPlugin;
    }

    public boolean initialize() {
        if (skillAPIPlugin == null) return false;
        if (!(skillAPIPlugin instanceof SkillAPI)) return false;

        attributeManager = SkillAPI.getAttributeManager();
        org.bukkit.Bukkit.getLogger().info("man: " + attributeManager);

        if (attributeManager == null) {
            owningPlugin.getLogger().warning("SkillAPI but but attributes are disabled");
        } else {
            owningPlugin.getLogger().info("SkillAPI Attributes: " + getAttributeKeys());
        }

        return true;
    }

    public Set<String> getAttributeKeys() {
        return attributeManager == null ? null : attributeManager.getKeys();
    }

    public Map<String, Integer> getAttributes(Player player) {
        if (attributeManager == null) return null;
        PlayerData playerData = SkillAPI.getPlayerData(player);
        return playerData.getAttributes();
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
