package com.elmakers.mine.bukkit.integration;

import java.util.Set;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import com.elmakers.mine.bukkit.api.attributes.AttributeProvider;
import com.elmakers.mine.bukkit.magic.MagicController;

import me.athlaeos.valhallammo.playerstats.profiles.ProfileCache;

public class ValhallaManager implements AttributeProvider {
    public ValhallaManager(MagicController controller) {
    }

    @Override
    public Set<String> getAllAttributes() {
        return Set.of("valhalla_level");
    }

    @Override
    public @Nullable Double getAttributeValue(String attribute, Player player) {
        switch (attribute) {
            case "valhalla_level":
                // ProfileCache.getOrCache(player, Profile.class).getLevel();
                return 0.0;
        }
        return null;
    }
}
