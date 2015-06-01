package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WandTemplate {
    private final MageController controller;
    private final String key;
    private final ConfigurationSection configuration;
    private Map<String, Collection<EffectPlayer>> effects = new HashMap<String, Collection<EffectPlayer>>();
    private Collection<EffectPlay> currentEffects = new ArrayList<EffectPlay>();

    public WandTemplate(MageController controller, String key, ConfigurationSection node) {
        this.key = key;
        this.configuration = node;
        this.controller = controller;

        effects.clear();
        if (node.contains("effects")) {
            ConfigurationSection effectsNode = node.getConfigurationSection("effects");
            Collection<String> effectKeys = effectsNode.getKeys(false);
            for (String effectKey : effectKeys) {
                if (effectsNode.isString(effectKey)) {
                    String referenceKey = effectsNode.getString(effectKey);
                    if (effects.containsKey(referenceKey)) {
                        effects.put(effectKey, new ArrayList(effects.get(referenceKey)));
                    }
                }
                else
                {
                    effects.put(effectKey, EffectPlayer.loadEffects(controller.getPlugin(), effectsNode, effectKey));
                }
            }
        }
    }

    public String getKey() {
        return key;
    }

    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    public Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> getEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        if (effectList == null) {
            return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>();
        }
        return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>(effectList);
    }

    public void playEffects(Mage mage, String key)
    {
        playEffects(mage, key, 1.0f);
    }

    public void playEffects(Mage mage, String effectName, float scale)
    {
        currentEffects.clear();
        Location wandLocation = null;
        Location location = mage.getLocation();
        Location eyeLocation = mage.getEyeLocation();
        Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> effects = getEffects(effectName);
        if (effects.size() > 0)
        {
            Entity sourceEntity = mage.getEntity();
            for (com.elmakers.mine.bukkit.api.effect.EffectPlayer player : effects)
            {
                // Track effect plays for cancelling
                player.setEffectPlayList(currentEffects);

                // Set scale
                player.setScale(scale);

                // Set material and color
                player.setColor(mage.getEffectColor());
                String overrideParticle = mage.getEffectParticleName();
                player.setParticleOverride(overrideParticle);

                Location source = player.shouldUseEyeLocation() ? eyeLocation : location;
                if (player.shouldUseWandLocation()) {
                    if (wandLocation == null) {
                        wandLocation = mage.getWandLocation();
                    }
                    location = wandLocation;
                }

                player.start(source, sourceEntity, null, null, null);
            }
        }
    }
}
