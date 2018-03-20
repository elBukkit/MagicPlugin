package com.elmakers.mine.bukkit.api.wand;

import com.elmakers.mine.bukkit.api.effect.EffectPlayer;
import com.elmakers.mine.bukkit.api.magic.Mage;
import com.elmakers.mine.bukkit.api.magic.MagicProperties;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.Set;

public interface WandTemplate extends MagicProperties {
    String getKey();
    Collection<EffectPlayer> getEffects(String key);
    ConfigurationSection getConfiguration();
    boolean hasTag(String tag);
    String getCreatorId();
    String getCreator();
    WandTemplate getMigrateTemplate();
    String migrateIcon(String icon);
    boolean isRestorable();
    Set<String> getCategories();
    ConfigurationSection getAttributes();
    String getAttributeSlot();
    WandTemplate getParent();

    boolean playEffects(Wand wand, String key);
    boolean playEffects(Wand wand, String key, float scale);

    @Deprecated
    boolean playEffects(Mage mage, String key);
    @Deprecated
    boolean playEffects(Mage mage, String key, float scale);
    @Deprecated
    boolean isSoul();
}
