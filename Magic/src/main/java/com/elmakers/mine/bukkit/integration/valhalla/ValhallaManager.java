package com.elmakers.mine.bukkit.integration.valhalla;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.magic.Mage;
import com.elmakers.mine.bukkit.magic.MagicController;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.elmakers.mine.bukkit.utility.StringUtils;
import com.elmakers.mine.bukkit.wand.Wand;

import me.athlaeos.valhallammo.event.PlayerSkillLevelUpEvent;
import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;
import me.athlaeos.valhallammo.skills.perk_rewards.PerkRewardRegistry;
import me.athlaeos.valhallammo.skills.skills.Skill;
import me.athlaeos.valhallammo.skills.skills.SkillRegistry;

public class ValhallaManager implements AttributeProvider, Listener {
    private final MagicController controller;
    private boolean enabled = false;
    private boolean registeredSkill = false;
    private MagicSkill magicSkill;
    private final Map<String, Skill> registeredSkills = new HashMap<>();
    private final Set<String> attributes = new HashSet<>();

    public ValhallaManager(MagicController controller) {
        this.controller = controller;
    }

    public void load(ConfigurationSection config) {
        attributes.clear();
        enabled = config.getBoolean("enabled");
        if (!enabled) {
            controller.getLogger().info("ValhallaMMO integration disabled.");
            return;
        }

        controller.getLogger().info("Integrated with ValhallaMMO:");
        if (!controller.getLoadedExamples().contains("valhalla")) {
            controller.getLogger().info("  Use \"/mexample add valhalla\" to enable the magic skill in Valhalla");
        }

        for (Skill skill : SkillRegistry.getAllSkills().values()) {
            String skillId = skill.getType().toLowerCase(Locale.ROOT);
            registeredSkills.put(skillId, skill);
        }

        ConfigurationSection profileConfig = config.getConfigurationSection("profile");
        if (profileConfig != null && profileConfig.getBoolean("enabled")) {
            ConfigurationSection skillConfig = profileConfig.getConfigurationSection("skill");
            String skillId = skillConfig == null ? null : skillConfig.getString("id");
            String profileId = profileConfig.getString("id");
            if (skillId != null && profileId != null && !skillId.isEmpty() && !profileId.isEmpty()) {
                ProfileRegistry.registerProfileType(new MagicProfile(profileId));
                magicSkill = new MagicSkill(controller, skillId, profileConfig.getString("class"), skillConfig);
                registeredSkills.put(skillId, magicSkill);
                controller.getLogger().info("  Added " + profileId + " profile using " + skillId + " skill ");
            }
        }

        registerAttributes();
    }

    protected void registerAttributes() {
        for (String skillId : registeredSkills.keySet()) {
            attributes.add("valhalla_level_" + skillId);
        }

        controller.getLogger().info("  Added " + attributes.size() + " ValhallaMMO levels as attributes: " + StringUtils.join(attributes, ","));
    }

    public void registerSkill() {
        if (enabled && magicSkill != null && !registeredSkill) {
            PerkRewardRegistry.register(new SpellPerkReward(controller, magicSkill.getMageClass(),"learn_spell"));
            PerkRewardRegistry.register(new RecipePerkReward(controller, "discover_recipe"));
            SkillRegistry.registerSkill(magicSkill);

            Plugin plugin = controller.getPlugin();
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            registeredSkill = true;
        }
    }

    public void registerCurrencies(ConfigurationSection currencyConfiguration) {
        if (!enabled) return;

        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Skill> entry : registeredSkills.entrySet()) {
            String skillId = entry.getKey();
            String currencyId = "valhalla_xp_" + skillId;
            ConfigurationSection configuration = currencyConfiguration.getConfigurationSection(skillId);
            if (configuration == null) {
                configuration = ConfigurationUtils.newConfigurationSection();
                // We will rely on Valhalla to send messages
                configuration.set("silent", true);
            }
            controller.register(new ValhallaCurrency(this, entry.getValue(), currencyId, configuration));
            names.add(currencyId);
        }
        controller.getLogger().info("  Added " + names.size() + " ValhallaMMO XP as currencies: " + StringUtils.join(names, ","));
    }

    public MageController getController() {
        return controller;
    }

    @Override
    public Set<String> getAllAttributes() {
        return attributes;
    }

    @Override
    public @Nullable Double getAttributeValue(String attribute, Player player) {
        if (!enabled) return null;

        if (attribute.startsWith("valhalla_level_")) {
            String profileId = attribute.substring("valhalla_level_".length());
            Skill skill = registeredSkills.get(profileId);
            if (skill == null) {
                return null;
            }
            Class<? extends Profile> profileClass = skill.getProfileType();
            if (profileClass == null) {
                return null;
            }
            Profile playerProfile = ProfileCache.getOrCache(player, profileClass);
            if (playerProfile == null) {
                return null;
            }
            return (double)playerProfile.getLevel();
        }
        return null;
    }

    @EventHandler
    public void onPlayerSkillLevelUp(PlayerSkillLevelUpEvent event) {
        if (!enabled) return;

        Mage mage = controller.getRegisteredMage(event.getPlayer());
        if (mage == null) return;

        Wand wand = mage.getActiveWand();
        if (wand != null) {
            // Force lore update in case mana or other properties are based on level
            wand.updated();
        }
    }
}
