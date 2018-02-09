package com.elmakers.mine.bukkit.integration;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.manager.AttributeManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;

public class SkillAPIManager {
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
}
