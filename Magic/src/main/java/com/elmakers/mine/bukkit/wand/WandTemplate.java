package com.elmakers.mine.bukkit.wand;

import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MageController;
import com.elmakers.mine.bukkit.api.wand.Wand;
import com.elmakers.mine.bukkit.effect.EffectPlayer;
import com.elmakers.mine.bukkit.magic.BaseMagicProperties;
import com.elmakers.mine.bukkit.utility.ConfigurationUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class WandTemplate extends BaseMagicProperties implements com.elmakers.mine.bukkit.api.wand.WandTemplate {
    private final String key;
    private Map<String, Collection<EffectPlayer>> effects = new HashMap<>();
    private Set<String> tags;
    private @Nonnull Set<String> categories = ImmutableSet.of();
    private String creator;
    private String creatorId;
    private String migrateTemplate;
    private String migrateIcon;
    private String icon;
    private boolean restorable;
    private Map<String, String> migrateIcons;
    private ConfigurationSection attributes;
    private String attributeSlot;
    private String parentKey;

    public WandTemplate(MageController controller, String key, ConfigurationSection node) {
        super(controller);

        // Migrate attributes
        attributes = node.getConfigurationSection("item_attributes");
        ConfigurationSection migrateAttributes = node.getConfigurationSection("attributes");
        if (migrateAttributes != null) {
            boolean nagged = false;
            Set<String> keys = migrateAttributes.getKeys(false);
            for (String attributeKey : keys) {
                try {
                    Attribute attribute = Attribute.valueOf(attributeKey.toUpperCase());
                    if (attribute != null) {
                        if (attributes == null) {
                            attributes = node.createSection("item_attributes");
                        }
                        attributes.set(attributeKey, migrateAttributes.get(attributeKey));
                        node.set("attributes", null);
                        if (!nagged) {
                            nagged = true;
                            controller.getLogger().warning("You have vanilla item attributes in the 'attributes' property of wand template '" + key + "', please rename that to item_attributes.");
                        }
                    }
                } catch (Exception ex) {

                }
            }
        }

        this.load(node);
        this.key = key;

        effects.clear();
        creator = node.getString("creator");
        creatorId = node.getString("creator_id");
        migrateTemplate = node.getString("migrate_to");
        migrateIcon = node.getString("migrate_icon");
        restorable = node.getBoolean("restorable", true);
        icon = node.getString("icon");
        attributeSlot = node.getString("item_attribute_slot", node.getString("attribute_slot"));
        parentKey = node.getString("inherit");

        // Remove some properties that should not transfer to wands
        clearProperty("creator");
        clearProperty("creator_id");
        clearProperty("migrate_to");
        clearProperty("migrate_icon");
        clearProperty("restorable");
        clearProperty("hidden");
        clearProperty("enabled");
        clearProperty("inherit");

        ConfigurationSection migrateConfig = node.getConfigurationSection("migrate_icons");
        if (migrateConfig != null) {
            migrateIcons = new HashMap<>();
            Set<String> keys = migrateConfig.getKeys(false);
            for (String migrateKey : keys) {
                migrateIcons.put(migrateKey, migrateConfig.getString(migrateKey));
            }
            clearProperty("migrate_icons");
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
            clearProperty("effects");
        }

        Collection<String> tagList = ConfigurationUtils.getStringList(node, "tags");
        if (tagList != null) {
            tags = new HashSet<>(tagList);
            clearProperty("tags");
        } else {
            tags = null;
        }

        Collection<String> categoriesList = ConfigurationUtils.getStringList(node, "categories");
        if (categoriesList != null) {
            clearProperty("categories");
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
    public boolean playEffects(Wand wand, String key)
    {
        return playEffects(wand.getMage(), wand, key, 1.0f);
    }

    @Override
    public boolean playEffects(Wand wand, String effectName, float scale) {
        return playEffects(wand.getMage(), wand, key, scale);
    }

    @Override
    @Deprecated
    public boolean playEffects(Mage mage, String key)
    {
        return playEffects(mage, mage.getActiveWand(), key, 1.0f);
    }

    @Override
    @Deprecated
    public boolean playEffects(Mage mage, String effectName, float scale) {
        return playEffects(mage, mage.getActiveWand(), effectName, scale);
    }

    private boolean playEffects(Mage mage, Wand wand, String effectName, float scale)
    {
        Preconditions.checkNotNull(mage, "mage");
        Collection<com.elmakers.mine.bukkit.api.effect.EffectPlayer> effects = getEffects(effectName);
        if (effects.isEmpty()) return false;
        
        Entity sourceEntity = mage.getEntity();
        for (com.elmakers.mine.bukkit.api.effect.EffectPlayer player : effects)
        {
            // Set scale
            player.setScale(scale);

            // Set material and color
            player.setColor(wand == null ? mage.getEffectColor() : wand.getEffectColor());
            String overrideParticle = wand == null ? mage.getEffectParticleName() : wand.getEffectParticleName();
            player.setParticleOverride(overrideParticle);

            Location source = wand == null ? null : player.getSourceLocation(wand.getEffectsContext());
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

    @Nullable
    @Override
    public WandTemplate getMigrateTemplate() {
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
    @Deprecated
    public boolean isSoul() {
        return false;
    }
    
    @Override
    public boolean isRestorable() {
        return restorable;
    }

    @Override
    public ConfigurationSection getAttributes() {
        return attributes;
    }

    @Override
    public String getAttributeSlot() {
        return attributeSlot;
    }

    @Nullable
    @Override
    public com.elmakers.mine.bukkit.api.wand.WandTemplate getParent() {
        if (parentKey != null && !parentKey.isEmpty() && !parentKey.equalsIgnoreCase("false")) {
            return controller.getWandTemplate(parentKey);
        }
        return null;
    }
}
