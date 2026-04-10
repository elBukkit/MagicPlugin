package com.elmakers.mine.bukkit.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.magic.MagicController;

import me.athlaeos.valhallammo.playerstats.profiles.Profile;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;
import me.athlaeos.valhallammo.playerstats.profiles.ProfileRegistry;

public class ValhallaManager implements AttributeProvider {
    private final MagicController controller;
    private boolean enabled = false;
    private final Map<String, Profile> registeredProfiles = new HashMap<>();
    private final Set<String> attributes = new HashSet<>();

    public ValhallaManager(MagicController controller) {
        this.controller = controller;
    }

    protected void buildData() {
    }

    public void load(ConfigurationSection config) {
        registeredProfiles.clear();
        attributes.clear();
        enabled = config.getBoolean("enabled");
        if (!enabled) {
            controller.getLogger().info("ValhallaMMO integration disabled.");
            return;
        }

        for (Profile profile : ProfileRegistry.getRegisteredProfiles().values()) {
            String profileId = profile.getTableName().replace("profiles_", "");
            registeredProfiles.put(profileId, profile);
            attributes.add("valhalla_level_" + profileId);
        }
        controller.getLogger().info("Integrated with ValhallaMMO:");
        controller.getLogger().info("  Added " + attributes.size() + " attributes as valhalla_level_<profile>");
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
            Profile profile = registeredProfiles.get(profileId);
            if (profile == null) {
                return null;
            }
            Profile playerProfile = ProfileCache.getOrCache(player, profile.getClass());
            if (playerProfile == null) {
                return null;
            }
            return (double)playerProfile.getLevel();
        }
        return null;
    }
}
