package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WandTemplate implements com.elmakers.mine.bukkit.api.wand.WandTemplate {
    private final MageController controller;
    private final String key;
    private final ConfigurationSection configuration;
    private Map<String, Collection<EffectPlayer>> effects = new HashMap<String, Collection<EffectPlayer>>();
    private Collection<EffectPlay> currentEffects = new ArrayList<EffectPlay>();
    private Set<String> tags;
    private String creator;
    private String creatorId;
    private String migrateTemplate;

    public WandTemplate(MageController controller, String key, ConfigurationSection node) {
        this.key = key;
        this.configuration = node;
        this.controller = controller;

        effects.clear();
        creator = node.getString("creator");
        creatorId = node.getString("creator_id");
        migrateTemplate = node.getString("migrate_to");
        
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

        Collection<String> tagList = ConfigurationUtils.getStringList(node, "tags");
        if (tagList != null) {
            tags = new HashSet<String>(tagList);
        } else {
            tags = null;
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public ConfigurationSection getConfiguration() {
        return configuration;
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> getEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        if (effectList == null) {
            return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>();
        }
        return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>(effectList);
    }

    @Override
    public void playEffects(Mage mage, String key)
    {
        playEffects(mage, key, 1.0f);
    }

    @Override
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

    @Override
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    @Override
    public String getCreator() {
        return creator;
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.WandTemplate getMigrateTemplate() {
        return migrateTemplate == null ? null : controller.getWandTemplate(migrateTemplate);
    }
}
