package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.effect.EffectPlay;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.collect.ImmutableSet;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

public class WandTemplate extends BaseMagicProperties implements com.elmakers.mine.bukkit.api.wand.WandTemplate {
    private final MageController controller;
    private final String key;
    private Map<String, Collection<EffectPlayer>> effects = new HashMap<>();
    private Set<String> tags;
    private @Nonnull Set<String> categories = ImmutableSet.of();
    private String creator;
    private String creatorId;
    private String migrateTemplate;
    private String migrateIcon;
    private String icon;
    private boolean soul;
    private boolean restorable;
    private Map<String, String> migrateIcons;

    public WandTemplate(MageController controller, String key, ConfigurationSection node) {
        this.key = key;
        this.load(node);
        this.controller = controller;

        effects.clear();
        creator = node.getString("creator");
        creatorId = node.getString("creator_id");
        migrateTemplate = node.getString("migrate_to");
        migrateIcon = node.getString("migrate_icon");
        restorable = node.getBoolean("restorable", true);
        icon = node.getString("icon");
        soul = node.getBoolean("soul", false);
        ConfigurationSection migrateConfig = node.getConfigurationSection("migrate_icons");
        if (migrateConfig != null) {
            migrateIcons = new HashMap<>();
            Set<String> keys = migrateConfig.getKeys(false);
            for (String migrateKey : keys) {
                migrateIcons.put(migrateKey, migrateConfig.getString(migrateKey));
            }
        }
        
        if (node.contains("effects")) {
            ConfigurationSection effectsNode = node.getConfigurationSection("effects");
            Collection<String> effectKeys = effectsNode.getKeys(false);
            for (String effectKey : effectKeys) {
                if (effectsNode.isString(effectKey)) {
                    String referenceKey = effectsNode.getString(effectKey);
                    if (effects.containsKey(referenceKey)) {
                        effects.put(effectKey, new ArrayList<>(effects.get(referenceKey)));
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
            tags = new HashSet<>(tagList);
        } else {
            tags = null;
        }

        Collection<String> categoriesList = ConfigurationUtils.getStringList(node, "categories");
        if (categoriesList != null) {
            categories = ImmutableSet.copyOf(categoriesList);
        }
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> getEffects(String key) {
        Collection<EffectPlayer> effectList = effects.get(key);
        if (effectList == null) {
            return new ArrayList<>();
        }
        return new ArrayList<com.elmakers.mine.bukkit.api.effect.EffectPlayer>(effectList);
    }

    @Override
    public boolean playEffects(Mage mage, String key)
    {
        return playEffects(mage, key, 1.0f);
    }

    @Override
    public boolean playEffects(Mage mage, String effectName, float scale)
    {
        Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> effects = getEffects(effectName);
        if (effects.isEmpty()) return false;
        
        Entity sourceEntity = mage.getEntity();
        for (com.elmakers.mine.bukkit.api.effect.EffectPlayer player : effects)
        {
            // Set scale
            player.setScale(scale);

            // Set material and color
            player.setColor(mage.getEffectColor());
            String overrideParticle = mage.getEffectParticleName();
            player.setParticleOverride(overrideParticle);

            Location source = null;
            if (player.shouldUseWandLocation()) {
                source = mage.getWandLocation();
            } else if (player.shouldUseEyeLocation()) {
                source = mage.getEyeLocation();
            }
            if (source == null) {
                source = mage.getLocation();
            }

            player.start(source, sourceEntity, null, null, null);
        }
        
        return true;
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
    public Set<String> getCategories() {
        return categories;
    }

    @Override
    public com.elmakers.mine.bukkit.api.wand.WandTemplate getMigrateTemplate() {
        return migrateTemplate == null ? null : controller.getWandTemplate(migrateTemplate);
    }
    
    @Override
    public String migrateIcon(String currentIcon) {
        if (icon != null && migrateIcon != null && migrateIcon.equals(currentIcon)) {
            return icon;
        }
        if (migrateIcons != null) {
            String newIcon = migrateIcons.get(currentIcon);
            if (newIcon != null) {
                return newIcon;
            }
        }
        return currentIcon;
    }
    
    @Override
    public boolean isSoul() {
        return soul;
    }
    
    @Override
    public boolean isRestorable() {
        return restorable;
    }
}
